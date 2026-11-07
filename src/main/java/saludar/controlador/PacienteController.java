package saludar.controlador;

import java.util.Arrays;
import java.util.List;
import saludar.excepciones.DniDuplicadoException;
import saludar.excepciones.OperacionNoPermitidaException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.excepciones.PersistenciaException;
import saludar.excepciones.ValidacionException;
import saludar.modelo.Alergia;
import saludar.modelo.Paciente;
import saludar.servicio.PacienteService;

/**
 * Controlador de la gestion de pacientes (CU-02, CU-03, CU-06). Recibe los
 * eventos de la vista, invoca al modelo (PacienteService) y decide que debe
 * mostrar la vista, incluidos los errores.
 */
public class PacienteController {

    private final PacienteService service;
    private final PacienteVista vista;
    private boolean obrasCargadas;

    public PacienteController(PacienteService service, PacienteVista vista) {
        this.service = service;
        this.vista = vista;
        vista.setControlador(this);
    }

    /** Lee de MySQL el padron de obras sociales (una vez) y el listado de pacientes. */
    public void refrescar() {
        try {
            if (!obrasCargadas) {
                vista.mostrarObrasSociales(service.obrasSociales());
                obrasCargadas = true;
            }
            Paciente[] padron = service.listarOrdenadosPorApellido();       // arreglo ordenado
            vista.mostrarPacientes(Arrays.asList(padron), padron.length + " pacientes activos, ordenados por apellido");
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    public void registrar() {
        try {
            Paciente p = service.registrar(vista.leerDatosAlta());
            vista.mostrarInfo("Alta exitosa", "Paciente registrado:\n" + p.getResumen());
            vista.limpiarAlta();
            refrescar();
        } catch (ValidacionException | DniDuplicadoException e) {        // incluye DniInvalidoException
            vista.mostrarAviso("Datos invalidos", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    /** DNI: busqueda binaria y detalle. Texto: filtro por apellido resuelto en MySQL. */
    public void buscar(String texto) {
        String criterio = texto == null ? "" : texto.trim();
        if (criterio.isEmpty()) {
            refrescar();
            return;
        }
        try {
            if (criterio.matches("\\d+")) {
                Paciente p = service.buscarPorDni(criterio);
                refrescar();
                vista.seleccionar(p);
                verDetalle(p);
            } else {
                List<Paciente> r = service.buscarPorApellido(criterio);
                if (r.isEmpty()) {
                    vista.mostrarInfo("Sin resultados", "No hay pacientes activos con apellido \"" + criterio + "\".");
                    return;
                }
                vista.mostrarPacientes(r, r.size() + " resultado(s) para \"" + criterio + "\"");
            }
        } catch (PacienteNoEncontradoException e) {
            vista.mostrarAviso("Sin resultados", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    /** Muestra el detalle y ejecuta la accion que el usuario elija. */
    public void verDetalle(Paciente p) {
        switch (vista.mostrarDetalle(p)) {
            case MODIFICAR -> modificar(p);
            case DAR_DE_BAJA -> darDeBaja(p);
            case AGREGAR_ALERGIA -> agregarAlergia(p);
            default -> { }
        }
    }

    private void modificar(Paciente p) {
        try {
            vista.abrirEdicion(p, service.obrasSociales());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    /** Lo invoca la vista al confirmar la modificacion; si hay errores, la edicion sigue abierta. */
    public void guardarModificacion(Paciente p) {
        try {
            service.actualizar(p, vista.leerDatosEdicion());
            vista.cerrarEdicion();
            vista.mostrarInfo("Paciente actualizado", "Se guardaron los cambios.");
            refrescar();
        } catch (ValidacionException e) {
            vista.mostrarAviso("Datos invalidos", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    private void darDeBaja(Paciente p) {
        if (!vista.confirmar("Baja de paciente", "Dar de baja a " + p.getNombreCompleto()
                + "?\nSu historia clinica se conserva (baja logica, Ley 26.529).")) {
            return;
        }
        try {
            service.darDeBaja(p);
            refrescar();
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    private void agregarAlergia(Paciente p) {
        Alergia a = vista.pedirAlergia(p);
        if (a == null) return;
        try {
            service.agregarAlergia(p, a);
            vista.mostrarInfo("Alergia registrada", "Se registro la alergia a " + a.getSustancia() + ".");
        } catch (OperacionNoPermitidaException | ValidacionException e) {
            vista.mostrarAviso("No se pudo registrar", e.getMessage());
        } catch (PersistenciaException e) {
            errorBD(e);
        }
    }

    private void errorBD(PersistenciaException e) {
        vista.mostrarError("Error de base de datos", e.getMessage());
    }
}
