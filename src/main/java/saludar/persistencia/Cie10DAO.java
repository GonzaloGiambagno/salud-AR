package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Cie10;

/** Contrato de acceso al catalogo CIE-10 (RF-15). */
public interface Cie10DAO {
    List<Cie10> listar() throws PersistenciaException;

    /** Busca por codigo o por parte de la descripcion. */
    List<Cie10> buscar(String criterio) throws PersistenciaException;
}
