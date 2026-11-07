package saludar.controlador;

import java.nio.file.Path;
import java.util.List;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;

/** Vista de la historia clinica (CU-07, CU-08). */
public interface HistoriaVista extends Vista {
    void setControlador(HistoriaController controlador);

    void limpiarHistoria();

    void mostrarHistoria(Paciente paciente, List<Consulta> consultas);

    /** Pide el archivo destino de la exportacion; null si el usuario cancela. */
    Path elegirDestinoExportacion(String nombreSugerido);
}
