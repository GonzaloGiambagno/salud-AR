package saludar.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Paciente del sistema. HERENCIA: extiende Persona.
 * Mantiene sus alergias en un ArrayList (composicion) y aplica
 * ENCAPSULAMIENTO con validacion del DNI y del sexo.
 */
public class Paciente extends Persona {

    private int id;
    private String dni;
    private char sexo;
    private String domicilio;
    private String telefono;
    private String email;
    private ObraSocial obraSocial;       // null = paciente particular
    private String nroAfiliado;
    private String plan;
    private boolean activo;
    private final List<Alergia> alergias = new ArrayList<>();

    // SOBRECARGA DE CONSTRUCTORES -------------------------------------------
    // Constructor completo (lo usa la capa de persistencia al leer de MySQL).
    public Paciente(int id, String dni, String apellido, String nombre,
                    LocalDate fechaNacimiento, char sexo) {
        super(apellido, nombre, fechaNacimiento);   // llamada al constructor de Persona
        setDni(dni);
        setSexo(sexo);
        this.id = id;
        this.activo = true;
    }

    // Constructor para un paciente nuevo: delega en el completo con THIS(...).
    public Paciente(String dni, String apellido, String nombre, LocalDate fechaNacimiento, char sexo) {
        this(0, dni, apellido, nombre, fechaNacimiento, sexo);
    }

    // Encapsulamiento + validacion del DNI
    public final void setDni(String dni) {
        if (dni == null || !dni.matches("\\d{7,8}")) {
            throw new IllegalArgumentException(
                "DNI invalido: debe contener 7 u 8 digitos (recibido: " + dni + ").");
        }
        this.dni = dni;
    }

    public final void setSexo(char sexo) {
        if (sexo != 'F' && sexo != 'M' && sexo != 'X') {
            throw new IllegalArgumentException("Sexo invalido: debe ser F, M o X.");
        }
        this.sexo = sexo;
    }

    public void setContacto(String domicilio, String telefono, String email) {
        this.domicilio = domicilio;
        this.telefono = telefono;
        this.email = email;
    }

    public void setCobertura(ObraSocial obraSocial, String nroAfiliado, String plan) {
        this.obraSocial = obraSocial;
        this.nroAfiliado = obraSocial == null ? null : nroAfiliado;
        this.plan = obraSocial == null ? null : plan;
    }

    public String getDni() { return dni; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public char getSexo() { return sexo; }
    public String getDomicilio() { return domicilio; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public ObraSocial getObraSocial() { return obraSocial; }
    public String getNroAfiliado() { return nroAfiliado; }
    public String getPlan() { return plan; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Nombre de la cobertura para mostrar: la obra social o "Particular". */
    public String getNombreCobertura() {
        return obraSocial == null ? "Particular" : obraSocial.getNombre();
    }

    public void agregarAlergia(Alergia a) { alergias.add(a); }
    public void setAlergias(List<Alergia> nuevas) { alergias.clear(); alergias.addAll(nuevas); }
    public List<Alergia> getAlergias() { return alergias; }
    public boolean tieneAlergias() { return !alergias.isEmpty(); }

    // POLIMORFISMO: implementacion concreta del metodo abstracto.
    @Override
    public String getResumen() {
        String edad = getEdad() >= 0 ? getEdad() + " anios" : "edad s/d";
        return String.format("Paciente #%d  %-22s  DNI %-9s  %s  [%s]",
                id, getNombreCompleto(), dni, edad, getNombreCobertura());
    }
}
