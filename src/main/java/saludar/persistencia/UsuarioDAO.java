package saludar.persistencia;

import saludar.excepciones.PersistenciaException;
import saludar.modelo.Usuario;

/** Contrato de acceso a las cuentas de usuario (CU-01). */
public interface UsuarioDAO {

    /** Usuario con su profesional asociado (si es medico); null si no existe. */
    Usuario buscarPorNombre(String nombreUsuario) throws PersistenciaException;

    /** Hash almacenado de la contrasenia; null si el usuario no existe. */
    String buscarHash(String nombreUsuario) throws PersistenciaException;

    void registrarAcceso(int idUsuario) throws PersistenciaException;
}
