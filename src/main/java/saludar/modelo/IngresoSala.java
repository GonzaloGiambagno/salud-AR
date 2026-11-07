package saludar.modelo;

import java.time.Duration;
import java.time.LocalDateTime;

/** Ingreso de un paciente a la sala de espera, con su prioridad de triage. */
public class IngresoSala {

    private int id;
    private final Paciente paciente;
    private final Prioridad prioridad;
    private final LocalDateTime horaIngreso;
    private EstadoIngreso estado;

    /** Ingreso nuevo: queda EN_ESPERA con la hora actual. */
    public IngresoSala(Paciente paciente, Prioridad prioridad) {
        this(0, paciente, prioridad, LocalDateTime.now(), EstadoIngreso.EN_ESPERA);
    }

    /** Ingreso leido de la base de datos. */
    public IngresoSala(int id, Paciente paciente, Prioridad prioridad,
                       LocalDateTime horaIngreso, EstadoIngreso estado) {
        this.id = id;
        this.paciente = paciente;
        this.prioridad = prioridad;
        this.horaIngreso = horaIngreso;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
