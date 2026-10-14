package saludar.modelo;

import java.time.Duration;
import java.time.LocalDateTime;

/** Ingreso de un paciente a la sala de espera, con su prioridad de triage. */
public class IngresoSala {

    private final int id;
    private final Paciente paciente;
    private final Prioridad prioridad;
    private final LocalDateTime horaIngreso;
    private EstadoIngreso estado;

    public IngresoSala(int id, Paciente paciente, Prioridad prioridad, LocalDateTime horaIngreso) {
        this.id = id;
        this.paciente = paciente;
        this.prioridad = prioridad;
        this.horaIngreso = horaIngreso;
        this.estado = EstadoIngreso.EN_ESPERA;
    }

    public int getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Prioridad getPrioridad() { return prioridad; }
    public LocalDateTime getHoraIngreso() { return horaIngreso; }
    public EstadoIngreso getEstado() { return estado; }

    public void iniciarAtencion() { this.estado = EstadoIngreso.EN_ATENCION; }
    public void finalizar() { this.estado = EstadoIngreso.FINALIZADO; }

    /** Tiempo transcurrido desde el ingreso a la sala. */
    public Duration getTiempoEspera() {
        Duration d = Duration.between(horaIngreso, LocalDateTime.now());
        return d.isNegative() ? Duration.ZERO : d;
    }
}
