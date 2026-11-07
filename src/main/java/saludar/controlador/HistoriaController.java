package saludar.controlador;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;
import saludar.servicio.ConsultaService;
import saludar.servicio.ExportadorHistoriaClinica;
import saludar.servicio.PacienteService;

/** Controlador de la historia clinica: consulta y exportacion a archivo (CU-07). */
public class HistoriaController {

    private final PacienteService pacientes;
    private final ConsultaService consultas;
    private final ExportadorHistoriaClinica exportador;
    private final HistoriaVista vista;
    private Paciente actual;
    private List<Consulta> historia = List.of();

    public HistoriaController(PacienteService pacientes, ConsultaService consultas,
                              ExportadorHistoriaClinica exportador, HistoriaVista vista) {
        this.pacientes = pacientes;
        this.consultas = consultas;
        this.exportador = exportador;
        this.vista = vista;
        vista.setControlador(this);
    }

    public void ver(String dni) {
        vista.limpiarHistoria();
        actual = null;
        historia = List.of();
        String criterio = dni == null ? "" : dni.trim();
        if (criterio.isEmpty()) return;
        try {
            Paciente p = pacientes.buscarPorDni(criterio);          // con sus alergias (RF-19)
            historia = consultas.historiaDe(p);
            actual = p;
            vista.mostrarHistoria(p, historia);
        } catch (PacienteNoEncontradoException e) {
            vista.mostrarAviso("Sin resultados", e.getMessage());
        } catch (PersistenciaException e) {
            vista.mostrarError("Error de base de datos", e.getMessage());
        }
    }

    /** Al volver a la pestania se relee la historia abierta (puede haber consultas nuevas). */
    public void refrescar() {
        if (actual != null) ver(actual.getDni());
    }

    /** Exporta la historia clinica abierta a un archivo de texto elegido por el usuario. */
    public void exportar() {
        if (actual == null) return;
        Path destino = vista.elegirDestinoExportacion("HC_" + actual.getDni() + "_" + actual.getApellido() + ".txt");
        if (destino == null) return;
        if (!destino.getFileName().toString().toLowerCase().endsWith(".txt")) {
            destino = destino.resolveSibling(destino.getFileName() + ".txt");
        }
        try {
            exportador.exportar(actual, historia, destino);
            vista.mostrarInfo("Historia exportada", "Se guardo la historia clinica en:\n" + destino.toAbsolutePath());
        } catch (IOException e) {
            vista.mostrarError("Error de archivo", "No se pudo escribir el archivo:\n" + e.getMessage());
        }
    }
}
