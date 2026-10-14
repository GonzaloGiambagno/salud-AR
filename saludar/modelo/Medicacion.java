package saludar.modelo;

/** Medicacion indicada en una consulta. */
public class Medicacion {
    private String farmaco;
    private String dosis;
    private String frecuencia;
    private String duracion;

    public Medicacion(String farmaco, String dosis, String frecuencia, String duracion) {
        this.farmaco = farmaco;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracion = duracion;
    }

    public String getFarmaco() { return farmaco; }
    public String getDosis() { return dosis; }
    public String getFrecuencia() { return frecuencia; }
    public String getDuracion() { return duracion; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(farmaco);
        if (dosis != null && !dosis.isBlank()) sb.append(" ").append(dosis);
        if (frecuencia != null && !frecuencia.isBlank()) sb.append(" - ").append(frecuencia);
        if (duracion != null && !duracion.isBlank()) sb.append(" x ").append(duracion);
        return sb.toString();
    }
}
