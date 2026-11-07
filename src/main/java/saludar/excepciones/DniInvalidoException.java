package saludar.excepciones;

/** Se lanza cuando un DNI no cumple el formato requerido (7 u 8 digitos). */
public class DniInvalidoException extends ValidacionException {
    public DniInvalidoException(String mensaje) { super(mensaje); }
}
