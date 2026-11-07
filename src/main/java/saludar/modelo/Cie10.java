package saludar.modelo;

/** Codigo del catalogo CIE-10 (tabla cie10). */
public class Cie10 {
    private final String codigo;
    private final String descripcion;
    private final String capitulo;

    public Cie10(String codigo, String descripcion, String capitulo) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.capitulo = capitulo;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public String getCapitulo() { return capitulo; }

    @Override public String toString() { return codigo + " - " + descripcion; }
}
