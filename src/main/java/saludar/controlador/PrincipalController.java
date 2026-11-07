package saludar.controlador;

import saludar.excepciones.PersistenciaException;
import saludar.servicio.AutenticacionService;

/** Controlador de la ventana principal: cierre de sesion y salida (RF-04). */
public class PrincipalController {

    private final AutenticacionService autenticacion;
    private final PrincipalVista vista;
    private final Runnable volverAlLogin;

    public PrincipalController(AutenticacionService autenticacion, PrincipalVista vista, Runnable volverAlLogin) {
        this.autenticacion = autenticacion;
        this.vista = vista;
        this.volverAlLogin = volverAlLogin;
        vista.setControlador(this);
    }

    /** Cierra la sesion (queda auditada) y vuelve a la pantalla de inicio de sesion. */
    public void cerrarSesion() {
        registrarCierre();
        vista.cerrar();
        volverAlLogin.run();
    }

    /** Cierre de la ventana: cierra la sesion y termina la aplicacion. */
    public void salir() {
        registrarCierre();
        System.exit(0);
    }

    private void registrarCierre() {
        try {
            autenticacion.cerrarSesion();
        } catch (PersistenciaException e) {
            vista.mostrarError("Error de base de datos", e.getMessage());
        }
    }
}
