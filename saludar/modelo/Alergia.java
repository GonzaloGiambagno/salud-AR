package saludar.modelo;

/** Alergia de un paciente. */
public class Alergia {
    private String sustancia;
    private String tipoReaccion;
    private Severidad severidad;

    public Alergia(String sustancia, String tipoReaccion, Severidad severidad) {
        this.sustancia = sustancia;
        this.tipoReaccion = tipoReaccion;
        this.severidad = severidad;
    }

    public String getSustancia() { return sustancia; }
    public String getTipoReaccion() { return tipoReaccion; }
    public Severidad getSeveridad() { return severidad; }

    @Override
    public String toString() {
        return sustancia + " (" + severidad + ")";
    }
}
