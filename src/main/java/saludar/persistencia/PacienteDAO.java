package saludar.persistencia;

import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Paciente;

/**
 * Contrato de persistencia de pacientes (patron DAO). Los servicios dependen
 * de esta INTERFAZ y no de una tecnologia concreta (hoy, PacienteDAOMySQL):
 * cambiar de motor no obliga a modificar la logica de negocio.
 */
public interface PacienteDAO {

    /** Inserta el paciente y su cobertura; devuelve el id generado. */
    int insertar(Paciente p) throws PersistenciaException;

    /** Actualiza datos personales y de cobertura (RF-09). */
    void actualizar(Paciente p) throws PersistenciaException;

    /** Baja logica: el paciente deja de listarse pero su HC se conserva (RF-10). */
    void darDeBaja(int idPaciente) throws PersistenciaException;

    Paciente buscarPorId(int idPaciente) throws PersistenciaException;

    /** Busca por DNI entre todos los pacientes (activos o no); null si no existe. */
    Paciente buscarPorDni(String dni) throws PersistenciaException;

    boolean existeDni(String dni) throws PersistenciaException;

    List<Paciente> listarActivos() throws PersistenciaException;

    /** Pacientes activos cuyo apellido comienza con el texto indicado (RF-11). */
    List<Paciente> buscarPorApellido(String prefijo) throws PersistenciaException;
}
