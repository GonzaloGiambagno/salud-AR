package saludar.persistencia.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Cie10;
import saludar.persistencia.Cie10DAO;

/** Implementacion MySQL del catalogo CIE-10. */
public class Cie10DAOMySQL extends DAOBaseMySQL<Cie10> implements Cie10DAO {

    private static final String SELECT_BASE = "SELECT codigo_cie10, descripcion, capitulo FROM cie10 ";

    @Override
    protected Cie10 mapear(ResultSet rs) throws SQLException {
        return new Cie10(rs.getString("codigo_cie10"), rs.getString("descripcion"), rs.getString("capitulo"));
    }

    @Override
    public List<Cie10> listar() throws PersistenciaException {
        return consultarLista(SELECT_BASE + "ORDER BY codigo_cie10");
    }

    @Override
    public List<Cie10> buscar(String criterio) throws PersistenciaException {
        String c = criterio == null ? "" : criterio.trim();
        return consultarLista(SELECT_BASE + "WHERE codigo_cie10 LIKE ? OR descripcion LIKE ? ORDER BY codigo_cie10",
                c + "%", "%" + c + "%");
    }
}
