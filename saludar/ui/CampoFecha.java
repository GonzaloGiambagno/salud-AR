package saludar.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

/**
 * Selector de fecha en formato DD/MM/AAAA: campo de texto (con las barras
 * automaticas) y un calendario desplegable para elegir el dia.
 * No admite fechas futuras.
 */
public class CampoFecha extends JPanel {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final String[] DIAS = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};

    private final JTextField campo = new JTextField(8);
    private final JComboBox<String> cbMes = new JComboBox<>(MESES);
    private final JComboBox<Integer> cbAnio = new JComboBox<>();
    private final JPanel grilla = new JPanel(new GridLayout(0, 7, 2, 2));
    private final List<Runnable> oyentes = new ArrayList<>();
    private JDialog calendario;
    private boolean cargando;   // evita redibujar mientras se setean los combos

    public CampoFecha() {
        super(new BorderLayout(4, 0));
        setOpaque(false);
        campo.setFont(Estilo.BASE);
        campo.setToolTipText("DD/MM/AAAA");
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroFecha());
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { avisar(); }
            @Override public void removeUpdate(DocumentEvent e) { avisar(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        JButton bCal = new JButton(new IconoCalendario());
        bCal.setFocusable(false);
        bCal.setToolTipText("Elegir fecha");
        bCal.setMargin(new Insets(2, 6, 2, 6));
        bCal.addActionListener(e -> abrirCalendario());

        add(campo, BorderLayout.CENTER);
        add(bCal, BorderLayout.EAST);

        int hoy = LocalDate.now().getYear();
        for (int a = hoy; a >= 1900; a--) cbAnio.addItem(a);
        cbAnio.setMaximumRowCount(12);
        cbAnio.setPrototypeDisplayValue(99999);
        cbMes.setMaximumRowCount(12);
        cbMes.addActionListener(e -> { if (!cargando) dibujarMes(); });
        cbAnio.addActionListener(e -> { if (!cargando) dibujarMes(); });
    }

    /** Fecha cargada, o null si el campo esta vacio. */
    public LocalDate getFecha() {
        String t = campo.getText().trim();
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t, FORMATO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha invalida: use el formato DD/MM/AAAA.");
        }
    }

    public void setFecha(LocalDate f) { campo.setText(f == null ? "" : FORMATO.format(f)); }

    /** Registra una accion a ejecutar cada vez que cambia la fecha. */
    public void alCambiar(Runnable r) { oyentes.add(r); }

    private void avisar() { for (Runnable r : oyentes) r.run(); }

    // ---- Calendario desplegable ----

    private void abrirCalendario() {
        if (calendario == null) armarCalendario();
        LocalDate base;
        try { base = getFecha(); } catch (IllegalArgumentException e) { base = null; }
        if (base == null || base.isAfter(LocalDate.now())) base = LocalDate.now();
        mostrarMes(YearMonth.from(base));
        calendario.pack();
        Point p = getLocationOnScreen();
        calendario.setLocation(p.x, p.y + getHeight() + 2);
        calendario.setVisible(true);
    }

    private void armarCalendario() {
        calendario = new JDialog(SwingUtilities.getWindowAncestor(this));
        calendario.setUndecorated(true);
        calendario.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) { calendario.setVisible(false); }
        });

        JButton ant = flecha("<", -1);
        JButton sig = flecha(">", 1);
        JPanel cab = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        cab.setOpaque(false);
        cab.add(ant); cab.add(cbMes); cab.add(cbAnio); cab.add(sig);

        grilla.setOpaque(false);
        JPanel cont = new JPanel(new BorderLayout(6, 6));
        cont.setBackground(Color.WHITE);
        cont.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.TEAL, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        cont.add(cab, BorderLayout.NORTH);
        cont.add(grilla, BorderLayout.CENTER);
        calendario.setContentPane(cont);
    }

    private JButton flecha(String txt, int delta) {
        JButton b = new JButton(txt);
        b.setFocusable(false);
        b.setMargin(new Insets(2, 8, 2, 8));
        b.addActionListener(e -> {
            YearMonth ym = mesVisible().plusMonths(delta);
            if (!ym.isAfter(YearMonth.now()) && ym.getYear() >= 1900) mostrarMes(ym);
        });
        return b;
    }

    private YearMonth mesVisible() {
        return YearMonth.of((Integer) cbAnio.getSelectedItem(), cbMes.getSelectedIndex() + 1);
    }

    private void mostrarMes(YearMonth ym) {
        cargando = true;
        cbAnio.setSelectedItem(ym.getYear());
        cbMes.setSelectedIndex(ym.getMonthValue() - 1);
        cargando = false;
        dibujarMes();
    }

    private void dibujarMes() {
        YearMonth ym = mesVisible();
        LocalDate hoy = LocalDate.now();
        LocalDate elegida;
        try { elegida = getFecha(); } catch (IllegalArgumentException e) { elegida = null; }

        grilla.removeAll();
        for (String d : DIAS) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(Estilo.H3);
            l.setForeground(Estilo.DARK);
            grilla.add(l);
        }
        int blancos = ym.atDay(1).getDayOfWeek().getValue() - 1;   // lunes = 0
        for (int i = 0; i < blancos; i++) grilla.add(new JLabel());
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            grilla.add(celdaDia(ym.atDay(d), hoy, elegida));
        }
        // siempre 6 semanas, para que el calendario no cambie de tamanio
        for (int i = blancos + ym.lengthOfMonth(); i < 42; i++) grilla.add(new JLabel());
        grilla.revalidate();
        grilla.repaint();
    }

    private JLabel celdaDia(LocalDate fecha, LocalDate hoy, LocalDate elegida) {
        JLabel l = new JLabel(String.valueOf(fecha.getDayOfMonth()), SwingConstants.CENTER);
        l.setOpaque(true);
        l.setFont(Estilo.BASE);
        l.setPreferredSize(new Dimension(32, 26));
        boolean futura = fecha.isAfter(hoy);
        boolean sel = fecha.equals(elegida);
        Color fondo = sel ? Estilo.TEAL : Color.WHITE;
        l.setBackground(fondo);
        l.setForeground(futura ? Estilo.BORDER : sel ? Color.WHITE : Estilo.DARK);
        if (fecha.equals(hoy) && !sel) l.setBorder(BorderFactory.createLineBorder(Estilo.TEAL));
        if (!futura) {
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (!sel) l.setBackground(Estilo.LIGHT); }
                @Override public void mouseExited(MouseEvent e) { l.setBackground(fondo); }
                @Override public void mouseClicked(MouseEvent e) {
                    setFecha(fecha);
                    calendario.setVisible(false);
                }
            });
        }
        return l;
    }

    /** Deja escribir solo digitos y agrega las barras: DD/MM/AAAA. */
    private static class FiltroFecha extends DocumentFilter {
        @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a)
                throws BadLocationException { replace(fb, off, 0, s, a); }

        @Override public void remove(FilterBypass fb, int off, int len)
                throws BadLocationException { replace(fb, off, len, "", null); }

        @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a)
                throws BadLocationException {
            Document doc = fb.getDocument();
            String actual = doc.getText(0, doc.getLength());
            String nuevo = actual.substring(0, off) + (s == null ? "" : s) + actual.substring(off + len);
            String dig = nuevo.replaceAll("\\D", "");
            if (dig.length() > 8) dig = dig.substring(0, 8);
            StringBuilder f = new StringBuilder();
            for (int i = 0; i < dig.length(); i++) {
                if (i == 2 || i == 4) f.append('/');
                f.append(dig.charAt(i));
            }
            if (!f.toString().equals(actual)) fb.replace(0, doc.getLength(), f.toString(), a);
        }
    }

    /** Icono de calendario dibujado a mano (sin archivos de imagen). */
    private static class IconoCalendario implements Icon {
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.DARK);
            g2.drawRoundRect(x + 1, y + 2, 13, 12, 3, 3);
            g2.fillRect(x + 1, y + 2, 14, 4);
            g2.fillRect(x + 4, y, 2, 4);
            g2.fillRect(x + 10, y, 2, 4);
            for (int f = 0; f < 2; f++) {
                for (int col = 0; col < 3; col++) g2.fillRect(x + 4 + col * 3, y + 8 + f * 3, 2, 2);
            }
            g2.dispose();
        }
    }
}
