package saludar.modelo;

/** Diagnostico codificado segun CIE-10, asociado a una consulta. */
public class Diagnostico {
    private final String codigoCie10;
    private final String descripcion;
    private final boolean principal;

    public Diagnostico(String codigoCie10, String descripcion, boolean principal) {
        this.codigoCie10 = codigoCie10;
        this.descripcion = descripcion;
        this.principal = principal;
    }

    public Diagnostico(Cie10 codigo, boolean principal) {
        this(codigo.getCodigo(), codigo.getDescripcion(), principal);
    }

    public String getCodigoCie10() { return codigoCie10; }
    public String getDescripcion() { return descripcion; }
    public boolean isPrincipal() { return principal; }

    @Override
    public String toString() {
        return codigoCie10 + " " + descripcion + (principal ? " (principal)" : "");
    }
}
