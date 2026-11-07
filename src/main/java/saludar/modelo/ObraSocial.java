package saludar.modelo;

/** Obra social o prepaga del padron (tabla obra_social). */
public class ObraSocial {
    private final int id;
    private final String nombre;
    private final String codigo;

    public ObraSocial(int id, String nombre, String codigo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }

    @Override public String toString() { return nombre; }

    @Override public boolean equals(Object o) {
        return o instanceof ObraSocial os && os.id == id;
    }

    @Override public int hashCode() { return Integer.hashCode(id); }
}
