package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;

/** Contrato de persistencia de consultas y de la historia clinica. */
public interface ConsultaDAO {

    /**
     * Registra y CIERRA la consulta con sus diagnosticos y medicacion en una
     * unica transaccion (RNF-10). Si viene de la sala de espera, finaliza el
     * ingreso en la misma transaccion. Deja registro de auditoria.
     *
     * @return id generado de la consulta
     */
    int registrar(Consulta c, Integer idIngresoSala, Integer idUsuario) throws PersistenciaException;

    /** Historia clinica del paciente, de la consulta mas reciente a la mas antigua. */
    List<Consulta> listarPorPaciente(Paciente p) throws PersistenciaException;

    /** Modifica el motivo de una consulta en BORRADOR. */
    void actualizarMotivo(int idConsulta, String motivo) throws PersistenciaException;
}
