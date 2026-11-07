package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.ObraSocial;

/** Contrato de acceso al padron de obras sociales (catalogo de solo lectura). */
public interface ObraSocialDAO {
    List<ObraSocial> listar() throws PersistenciaException;
}
