package saludar.servicio;

import java.util.ArrayList;
import java.util.List;
import saludar.excepciones.ConsultaCerradaException;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;
import saludar.modelo.Profesional;

/** Logica de negocio de consultas y de la historia clinica. */
public class ConsultaService {

    private final List<Consulta> consultas = new ArrayList<>();
    private int secuencia = 0;

    public Consulta registrarConsulta(Paciente paciente, Profesional profesional,
                                      String motivo, String evaluacion) {
        Consulta c = new Consulta(++secuencia, paciente, profesional, motivo);
        c.setEvaluacion(evaluacion);
        c.cerrar();                     // la consulta nace y se cierra (firmada)
        consultas.add(c);
        return c;
    }

    /** Crea una consulta en BORRADOR para completarla (diagnostico, medicacion). */
    public Consulta crearBorrador(Paciente paciente, Profesional profesional,
                                 String motivo, String evaluacion) {
        Consulta c = new Consulta(++secuencia, paciente, profesional, motivo);
        c.setEvaluacion(evaluacion);
        consultas.add(c);
        return c;
    }

    /** Cierra (firma) la consulta: a partir de aqui es inviolable. */
    public void cerrar(Consulta c) { c.cerrar(); }

    /** Intento de edicion que respeta la inviolabilidad (RF-17, RN-05). */
    public void editarMotivo(Consulta c, String nuevoMotivo)
            throws ConsultaCerradaException {
        if (c.estaCerrada()) {
            throw new ConsultaCerradaException(c.getId());
        }
        c.setMotivo(nuevoMotivo);
    }

    /** Historia clinica del paciente, de la mas reciente a la mas antigua. */
    public List<Consulta> historiaDe(Paciente p) {
        List<Consulta> hc = new ArrayList<>();
        for (Consulta c : consultas) {
            if (c.getPaciente() == p) hc.add(c);
        }
        for (int i = 0; i < hc.size() / 2; i++) {    // invierte el orden
            Consulta tmp = hc.get(i);
            hc.set(i, hc.get(hc.size() - 1 - i));
            hc.set(hc.size() - 1 - i, tmp);
        }
        return hc;
    }
}
