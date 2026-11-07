package saludar.servicio;

import java.util.ArrayList;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.excepciones.ValidacionException;
import saludar.modelo.EstadoIngreso;
import saludar.modelo.IngresoSala;
import saludar.modelo.Paciente;
import saludar.modelo.Prioridad;
import saludar.persistencia.IngresoSalaDAO;
import saludar.seguridad.Sesion;

/**
 * Sala de espera modelada como COLA CON PRIORIDAD (triage): se atiende primero
 * la prioridad ALTA, luego MEDIA y luego BAJA; a igual prioridad, el primero
 * en llegar es el primero en ser atendido (FIFO).
 *
 * Los ingresos se persisten en MySQL (tabla ingreso_sala), de modo que la sala
 * se comparte entre estaciones de trabajo y sobrevive a un reinicio. Al leerlos
 * se arma la cola en un ArrayList insertando cada ingreso en su posicion.
 */
public class SalaDeEspera {

    private final IngresoSalaDAO dao;
    private final AuditoriaService auditoria;
    private final List<IngresoSala> cola = new ArrayList<>();

    public SalaDeEspera(IngresoSalaDAO dao, AuditoriaService auditoria) {
        this.dao = dao;
        this.auditoria = auditoria;
    }

    /** Vuelve a leer la sala desde la base y la ordena como cola con prioridad. */
    public void recargar() throws PersistenciaException {
        List<IngresoSala> activos = dao.listarActivos();
        cola.clear();
        for (IngresoSala i : activos) {
            encolarOrdenado(i);
        }
    }

    /** Ingreso del paciente con la prioridad asignada por triage. */
    public IngresoSala ingresar(Paciente p, Prioridad prioridad) throws ValidacionException, PersistenciaException {
        recargar();
        if (estaEnSala(p)) {
            throw new ValidacionException(p.getNombreCompleto() + " ya se encuentra en la sala de espera.");
        }
        IngresoSala nuevo = new IngresoSala(p, prioridad);
        dao.insertar(nuevo, Sesion.getInstancia().getIdUsuario());
        encolarOrdenado(nuevo);
        auditoria.registrar("ingreso_sala", nuevo.getId(), "INGRESO_SALA",
                "Ingreso de DNI " + p.getDni() + " - prioridad " + prioridad.getEtiqueta());
        return nuevo;
    }

    /** Insercion ordenada: avanza mientras el elemento actual deba atenderse antes. */
    private void encolarOrdenado(IngresoSala nuevo) {
        int i = 0;
        while (i < cola.size() && vaAntes(cola.get(i), nuevo)) {
            i++;
        }
        cola.add(i, nuevo);
    }

    public boolean estaEnSala(Paciente p) {
        for (IngresoSala i : cola) {
            if (i.getPaciente().getId() == p.getId()) return true;
        }
        return false;
    }

    /** El medico comienza a atender: el ingreso pasa a EN_ATENCION. */
    public void iniciarAtencion(IngresoSala i) throws PersistenciaException {
        if (i.getEstado() == EstadoIngreso.EN_ESPERA) {
            dao.actualizarEstado(i.getId(), EstadoIngreso.EN_ATENCION);
            i.iniciarAtencion();
        }
    }

    /** Ingresos en orden de atencion (copia defensiva). */
    public List<IngresoSala> getIngresos() { return new ArrayList<>(cola); }

    public int enEspera() { return cola.size(); }

    /**
     * Cantidad de pacientes por prioridad. Se usa un ARREGLO de tamanio fijo
     * indexado por el ordinal del enumerado: [0]=ALTA, [1]=MEDIA, [2]=BAJA.
     */
    public int[] conteoPorPrioridad() {
        int[] conteo = new int[Prioridad.values().length];
        for (IngresoSala i : cola) {
            conteo[i.getPrioridad().ordinal()]++;
        }
        return conteo;
    }

    private static boolean vaAntes(IngresoSala a, IngresoSala b) {
        int c = a.getPrioridad().compareTo(b.getPrioridad());
        return c != 0 ? c < 0 : !a.getHoraIngreso().isAfter(b.getHoraIngreso());
    }
}
