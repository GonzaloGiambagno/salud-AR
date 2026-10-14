package saludar.servicio;

import java.time.LocalDate;
import java.util.List;
import saludar.excepciones.DniDuplicadoException;
import saludar.excepciones.DniInvalidoException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.modelo.Paciente;
import saludar.persistencia.PacienteRepositorio;
import saludar.util.Busqueda;
import saludar.util.Ordenamiento;

/**
 * Logica de negocio de pacientes. Recibe el repositorio por constructor
 * (INYECCION DE DEPENDENCIAS) y opera contra la ABSTRACCION, no contra una
 * implementacion concreta.
 */
public class PacienteService {

    private final PacienteRepositorio repositorio;

    public PacienteService(PacienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Paciente registrar(String dni, String apellido, String nombre)
            throws DniDuplicadoException, DniInvalidoException {
        try {
            if (repositorio.existeDni(dni)) {
                throw new DniDuplicadoException(dni);     // regla RF-07
            }
            Paciente p = new Paciente(dni, apellido, nombre);
            return repositorio.guardar(p);
        } catch (IllegalArgumentException e) {
            // Traduce la validacion de bajo nivel a una excepcion de negocio.
            throw new DniInvalidoException(e.getMessage());
        }
    }

    /**
     * Alta completa: arma el paciente con todos sus datos ANTES de guardarlo,
     * para que la persistencia reciba la fecha de nacimiento (de la que se
     * calcula la edad) y la cobertura.
     */
    public Paciente registrar(String dni, String apellido, String nombre, LocalDate fechaNacimiento,
                              char sexo, String obraSocial, String nroAfiliado)
            throws DniDuplicadoException, DniInvalidoException {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        try {
            if (repositorio.existeDni(dni)) {
                throw new DniDuplicadoException(dni);     // regla RF-07
            }
            Paciente p = new Paciente(0, dni, apellido, nombre, fechaNacimiento,
                    sexo, obraSocial, nroAfiliado);
            return repositorio.guardar(p);
        } catch (IllegalArgumentException e) {
            throw new DniInvalidoException(e.getMessage());
        }
    }

    public void registrar(Paciente p) { repositorio.guardar(p); }

    /** Lista de pacientes ordenada por apellido (algoritmo propio). */
    public List<Paciente> listarOrdenadosPorApellido() {
        List<Paciente> lista = repositorio.listar();
        Ordenamiento.ordenarPorApellido(lista);   // insertion sort
        return lista;
    }

    /** Busqueda binaria por DNI sobre la lista ordenada por DNI. */
    public Paciente buscarPorDni(String dni) throws PacienteNoEncontradoException {
        List<Paciente> lista = repositorio.listar();
        Ordenamiento.ordenarPorDni(lista);
        Paciente p = Busqueda.binariaPorDni(lista, dni);
        if (p == null) {
            throw new PacienteNoEncontradoException("DNI " + dni);
        }
        return p;
    }

    public int cantidad() { return repositorio.cantidad(); }
}
