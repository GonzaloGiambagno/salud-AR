package saludar.app;

import javax.swing.SwingUtilities;
import saludar.ui.Estilo;
import saludar.ui.VentanaPrincipal;

/** Punto de entrada de la version con interfaz grafica Swing. */
public class MainSwing {
    public static void main(String[] args) {
        Estilo.aplicar();
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
