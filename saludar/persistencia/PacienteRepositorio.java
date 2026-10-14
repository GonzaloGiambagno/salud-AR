package saludar.persistencia;

import java.util.List;
import saludar.modelo.Paciente;

/**
 * ABSTRACCION de la persistencia (patron DAO/Repository).
 * El servicio depende de esta INTERFAZ, no de una implementacion concreta,
 * lo que habilita el POLIMORFISMO: en la demo se usa la version en memoria
 * y en produccion la version MySQL, sin cambiar la logica de negocio.
 */
public interface PacienteRepositorio {
    Paciente guardar(Paciente p);
    List<Paciente> listar();
    Paciente buscarPorDni(String dni);
    boolean existeDni(String dni);
    int cantidad();
}
