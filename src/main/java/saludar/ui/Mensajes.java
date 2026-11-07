package saludar.ui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import javax.swing.JOptionPane;

/**
 * Cuadros de dialogo comunes. Se muestran sobre la ventana activa (por
 * ejemplo, sobre el dialogo de la consulta si esta abierto) y, si no hay
 * ninguna, sobre el componente indicado.
 */
public final class Mensajes {
    private Mensajes() { }

    private static Component padre(Component porDefecto) {
        Window activa = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return activa != null ? activa : porDefecto;
    }

    public static void info(Component c, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre(c), mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Error de negocio o de validacion: el usuario puede corregirlo. */
    public static void aviso(Component c, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre(c), mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    /** Error del sistema (base de datos, archivos): se muestra el mensaje traducido, no la traza. */
    public static void error(Component c, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre(c), mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component c, String titulo, String pregunta) {
        return JOptionPane.showConfirmDialog(padre(c), pregunta, titulo, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
