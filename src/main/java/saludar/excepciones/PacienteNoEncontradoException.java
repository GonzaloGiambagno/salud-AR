package saludar.excepciones;

/** Se lanza cuando no se encuentra un paciente para el criterio dado. */
public class PacienteNoEncontradoException extends Exception {
    public PacienteNoEncontradoException(String criterio) {
        super("No se encontro ningun paciente para: " + criterio + ".");
    }
}
