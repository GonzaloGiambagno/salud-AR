package saludar.excepciones;

/** Credenciales invalidas o usuario inactivo al iniciar sesion (CU-01). */
public class AutenticacionException extends Exception {
    public AutenticacionException(String mensaje) { super(mensaje); }
}
