package saludar.modelo;

/** Diagnostico codificado segun CIE-10, asociado a una consulta. */
public class Diagnostico {
    private String codigoCie10;
    private String descripcion;
    private boolean principal;

    public Diagnostico(String codigoCie10, String descripcion, boolean principal) {
        this.codigoCie10 = codigoCie10;
        this.descripcion = descripcion;
        this.principal = principal;
    }

    public String getCodigoCie10() { return codigoCie10; }
    public String getDescripcion() { return descripcion; }
    public boolean isPrincipal() { return principal; }

    @Override
    public String toString() {
        return codigoCie10 + " " + descripcion + (principal ? " (principal)" : "");
    }
}
