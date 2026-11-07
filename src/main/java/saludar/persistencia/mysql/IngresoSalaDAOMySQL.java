package saludar.persistencia.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.EstadoIngreso;
import saludar.modelo.IngresoSala;
import saludar.modelo.Prioridad;
import saludar.persistencia.IngresoSalaDAO;

/** Implementacion MySQL de la sala de espera (tabla ingreso_sala). */
public class IngresoSalaDAOMySQL extends DAOBaseMySQL<IngresoSala> implements IngresoSalaDAO {

    @Override
    protected IngresoSala mapear(ResultSet rs) throws SQLException {
        return new IngresoSala(rs.getInt("id_ingreso"), PacienteDAOMySQL.mapearPaciente(rs),
                Prioridad.valueOf(rs.getString("prioridad")),
                rs.getObject("fecha_hora_ingreso", LocalDateTime.class),
                EstadoIngreso.valueOf(rs.getString("estado")));
    }

    @Override
    public int insertar(IngresoSala ingreso, Integer idUsuario) throws PersistenciaException {
        int id = insertarRegistro("INSERT INTO ingreso_sala (id_paciente, prioridad, estado, "
                + "fecha_hora_ingreso, id_usuario_registro) VALUES (?, ?, ?, ?, ?)",
                ingreso.getPaciente().getId(), ingreso.getPrioridad(), ingreso.getEstado(),
                ingreso.getHoraIngreso(), idUsuario);
        ingreso.setId(id);
        return id;
    }

    @Override
    public List<IngresoSala> listarActivos() throws PersistenciaException {
        // El orden de atencion (cola con prioridad) lo resuelve la capa de servicio
        return consultarLista("SELECT i.id_ingreso, i.prioridad, i.estado, i.fecha_hora_ingreso, v.* "
                + "FROM ingreso_sala i JOIN v_paciente v ON v.id_paciente = i.id_paciente "
                + "WHERE i.estado IN ('EN_ESPERA', 'EN_ATENCION') ORDER BY i.id_ingreso");
    }

    @Override
    public void actualizarEstado(int idIngreso, EstadoIngreso estado) throws PersistenciaException {
        int filas = ejecutarActualizacion("UPDATE ingreso_sala SET estado = ? "
                + "WHERE id_ingreso = ? AND estado <> 'FINALIZADO'", estado, idIngreso);
        if (filas == 0) {
            throw new PersistenciaException("El ingreso #" + idIngreso + " ya no esta activo en la sala.");
        }
    }
}
