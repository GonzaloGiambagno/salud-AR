package saludar.seguridad;

import saludar.excepciones.OperacionNoPermitidaException;
import saludar.modelo.Rol;
import saludar.modelo.Usuario;

/**
 * Sesion del usuario autenticado. Patron SINGLETON: hay una unica sesion
 * activa por aplicacion, accesible desde los servicios para auditar y
 * controlar permisos por rol (RF-03, RN-08).
 */
public final class Sesion {

    private static final Sesion INSTANCIA = new Sesion();
    private Usuario usuario;

    private Sesion() { }

    public static Sesion getInstancia() { return INSTANCIA; }

    public void iniciar(Usuario u) { this.usuario = u; }
    public void cerrar() { this.usuario = null; }
    public boolean activa() { return usuario != null; }
    public Usuario getUsuario() { return usuario; }

    /** Id del usuario en sesion, o null si no hay sesion (para la auditoria). */
    public Integer getIdUsuario() { return usuario == null ? null : usuario.getId(); }

    /** Verifica que el usuario en sesion tenga alguno de los roles indicados. */
    public void exigirRol(String operacion, Rol... roles) throws OperacionNoPermitidaException {
        if (usuario == null) {
            throw new OperacionNoPermitidaException("Debe iniciar sesion para " + operacion + ".");
        }
        if (!usuario.tieneRol(roles)) {
            throw new OperacionNoPermitidaException("El rol " + usuario.getRol()
                    + " no tiene permiso para " + operacion + ".");
        }
    }
}
