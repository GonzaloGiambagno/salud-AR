package saludar.persistencia;

import java.util.ArrayList;
import java.util.List;
import saludar.modelo.Paciente;

/**
 * Implementacion en memoria (LISTA ENLAZADA / ArrayList) del repositorio.
 * Se usa en el prototipo para que el programa compile y se ejecute sin
 * requerir un servidor MySQL en linea.
 */
public class PacienteRepositorioMemoria implements PacienteRepositorio {

    private final List<Paciente> pacientes = new ArrayList<>();
    private int secuenciaId = 0;

    @Override
    public Paciente guardar(Paciente p) {
        if (p.getId() == 0) {
            p.setId(++secuenciaId);
        }
        pacientes.add(p);
        return p;
    }

    @Override
    public List<Paciente> listar() {
        return new ArrayList<>(pacientes);   // copia defensiva (encapsulamiento)
    }

    @Override
    public Paciente buscarPorDni(String dni) {
        for (Paciente p : pacientes) {        // busqueda lineal
            if (p.getDni().equals(dni)) return p;
        }
        return null;
    }

    @Override
    public boolean existeDni(String dni) {
        return buscarPorDni(dni) != null;
    }

    @Override
    public int cantidad() {
        return pacientes.size();
    }
}
