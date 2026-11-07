package saludar.servicio;

import java.util.List;
import saludar.excepciones.ConsultaCerradaException;
import saludar.excepciones.OperacionNoPermitidaException;
import saludar.excepciones.PersistenciaException;
import saludar.excepciones.ValidacionException;
import saludar.modelo.Consulta;
import saludar.modelo.Diagnostico;
import saludar.modelo.IngresoSala;
import saludar.modelo.Paciente;
import saludar.modelo.Rol;
import saludar.persistencia.ConsultaDAO;
import saludar.seguridad.Sesion;

/** Logica de negocio de consultas y de la historia clinica (CU-04, CU-07). */
public class ConsultaService {

    private final ConsultaDAO dao;

    public ConsultaService(ConsultaDAO dao) {
        this.dao = dao;
    }

    /**
     * Crea una consulta en BORRADOR firmada por el profesional en sesion.
     * Solo un MEDICO puede registrar evoluciones (RF-03).
     */
    public Consulta nuevaConsulta(Paciente p) throws OperacionNoPermitidaException {
        Sesion s = Sesion.getInstancia();
        s.exigirRol("registrar consultas", Rol.MEDICO);
        if (s.getUsuario().getProfesional() == null) {
            throw new OperacionNoPermitidaException("El usuario no tiene un profesional asociado.");
        }
        return new Consulta(p, s.getUsuario().getProfesional(), "");
    }

    /**
     * Valida y guarda la consulta CERRADA (inviolable desde ese momento).
     *
     * @param origen ingreso a la sala de espera que se finaliza, o null
     */
    public void registrarYCerrar(Consulta c, IngresoSala origen)
            throws ValidacionException, PersistenciaException {
        if (c.getMotivo() == null || c.getMotivo().isBlank()) {
            throw new ValidacionException("El motivo de la consulta es obligatorio.");
        }
        int principales = 0;
        for (Diagnostico d : c.getDiagnosticos()) {
            if (d.isPrincipal()) principales++;
        }
        if (principales != 1) {
            throw new ValidacionException("La consulta debe tener exactamente un diagnostico principal (RF-14).");
        }
        dao.registrar(c, origen == null ? null : origen.getId(), Sesion.getInstancia().getIdUsuario());
        if (origen != null) origen.finalizar();
    }

    /** Historia clinica del paciente, de la mas reciente a la mas antigua (RF-21). */
    public List<Consulta> historiaDe(Paciente p) throws PersistenciaException {
        return dao.listarPorPaciente(p);
    }

    /** Edicion que respeta la inviolabilidad: una consulta cerrada no se modifica (RF-17, RN-05). */
    public void editarMotivo(Consulta c, String nuevoMotivo)
            throws ConsultaCerradaException, PersistenciaException {
        if (c.estaCerrada()) {
            throw new ConsultaCerradaException(c.getId());
        }
        dao.actualizarMotivo(c.getId(), nuevoMotivo);
        c.setMotivo(nuevoMotivo);
    }
}
