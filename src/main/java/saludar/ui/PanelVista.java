package saludar.ui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import saludar.controlador.Vista;

/**
 * Clase ABSTRACTA base de las pestanias de la ventana principal. Resuelve una
 * sola vez el aspecto comun y los mensajes al usuario que exige el contrato
 * Vista; cada pestania concreta define su contenido y como se refresca.
 */
public abstract class PanelVista extends JPanel implements Vista, Refrescable {

    protected PanelVista() {
        super(new BorderLayout(12, 12));
        setBackground(Estilo.PANEL);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
    }

    @Override public void mostrarInfo(String titulo, String mensaje) { Mensajes.info(this, titulo, mensaje); }
    @Override public void mostrarAviso(String titulo, String mensaje) { Mensajes.aviso(this, titulo, mensaje); }
    @Override public void mostrarError(String titulo, String mensaje) { Mensajes.error(this, titulo, mensaje); }
    @Override public boolean confirmar(String titulo, String pregunta) { return Mensajes.confirmar(this, titulo, pregunta); }
}
