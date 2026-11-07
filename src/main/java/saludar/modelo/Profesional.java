package saludar.modelo;

import java.time.LocalDate;

/** Profesional de salud. HERENCIA: extiende Persona. */
public class Profesional extends Persona {

    private final int id;
    private final String matricula;
    private final String especialidad;

    public Profesional(int id, String apellido, String nombre,
                       String matricula, String especialidad) {
        super(apellido, nombre, (LocalDate) null);
        this.id = id;
        this.matricula = matricula;
        this.especialidad = especialidad;
    }

    public int getId() { return id; }
    public String getMatricula() { return matricula; }
    public String getEspecialidad() { return especialidad; }

    // POLIMORFISMO: otra implementacion del mismo metodo abstracto.
    @Override
    public String getResumen() {
        return String.format("Dr/a. %s  (%s - %s)",
                getNombreCompleto(), matricula, especialidad);
    }
}
