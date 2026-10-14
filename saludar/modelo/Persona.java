package saludar.modelo;

import java.time.LocalDate;
import java.time.Period;

/**
 * Clase ABSTRACTA que modela a una persona del dominio.
 * Aplica ABSTRACCION (no tiene sentido instanciar "una persona" a secas:
 * siempre es un Paciente o un Profesional) y ENCAPSULAMIENTO (atributos
 * privados accesibles solo mediante getters/setters con validacion).
 */
public abstract class Persona {

    private String apellido;
    private String nombre;
    private LocalDate fechaNacimiento;

    protected Persona(String apellido, String nombre, LocalDate fechaNacimiento) {
        setApellido(apellido);
        setNombre(nombre);
        this.fechaNacimiento = fechaNacimiento;
    }

    /** Metodo ABSTRACTO: cada subclase describe como se resume a si misma. */
    public abstract String getResumen();

    public String getApellido() { return apellido; }

    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        this.apellido = apellido.trim();
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getEdad() {
        if (fechaNacimiento == null) return -1;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    // POLIMORFISMO: toString delega en getResumen() de la subclase concreta.
    @Override
    public String toString() {
        return getResumen();
    }
}
