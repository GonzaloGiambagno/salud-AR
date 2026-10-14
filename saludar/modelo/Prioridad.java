package saludar.modelo;

/**
 * Prioridad de atencion asignada por TRIAGE al ingresar a la sala de espera.
 * El orden de declaracion define el orden de atencion (ALTA primero).
 */
public enum Prioridad {
    ALTA("Rojo", "Alta"),
    MEDIA("Amarillo", "Media"),
    BAJA("Verde", "Baja");

    private final String color;
    private final String etiqueta;

    Prioridad(String color, String etiqueta) {
        this.color = color;
        this.etiqueta = etiqueta;
    }

    public String getColor() { return color; }
    public String getEtiqueta() { return etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
