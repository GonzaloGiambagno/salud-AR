package saludar.modelo;

/**
 * Cuenta de acceso al sistema. No hereda de Persona: representa una
 * credencial con un rol, no a una persona del dominio clinico. Si el rol es
 * MEDICO, referencia al Profesional que firma las consultas.
 */
public class Usuario {
    private final int id;
    private final String nombreUsuario;
    private final Rol rol;
    private final boolean activo;
    private final Profesional profesional;   // null si no es medico

    public Usuario(int id, String nombreUsuario, Rol rol, boolean activo, Profesional profesional) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.activo = activo;
        this.profesional = profesional;
    }

    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public Profesional getProfesional() { return profesional; }

    /** true si el rol del usuario es alguno de los indicados (parametro variable = ARREGLO). */
    public boolean tieneRol(Rol... roles) {
        for (Rol r : roles) {
            if (r == rol) return true;
        }
        return false;
    }

    @Override public String toString() { return nombreUsuario + " (" + rol + ")"; }
}
