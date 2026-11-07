package saludar.servicio;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.RegistroAuditoria;
import saludar.persistencia.AuditoriaDAO;
import saludar.seguridad.Sesion;

/** Registro de auditoria de las operaciones sensibles (RF-23): quien, que y cuando. */
public class AuditoriaService {

    private final AuditoriaDAO dao;

    public AuditoriaService(AuditoriaDAO dao) { this.dao = dao; }

    /** Registra el evento a nombre del usuario en sesion. */
    public void registrar(String entidad, Integer idRegistro, String accion, String detalle)
            throws PersistenciaException {
        dao.registrar(Sesion.getInstancia().getIdUsuario(), entidad, idRegistro, accion, detalle);
    }

    public List<RegistroAuditoria> ultimos(int cantidad) throws PersistenciaException {
        return dao.ultimos(cantidad);
    }
}
