package saludar.persistencia.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Profesional;
import saludar.modelo.Rol;
import saludar.modelo.Usuario;
import saludar.persistencia.UsuarioDAO;

/** Implementacion MySQL del DAO de usuarios. */
public class UsuarioDAOMySQL extends DAOBaseMySQL<Usuario> implements UsuarioDAO {

    @Override
    protected Usuario mapear(ResultSet rs) throws SQLException {
        Integer idProfesional = enteroONulo(rs, "id_profesional");
        Profesional prof = idProfesional == null ? null : new Profesional(idProfesional,
                rs.getString("apellido"), rs.getString("nombre"),
                rs.getString("matricula"), rs.getString("especialidad"));
        return new Usuario(rs.getInt("id_usuario"), rs.getString("nombre_usuario"),
                Rol.valueOf(rs.getString("rol")), rs.getBoolean("activo"), prof);
    }

    @Override
    public Usuario buscarPorNombre(String nombreUsuario) throws PersistenciaException {
        return consultarUno("SELECT u.id_usuario, u.nombre_usuario, u.rol, u.activo, "
                + "p.id_profesional, p.apellido, p.nombre, p.matricula, p.especialidad "
                + "FROM usuario u LEFT JOIN profesional p ON p.id_usuario = u.id_usuario "
                + "WHERE u.nombre_usuario = ?", nombreUsuario);
    }

    @Override
    public String buscarHash(String nombreUsuario) throws PersistenciaException {
        return consultarValor("SELECT hash_contrasena FROM usuario WHERE nombre_usuario = ?",
                String.class, nombreUsuario);
    }

    @Override
    public void registrarAcceso(int idUsuario) throws PersistenciaException {
        ejecutarActualizacion("UPDATE usuario SET ultimo_acceso = NOW() WHERE id_usuario = ?", idUsuario);
    }
}
