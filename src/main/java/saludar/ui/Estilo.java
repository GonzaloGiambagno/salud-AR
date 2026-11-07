package saludar.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Paleta, tipografias y helpers de estilo (identidad Salud-AR). */
public final class Estilo {
    private Estilo() { }

    public static final Color DARK   = new Color(0x26, 0x46, 0x53);
    public static final Color TEAL   = new Color(0x2a, 0x9d, 0x8f);
    public static final Color TEALD  = new Color(0x23, 0x87, 0x7b);
    public static final Color LIGHT  = new Color(0xe9, 0xf5, 0xf3);
    public static final Color RED    = new Color(0xc1, 0x44, 0x3c);
    public static final Color REDBG  = new Color(0xfd, 0xec, 0xec);
    public static final Color PANEL  = new Color(0xf6, 0xf8, 0xf9);
    public static final Color ROW    = new Color(0xf2, 0xf5, 0xf6);
    public static final Color BORDER = new Color(0xcf, 0xd8, 0xdc);

    // Triage: color fuerte (bordes/acentos) y fondo muy claro (filas de la tabla)
    public static final Color VERDE      = new Color(0x2e, 0x7d, 0x32);
    public static final Color VERDEBG    = new Color(0xe8, 0xf5, 0xe9);
    public static final Color AMARILLO   = new Color(0xc7, 0x9a, 0x00);
    public static final Color AMARILLOBG = new Color(0xff, 0xf8, 0xdc);

    public static final Font H1   = new Font("SansSerif", Font.BOLD, 22);
    public static final Font H2   = new Font("SansSerif", Font.BOLD, 14);
    public static final Font H3   = new Font("SansSerif", Font.BOLD, 13);
    public static final Font BASE = new Font("SansSerif", Font.PLAIN, 13);

    /** Aplica Nimbus con acentos de la paleta. */
    public static void aplicar() {
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; }
            }
            UIManager.put("control", PANEL);
            UIManager.put("nimbusBase", DARK);
            UIManager.put("nimbusBlueGrey", new Color(0xd3, 0xdd, 0xe1));
            UIManager.put("nimbusFocus", TEAL);
            UIManager.put("nimbusSelectionBackground", TEAL);
            UIManager.put("Table.alternateRowColor", ROW);
            UIManager.put("defaultFont", BASE);
        } catch (Exception ignore) { }
    }

    public static JButton boton(String texto, boolean primario) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        b.setFont(H3);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        if (primario) { b.setBackground(TEAL); b.setForeground(Color.WHITE); }
        else { b.setBackground(new Color(0xe6, 0xeb, 0xed)); b.setForeground(DARK); }
        return b;
    }

    public static Border card(String titulo) {
        Border line = BorderFactory.createMatteBorder(3, 0, 0, 0, TEAL);
        Border tit = BorderFactory.createTitledBorder(line, "  " + titulo);
        ((javax.swing.border.TitledBorder) tit).setTitleFont(H3);
        ((javax.swing.border.TitledBorder) tit).setTitleColor(DARK);
        return BorderFactory.createCompoundBorder(tit, BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    public static JLabel etiqueta(String t) {
        JLabel l = new JLabel(t);
        l.setFont(H3);
        l.setForeground(DARK);
        return l;
    }

    /** Da formato a una JTable: cabecera oscura, filas altas y alternadas. */
    public static void estilizarTabla(JTable t) {
        t.setRowHeight(26);
        t.setFont(BASE);
        t.setGridColor(BORDER);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new java.awt.Dimension(10, 30));
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setOpaque(true);
        hr.setBackground(DARK);
        hr.setForeground(Color.WHITE);
        hr.setFont(H3);
        hr.setHorizontalAlignment(SwingConstants.LEFT);
        hr.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        h.setDefaultRenderer(hr);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : ROW);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static Color colorTriage(saludar.modelo.Prioridad p) {
        switch (p) {
            case ALTA:  return RED;
            case MEDIA: return AMARILLO;
            default:    return VERDE;
        }
    }

    public static Color fondoTriage(saludar.modelo.Prioridad p) {
        switch (p) {
            case ALTA:  return REDBG;
            case MEDIA: return AMARILLOBG;
            default:    return VERDEBG;
        }
    }

    public static JLabel alerta() {
        JLabel l = new JLabel(" ");
        l.setOpaque(true);
        l.setFont(H2);
        l.setForeground(RED);
        l.setBackground(REDBG);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RED, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        l.setVisible(false);
        return l;
    }
}
