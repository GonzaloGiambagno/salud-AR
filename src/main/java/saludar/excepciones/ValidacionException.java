package saludar.excepciones;

/** Un dato ingresado no cumple las reglas de formato o de negocio (RF-08). */
public class ValidacionException extends Exception {
    public ValidacionException(String mensaje) { super(mensaje); }
}
