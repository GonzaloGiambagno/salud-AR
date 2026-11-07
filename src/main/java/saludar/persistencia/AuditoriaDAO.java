package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.RegistroAuditoria;

/** Contrato del registro de auditoria (RF-23, RNF-04). */
public interface AuditoriaDAO {
    void registrar(Integer idUsuario, String entidad, Integer idRegistro,
                   String accion, String detalle) throws PersistenciaException;

    List<RegistroAuditoria> ultimos(int cantidad) throws PersistenciaException;
}
