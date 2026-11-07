package saludar.servicio;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Cie10;
import saludar.persistencia.Cie10DAO;

/** Catalogo CIE-10 leido de MySQL (RF-15). Se cachea porque no cambia durante la sesion. */
public class CatalogoCie10 {

    private final Cie10DAO dao;
    private List<Cie10> cache;

    public CatalogoCie10(Cie10DAO dao) { this.dao = dao; }

    public List<Cie10> todas() throws PersistenciaException {
        if (cache == null) cache = dao.listar();
        return cache;
    }

    public List<Cie10> buscar(String criterio) throws PersistenciaException {
        return dao.buscar(criterio);
    }
}
