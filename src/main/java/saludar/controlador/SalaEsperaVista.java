package saludar.controlador;

import java.util.List;
import saludar.modelo.Cie10;
import saludar.modelo.Consulta;
import saludar.modelo.IngresoSala;
import saludar.modelo.Paciente;

/** Vista de la sala de espera con triage y del registro de la consulta (CU-04). */
public interface SalaEsperaVista extends Vista {
    void setControlador(SalaEsperaController controlador);

    /**
     * @param conteoPorPrioridad arreglo indexado por el ordinal de Prioridad: [ALTA, MEDIA, BAJA]
     */
    void mostrarSala(List<IngresoSala> ingresos, int[] conteoPorPrioridad, int total);

    void limpiarDni();

    /** Abre el ingreso con seleccion de triage; al confirmar llama a ingresar() del controlador. */
    void abrirIngreso(Paciente p);

    void cerrarIngreso();

    /** Abre el registro de la consulta; al guardar llama a guardarConsulta() del controlador. */
    void abrirConsulta(IngresoSala ingreso, Consulta consulta, List<Cie10> catalogo);

    void cerrarConsulta();
}
