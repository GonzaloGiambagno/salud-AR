package saludar.servicio;

import java.util.Arrays;
import saludar.excepciones.AutenticacionException;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Usuario;
import saludar.persistencia.UsuarioDAO;
import saludar.seguridad.HashContrasena;
import saludar.seguridad.Sesion;

/** Inicio y cierre de sesion contra la base de datos (CU-01, RF-01 a RF-04). */
public class AutenticacionService {

    private final UsuarioDAO usuarioDAO;
    private final AuditoriaService auditoria;

    public AutenticacionService(UsuarioDAO usuarioDAO, AuditoriaService auditoria) {
        this.usuarioDAO = usuarioDAO;
        this.auditoria = auditoria;
    }

    /**
     * Valida las credenciales. La contrasenia llega como char[] (y no como
     * String) para poder borrarla de memoria apenas se verifica.
     */
    public Usuario iniciarSesion(String nombreUsuario, char[] clave)
            throws AutenticacionException, PersistenciaException {
        try {
            if (nombreUsuario == null || nombreUsuario.isBlank() || clave.length == 0) {
                throw new AutenticacionException("Ingrese el usuario y la contrasenia.");
            }
            String nombre = nombreUsuario.trim();
            String hash = usuarioDAO.buscarHash(nombre);
            if (hash == null || !HashContrasena.verificar(clave, hash)) {
                auditoria.registrar("usuario", null, "LOGIN_FALLIDO", "Intento fallido para '" + nombre + "'");
                // Mismo mensaje exista o no el usuario: no revela cuentas validas
                throw new AutenticacionException("Usuario o contrasenia incorrectos.");
            }
            Usuario u = usuarioDAO.buscarPorNombre(nombre);
            if (!u.isActivo()) {
                throw new AutenticacionException("El usuario esta inactivo. Consulte al administrador.");
            }
            usuarioDAO.registrarAcceso(u.getId());
            Sesion.getInstancia().iniciar(u);
            auditoria.registrar("usuario", u.getId(), "LOGIN", "Inicio de sesion");
            return u;
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    public void cerrarSesion() throws PersistenciaException {
        Sesion s = Sesion.getInstancia();
        if (!s.activa()) return;
        try {
            auditoria.registrar("usuario", s.getIdUsuario(), "LOGOUT", "Cierre de sesion");
        } finally {
            s.cerrar();
        }
    }
}
