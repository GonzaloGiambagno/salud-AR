package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.EstadoIngreso;
import saludar.modelo.IngresoSala;

/** Contrato de persistencia de la sala de espera. */
public interface IngresoSalaDAO {
    int insertar(IngresoSala ingreso, Integer idUsuario) throws PersistenciaException;

    /** Ingresos EN_ESPERA o EN_ATENCION, con los datos del paciente. */
    List<IngresoSala> listarActivos() throws PersistenciaException;

    void actualizarEstado(int idIngreso, EstadoIngreso estado) throws PersistenciaException;
}
