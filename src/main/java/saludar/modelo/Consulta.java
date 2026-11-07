package saludar.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta / evolucion clinica. Implementa la regla de inviolabilidad:
 * una vez CERRADA no admite modificacion (ver ConsultaService y los
 * triggers de la base de datos).
 */
public class Consulta {

    private int id;
    private final Paciente paciente;
    private final Profesional profesional;
    private LocalDateTime fechaHora;
    private String motivo;
    private String evaluacion;
    private String indicaciones;
    private EstadoConsulta estado;
    private Integer idConsultaRectificada;
    private final List<Diagnostico> diagnosticos = new ArrayList<>();
    private final List<Medicacion> medicaciones = new ArrayList<>();

    /** Consulta nueva, en BORRADOR, con fecha y hora asignadas automaticamente (RF-12). */
    public Consulta(Paciente paciente, Profesional profesional, String motivo) {
        this(0, paciente, profesional, LocalDateTime.now(), motivo, EstadoConsulta.BORRADOR);
    }

    /** Consulta leida de la base de datos. */
    public Consulta(int id, Paciente paciente, Profesional profesional, LocalDateTime fechaHora,
                    String motivo, EstadoConsulta estado) {
        this.id = id;
        this.paciente = paciente;
        this.profesional = profesional;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public Profesional getProfesional() { return profesional; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEvaluacion() { return evaluacion; }
    public void setEvaluacion(String evaluacion) { this.evaluacion = evaluacion; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public EstadoConsulta getEstado() { return estado; }
    public Integer getIdConsultaRectificada() { return idConsultaRectificada; }
    public void setIdConsultaRectificada(Integer id) { this.idConsultaRectificada = id; }

    public void cerrar() { this.estado = EstadoConsulta.CERRADA; }
    public boolean estaCerrada() { return estado != EstadoConsulta.BORRADOR; }

    public void agregarDiagnostico(Diagnostico d) { diagnosticos.add(d); }
    public List<Diagnostico> getDiagnosticos() { return diagnosticos; }

    public void agregarMedicacion(Medicacion m) { medicaciones.add(m); }
    public List<Medicacion> getMedicaciones() { return medicaciones; }

    public String getMedicacionResumen() {
        if (medicaciones.isEmpty()) return "(sin medicacion)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < medicaciones.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(medicaciones.get(i));
        }
        return sb.toString();
    }

    public String getDiagnosticoPrincipal() {
        for (Diagnostico d : diagnosticos) {       // estructura de repeticion
            if (d.isPrincipal()) return d.toString();
        }
        return diagnosticos.isEmpty() ? "(sin diagnostico)" : diagnosticos.get(0).toString();
    }
}
