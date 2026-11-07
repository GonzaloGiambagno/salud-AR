package saludar.controlador;

import saludar.excepciones.AutenticacionException;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Usuario;
import saludar.servicio.AutenticacionService;

/** Controlador del inicio de sesion (CU-01). */
public class LoginController {

    private final AutenticacionService autenticacion;
    private final LoginVista vista;
    private Usuario usuario;

    public LoginController(AutenticacionService autenticacion, LoginVista vista) {
        this.autenticacion = autenticacion;
        this.vista = vista;
        vista.setControlador(this);
    }

    public void ingresar(String nombreUsuario, char[] clave) {
        try {
            usuario = autenticacion.iniciarSesion(nombreUsuario, clave);
            vista.cerrar();
        } catch (AutenticacionException e) {
            vista.mostrarErrorCredenciales(e.getMessage());
        } catch (PersistenciaException e) {
            vista.mostrarError("Error de base de datos", e.getMessage());
        }
    }

    /** Usuario autenticado, o null si se cancelo el inicio de sesion. */
    public Usuario getUsuario() { return usuario; }
}
