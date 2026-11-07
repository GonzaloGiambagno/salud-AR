package saludar.controlador;

/** Vista de inicio de sesion (CU-01). */
public interface LoginVista extends Vista {
    void setControlador(LoginController controlador);

    /** Muestra el error de credenciales en la propia pantalla y limpia la contrasenia. */
    void mostrarErrorCredenciales(String mensaje);

    /** Cierra la pantalla tras un inicio de sesion exitoso. */
    void cerrar();
}
