package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Alergia;

/** Contrato de persistencia de las alergias del paciente (RF-18, RF-19). */
public interface AlergiaDAO {
    List<Alergia> listarPorPaciente(int idPaciente) throws PersistenciaException;
    int insertar(int idPaciente, Alergia a) throws PersistenciaException;
}
