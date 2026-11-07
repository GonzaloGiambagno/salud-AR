package saludar.persistencia.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.RegistroAuditoria;
import saludar.persistencia.AuditoriaDAO;

/** Implementacion MySQL del registro de auditoria. */
public class AuditoriaDAOMySQL extends DAOBaseMySQL<RegistroAuditoria> implements AuditoriaDAO {

    @Override
    protected RegistroAuditoria mapear(ResultSet rs) throws SQLException {
        return new RegistroAuditoria(rs.getObject("fecha_hora", LocalDateTime.class),
                rs.getString("usuario"), rs.getString("accion"), rs.getString("entidad"),
                enteroONulo(rs, "id_registro"), rs.getString("detalle"));
    }

    @Override
    public void registrar(Integer idUsuario, String entidad, Integer idRegistro,
                          String accion, String detalle) throws PersistenciaException {
        insertarRegistro("INSERT INTO auditoria (id_usuario, entidad, id_registro, accion, detalle) "
                + "VALUES (?, ?, ?, ?, ?)", idUsuario, entidad, idRegistro, accion, detalle);
    }

    @Override
    public List<RegistroAuditoria> ultimos(int cantidad) throws PersistenciaException {
        return consultarLista("SELECT a.fecha_hora, COALESCE(u.nombre_usuario, '(sin sesion)') AS usuario, "
                + "a.accion, a.entidad, a.id_registro, a.detalle "
                + "FROM auditoria a LEFT JOIN usuario u ON u.id_usuario = a.id_usuario "
                + "ORDER BY a.fecha_hora DESC, a.id_auditoria DESC LIMIT ?", cantidad);
    }
}
