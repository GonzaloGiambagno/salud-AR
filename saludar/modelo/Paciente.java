package saludar.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Paciente del sistema. HERENCIA: extiende Persona.
 * Mantiene la lista de sus alergias (composicion) y aplica ENCAPSULAMIENTO
 * con validacion del DNI.
 */
public class Paciente extends Persona {

    private int id;
    private String dni;
    private char sexo;
    private String obraSocial;
    private String nroAfiliado;
    private boolean activo;
    private final List<Alergia> alergias = new ArrayList<>();

    // SOBRECARGA DE CONSTRUCTORES -------------------------------------------
    // Constructor completo.
    public Paciente(int id, String dni, String apellido, String nombre,
                    LocalDate fechaNacimiento, char sexo,
                    String obraSocial, String nroAfiliado) {
        super(apellido, nombre, fechaNacimiento);   // llamada al constructor de Persona
        setDni(dni);
        this.id = id;
        this.sexo = sexo;
        this.obraSocial = obraSocial;
        this.nroAfiliado = nroAfiliado;
        this.activo = true;
    }

    // Constructor reducido: delega en el completo con THIS(...).
    public Paciente(String dni, String apellido, String nombre) {
        this(0, dni, apellido, nombre, null, '-', "Particular", "-");
    }

    // Encapsulamiento + validacion del DNI
    public final void setDni(String dni) {
        if (dni == null || !dni.matches("\\d{7,8}")) {
            throw new IllegalArgumentException(
                "DNI invalido: debe contener 7 u 8 digitos (recibido: " + dni + ").");
        }
        this.dni = dni;
    }

    public String getDni() { return dni; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public char getSexo() { return sexo; }
    public void setSexo(char sexo) { this.sexo = sexo; }
    public String getObraSocial() { return obraSocial; }
    public void setObraSocial(String o) { this.obraSocial = o; }
    public String getNroAfiliado() { return nroAfiliado; }
    public void setNroAfiliado(String n) { this.nroAfiliado = n; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public void agregarAlergia(Alergia a) { alergias.add(a); }
    public List<Alergia> getAlergias() { return alergias; }
    public boolean tieneAlergias() { return !alergias.isEmpty(); }

    // POLIMORFISMO: implementacion concreta del metodo abstracto.
    @Override
    public String getResumen() {
        String edad = getEdad() >= 0 ? getEdad() + " anios" : "edad s/d";
        return String.format("Paciente #%d  %-22s  DNI %-9s  %s  [%s]",
                id, getNombreCompleto(), dni, edad, obraSocial);
    }
}
