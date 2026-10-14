package saludar.excepciones;

/** Se lanza cuando un DNI no cumple el formato requerido. */
public class DniInvalidoException extends Exception {
    public DniInvalidoException(String mensaje) { super(mensaje); }
}
