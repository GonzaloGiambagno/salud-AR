package saludar.persistencia.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.ObraSocial;
import saludar.persistencia.ObraSocialDAO;

/** Implementacion MySQL del padron de obras sociales. */
public class ObraSocialDAOMySQL extends DAOBaseMySQL<ObraSocial> implements ObraSocialDAO {

    @Override
    protected ObraSocial mapear(ResultSet rs) throws SQLException {
        return new ObraSocial(rs.getInt("id_obra_social"), rs.getString("nombre"), rs.getString("codigo"));
    }

    @Override
    public List<ObraSocial> listar() throws PersistenciaException {
        return consultarLista("SELECT id_obra_social, nombre, codigo FROM obra_social ORDER BY nombre");
    }
}
