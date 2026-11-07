package saludar.persistencia.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Alergia;
import saludar.modelo.Severidad;
import saludar.persistencia.AlergiaDAO;

/** Implementacion MySQL del DAO de alergias. */
public class AlergiaDAOMySQL extends DAOBaseMySQL<Alergia> implements AlergiaDAO {

    @Override
    protected Alergia mapear(ResultSet rs) throws SQLException {
        return new Alergia(rs.getInt("id_alergia"), rs.getString("sustancia"),
                rs.getString("tipo_reaccion"), Severidad.valueOf(rs.getString("severidad")));
    }

    @Override
    public List<Alergia> listarPorPaciente(int idPaciente) throws PersistenciaException {
        // Las mas graves primero: es lo que el medico debe ver antes (RN-04)
        return consultarLista("SELECT id_alergia, sustancia, tipo_reaccion, severidad FROM alergia "
                + "WHERE id_paciente = ? ORDER BY FIELD(severidad, 'GRAVE', 'MODERADA', 'LEVE'), sustancia",
                idPaciente);
    }

    @Override
    public int insertar(int idPaciente, Alergia a) throws PersistenciaException {
        int id = insertarRegistro("INSERT INTO alergia (id_paciente, sustancia, tipo_reaccion, severidad) "
                + "VALUES (?, ?, ?, ?)", idPaciente, a.getSustancia(), a.getTipoReaccion(), a.getSeveridad());
        a.setId(id);
        return id;
    }
}
