package saludar.servicio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import saludar.modelo.IngresoSala;
import saludar.modelo.Paciente;
import saludar.modelo.Prioridad;

/**
 * Sala de espera modelada como COLA CON PRIORIDAD (triage): se atiende primero
 * la prioridad ALTA, luego MEDIA y luego BAJA; a igual prioridad, el primero
 * en llegar es el primero en ser atendido (FIFO).
 * La lista se mantiene ordenada insertando cada ingreso en su posicion.
 */
public class SalaDeEspera {

    private final List<IngresoSala> cola = new ArrayList<>();
    private int secuencia = 0;

    public IngresoSala ingresar(Paciente p, Prioridad prioridad) {
        return ingresar(p, prioridad, LocalDateTime.now());
    }

    public IngresoSala ingresar(Paciente p, Prioridad prioridad, LocalDateTime hora) {
        if (estaEnSala(p)) {
            throw new IllegalStateException(p.getNombreCompleto() + " ya se encuentra en la sala de espera.");
        }
        IngresoSala nuevo = new IngresoSala(++secuencia, p, prioridad, hora);
        int i = 0;
        while (i < cola.size() && vaAntes(cola.get(i), nuevo)) {
            i++;
        }
        cola.add(i, nuevo);
        return nuevo;
    }

    /** Ingreso sin triage (version consola): prioridad BAJA. */
    public void encolar(Paciente p) { ingresar(p, Prioridad.BAJA); }

    public boolean estaEnSala(Paciente p) {
        for (IngresoSala i : cola) {
            if (i.getPaciente() == p) return true;
        }
        return false;
    }

    public void iniciarAtencion(IngresoSala i) { i.iniciarAtencion(); }

    /** La consulta fue guardada: el ingreso pasa a FINALIZADO y sale de la sala. */
    public void finalizar(IngresoSala i) {
        i.finalizar();
        cola.remove(i);
    }

    /** Atiende al primero de la cola (version consola). */
    public Paciente atenderSiguiente() {
        if (cola.isEmpty()) return null;
        IngresoSala i = cola.get(0);
        finalizar(i);
        return i.getPaciente();
    }

    public boolean hayPacientes() { return !cola.isEmpty(); }
    public int enEspera() { return cola.size(); }

    /** Pacientes en sala, en orden de atencion (copia defensiva). */
    public List<IngresoSala> getIngresos() { return new ArrayList<>(cola); }

    private static boolean vaAntes(IngresoSala a, IngresoSala b) {
        int c = a.getPrioridad().compareTo(b.getPrioridad());
        return c != 0 ? c < 0 : !a.getHoraIngreso().isAfter(b.getHoraIngreso());
    }
}
