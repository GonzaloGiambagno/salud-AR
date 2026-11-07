package saludar.modelo;

/** Alergia de un paciente. */
public class Alergia {
    private int id;
    private final String sustancia;
    private final String tipoReaccion;
    private final Severidad severidad;

    public Alergia(String sustancia, String tipoReaccion, Severidad severidad) {
        this(0, sustancia, tipoReaccion, severidad);
    }

    public Alergia(int id, String sustancia, String tipoReaccion, Severidad severidad) {
        this.id = id;
        this.sustancia = sustancia;
        this.tipoReaccion = tipoReaccion;
        this.severidad = severidad;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSustancia() { return sustancia; }
    public String getTipoReaccion() { return tipoReaccion; }
    public Severidad getSeveridad() { return severidad; }

    @Override
    public String toString() {
        return sustancia + " (" + severidad + ")";
    }
}
