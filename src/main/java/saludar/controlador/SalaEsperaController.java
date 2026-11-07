package saludar.controlador;

import java.util.List;
import saludar.excepciones.OperacionNoPermitidaException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.excepciones.PersistenciaException;
import saludar.excepciones.ValidacionException;
import saludar.modelo.Cie10;
import saludar.modelo.Consulta;
import saludar.modelo.Diagnostico;
import saludar.modelo.IngresoSala;
import saludar.modelo.Medicacion;
import saludar.modelo.Paciente;
import saludar.modelo.Prioridad;
import saludar.servicio.CatalogoCie10;
import saludar.servicio.ConsultaService;
import saludar.servicio.PacienteService;
import saludar.servicio.SalaDeEspera;

/** Controlador de la sala de espera con triage y del registro de la consulta (CU-04). */
public class SalaEsperaController {

    private final SalaDeEspera sala;
    private final PacienteService pacientes;
    private final ConsultaService consultas;
    private final CatalogoCie10 catalogo;
    private final SalaEsperaVista vista;

    public SalaEsperaController(SalaDeEspera sala, PacienteService pacientes, ConsultaService consultas,
                                CatalogoCie10 catalogo, SalaEsperaVista vista) {
        this.sala = sala;
        this.pacientes = pacientes;
        this.consultas = consultas;
        this.catalogo = catalogo;
        this.vista = vista;
        vista.setControlador(this);
    }

    /** Relee la sala desde MySQL (otras estaciones pueden haber ingresado pacientes). */
    public void refrescar() {
        try {
            sala.recargar();
        } catch (PersistenciaException e) {
            errorBD(e);
        }
        vista.mostrarSala(sala.getIngresos(), sala.conteoPorPrioridad(), sala.enEspera());
    }

    /** Busca al paciente por DNI y abre el ingreso con seleccion de triage. */
    public void buscarParaIngreso(String dni) {
        String criterio = dni == null ? "" : dni.trim();
        if (criterio.isEmpty()) {
            vista.mostrarAviso("Falta el DNI", "Ingrese el DNI del paciente.");
            return;
        }
        try {
            Paciente p = pacientes.buscarPorDni(criterio);
            if (sala.estaEnSala(p)) {
                vista.mostrarInfo("Sala de espera", p.getNombreCompleto() + " ya se encuentra en la sala de espera.");
                return;
            }
            vista.abrirIngreso(p);
        } catch (PacienteNoEncontradoException e) {
            vista.mostrarAviso("Sin resultados", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    /** Lo invoca la vista al confirmar el ingreso. */
    public void ingresar(Paciente p, Prioridad prioridad) {
        if (prioridad == null) {
            vista.mostrarAviso("Falta el triage", "Seleccione un triage (Verde, Amarillo o Rojo).");
            return;
        }
        try {
            sala.ingresar(p, prioridad);
            vista.cerrarIngreso();
            vista.limpiarDni();
            refrescar();
        } catch (ValidacionException e) {
            vista.mostrarAviso("No se pudo ingresar", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    /** Comienza la atencion: valida el rol, marca el ingreso EN_ATENCION y abre la consulta. */
    public void atender(IngresoSala ingreso) {
        try {
            Paciente p = pacientes.obtener(ingreso.getPaciente().getId());     // con sus alergias
            Consulta c = consultas.nuevaConsulta(p);                           // solo rol MEDICO
            sala.iniciarAtencion(ingreso);
            List<Cie10> codigos = catalogo.todas();
            refrescar();
            vista.abrirConsulta(ingreso, c, codigos);
        } catch (OperacionNoPermitidaException | PacienteNoEncontradoException e) {
            vista.mostrarAviso("No se puede atender", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
            refrescar();
        }
    }

    /** Lo invoca la vista al guardar: arma la consulta y la cierra en una transaccion. */
    public void guardarConsulta(IngresoSala ingreso, Consulta c, DatosConsulta d) {
        c.setMotivo(d.motivo() == null ? "" : d.motivo().trim());
        c.setEvaluacion(vacioANulo(d.evaluacion()));
        c.setIndicaciones(vacioANulo(d.indicaciones()));
        c.getDiagnosticos().clear();
        c.getMedicaciones().clear();
        if (d.diagnostico() != null) c.agregarDiagnostico(new Diagnostico(d.diagnostico(), true));
        if (vacioANulo(d.farmaco()) != null) {
            c.agregarMedicacion(new Medicacion(d.farmaco().trim(), texto(d.dosis()), texto(d.frecuencia()),
                    texto(d.duracion())));
        }
        try {
            consultas.registrarYCerrar(c, ingreso);      // queda CERRADA e inviolable
            vista.cerrarConsulta();
            refrescar();
            vista.mostrarInfo("Consulta registrada", "Consulta #" + c.getId() + " [" + c.getEstado() + "] registrada.\n"
                    + "Dx: " + c.getDiagnosticoPrincipal() + "\n"
                    + "Medicacion: " + c.getMedicacionResumen());
        } catch (ValidacionException e) {
            vista.mostrarAviso("Faltan datos", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    private void errorBD(PersistenciaException e) {
        vista.mostrarError("Error de base de datos", e.getMessage());
    }

    private static String vacioANulo(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private static String texto(String s) { return s == null ? "" : s.trim(); }
}
