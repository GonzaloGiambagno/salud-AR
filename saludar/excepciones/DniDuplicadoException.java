package saludar.excepciones;

/** Se lanza al intentar registrar un paciente con un DNI ya existente. */
public class DniDuplicadoException extends Exception {
    public DniDuplicadoException(String dni) {
        super("Ya existe un paciente registrado con el DNI " + dni + ".");
    }
}
