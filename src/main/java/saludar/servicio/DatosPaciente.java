package saludar.servicio;

import java.time.LocalDate;
import saludar.modelo.ObraSocial;

/**
 * DTO (Data Transfer Object) con los datos que la interfaz captura para dar
 * de alta o modificar un paciente. Es un record de Java: inmutable y sin
 * logica; la validacion la realiza PacienteService.
 *
 * @param obraSocial null si el paciente es particular
 */
public record DatosPaciente(String dni, String apellido, String nombre, LocalDate fechaNacimiento,
                            char sexo, String domicilio, String telefono, String email,
                            ObraSocial obraSocial, String nroAfiliado, String plan) {
}
