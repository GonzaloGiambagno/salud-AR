package saludar.controlador;

/** Ventana principal: contiene las pestanias y el cierre de sesion. */
public interface PrincipalVista extends Vista {
    void setControlador(PrincipalController controlador);

    void cerrar();
}
