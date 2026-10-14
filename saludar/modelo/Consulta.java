package saludar.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta / evolucion clinica. Implementa la regla de inviolabilidad:
 * una vez CERRADA no admite modificacion (ver ConsultaService).
 */
public class Consulta {

    private int id;
    private Paciente paciente;
    private Profesional profesional;
    private LocalDateTime fechaHora;
    private String motivo;
    private String evaluacion;
    private EstadoConsulta estado;
    private final List<Diagnostico> diagnosticos = new ArrayList<>();
    private final List<Medicacion> medicaciones = new ArrayList<>();

    public Consulta(int id, Paciente paciente, Profesional profesional, String motivo) {
        this.id = id;
        this.paciente = paciente;
        this.profesional = profesional;
        this.motivo = motivo;
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoConsulta.BORRADOR;
    }

    public int getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Profesional getProfesional() { return profesional; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEvaluacion() { return evaluacion; }
    public void setEvaluacion(String evaluacion) { this.evaluacion = evaluacion; }
    public EstadoConsulta getEstado() { return estado; }
    public void cerrar() { this.estado = EstadoConsulta.CERRADA; }
    public boolean estaCerrada() { return estado == EstadoConsulta.CERRADA; }

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
