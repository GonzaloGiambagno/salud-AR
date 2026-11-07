package saludar.excepciones;

/** El rol del usuario en sesion no habilita la operacion solicitada (RF-03, RN-08). */
public class OperacionNoPermitidaException extends Exception {
    public OperacionNoPermitidaException(String mensaje) { super(mensaje); }
}
