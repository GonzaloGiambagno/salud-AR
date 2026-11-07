package saludar.controlador;

import saludar.modelo.Cie10;

/**
 * DTO con lo que el medico carga en el formulario de la consulta. La vista
 * solo lo arma; el controlador lo vuelca en el objeto Consulta del modelo.
 */
public record DatosConsulta(String motivo, String evaluacion, String indicaciones, Cie10 diagnostico,
                            String farmaco, String dosis, String frecuencia, String duracion) {
}
