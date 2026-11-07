package saludar.servicio;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import saludar.modelo.Alergia;
import saludar.modelo.Consulta;
import saludar.modelo.Diagnostico;
import saludar.modelo.Paciente;

/**
 * Exporta la historia clinica de un paciente a un ARCHIVO de texto (UTF-8),
 * por ejemplo para entregarla al paciente o adjuntarla a una derivacion.
 */
public class ExportadorHistoriaClinica {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void exportar(Paciente p, List<Consulta> historia, Path destino) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            linea(w, "SALUD-AR - HISTORIA CLINICA");
            linea(w, "Emitida el " + LocalDateTime.now().format(FECHA_HORA));
            linea(w, "=".repeat(70));
            linea(w, "Paciente:    " + p.getNombreCompleto());
            linea(w, "DNI:         " + p.getDni());
            linea(w, "Nacimiento:  " + (p.getFechaNacimiento() == null ? "s/d"
                    : p.getFechaNacimiento().format(FECHA) + " (" + p.getEdad() + " anios)"));
            linea(w, "Cobertura:   " + p.getNombreCobertura()
                    + (p.getNroAfiliado() == null ? "" : " - afiliado " + p.getNroAfiliado()));
            if (p.tieneAlergias()) {
                StringBuilder sb = new StringBuilder("ALERGIAS:    ");
                for (Alergia a : p.getAlergias()) sb.append(a).append("  ");
                linea(w, sb.toString().trim());
            } else {
                linea(w, "Alergias:    sin alergias registradas");
            }
            linea(w, "=".repeat(70));
            if (historia.isEmpty()) {
                linea(w, "(sin consultas registradas)");
            }
            for (Consulta c : historia) {
                w.newLine();
                linea(w, "Consulta #" + c.getId() + "  -  " + c.getFechaHora().format(FECHA_HORA)
                        + "  [" + c.getEstado() + "]");
                linea(w, "Profesional: " + c.getProfesional().getResumen());
                linea(w, "Motivo:      " + c.getMotivo());
                if (c.getEvaluacion() != null) linea(w, "Evaluacion:  " + c.getEvaluacion());
                if (c.getIndicaciones() != null) linea(w, "Indicaciones:" + " " + c.getIndicaciones());
                for (Diagnostico d : c.getDiagnosticos()) {
                    linea(w, "Diagnostico: " + d);
                }
                linea(w, "Medicacion:  " + c.getMedicacionResumen());
                if (c.getIdConsultaRectificada() != null) {
                    linea(w, "Rectifica la consulta #" + c.getIdConsultaRectificada());
                }
                linea(w, "-".repeat(70));
            }
        }
    }

    private static void linea(BufferedWriter w, String texto) throws IOException {
        w.write(texto);
        w.newLine();
    }
}
