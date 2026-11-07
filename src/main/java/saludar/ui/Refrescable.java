package saludar.ui;

/**
 * Pestania que vuelve a leer sus datos de la base cuando se la selecciona.
 * VentanaPrincipal trata a todas las pestanias por esta INTERFAZ
 * (polimorfismo), sin conocer la clase concreta de cada panel.
 */
public interface Refrescable {
    void refrescar();
}
