package saludar.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import saludar.controlador.DatosConsulta;
import saludar.controlador.SalaEsperaController;
import saludar.controlador.SalaEsperaVista;
import saludar.modelo.*;

/**
 * Vista de la sala de espera con triage y del registro de la consulta (CU-04).
 * Arma la tabla y los dialogos; las decisiones las toma SalaEsperaController.
 */
public class PanelAtencion extends PanelVista implements SalaEsperaVista {

    private static final String[] COLUMNAS = {"ID", "Paciente", "Edad", "Prioridad", "Tiempo en espera", ""};
    private static final int COL_ATENDER = 5;

    private SalaEsperaController controlador;
    private final JTextField tfDni = new JTextField(9);
    private final JLabel lblConteo = new JLabel(" ");
    private final ModeloSala modeloSala = new ModeloSala();
    private final JTable tabla = new JTable(modeloSala);
    private DialogoIngreso dialogoIngreso;
    private DialogoConsulta dialogoConsulta;

    public PanelAtencion() {
        // ---- Ingreso por DNI ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setBorder(Estilo.card("Ingreso a la sala de espera"));
        top.setOpaque(false);
        top.add(Estilo.etiqueta("DNI del paciente:"));
        top.add(tfDni);
        JButton bBuscar = Estilo.boton("Buscar paciente", true);
        top.add(bBuscar);
        bBuscar.addActionListener(e -> controlador.buscarParaIngreso(tfDni.getText()));
        tfDni.addActionListener(e -> controlador.buscarParaIngreso(tfDni.getText()));

        // ---- Tabla de la sala ----
        Estilo.estilizarTabla(tabla);
        tabla.setRowHeight(38);
        tabla.setRowSelectionAllowed(false);
        tabla.setFocusable(false);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderSala());
        int[] anchos = {50, 380, 60, 90, 170, 120};          // ARREGLO de anchos por columna
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila >= 0 && tabla.columnAtPoint(e.getPoint()) == COL_ATENDER) controlador.atender(modeloSala.get(fila));
            }
        });
        tabla.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean enBoton = tabla.rowAtPoint(e.getPoint()) >= 0
                        && tabla.columnAtPoint(e.getPoint()) == COL_ATENDER;
                tabla.setCursor(Cursor.getPredefinedCursor(enBoton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JButton bActualizar = Estilo.boton("Actualizar", false);
        bActualizar.addActionListener(e -> controlador.refrescar());
        lblConteo.setFont(Estilo.H3);
        lblConteo.setForeground(Estilo.DARK);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(lblConteo, BorderLayout.WEST);
        pie.add(bActualizar, BorderLayout.EAST);

        JPanel sala = new JPanel(new BorderLayout(0, 8));
        sala.setOpaque(false);
        sala.setBorder(Estilo.card("Sala de espera"));
        sala.add(new JScrollPane(tabla), BorderLayout.CENTER);
        sala.add(pie, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(sala, BorderLayout.CENTER);

        // actualiza el tiempo en espera cada segundo (presentacion pura: no consulta la base)
        new Timer(1000, e -> modeloSala.actualizarTiempos()).start();
    }

    @Override public void setControlador(SalaEsperaController controlador) { this.controlador = controlador; }

    @Override public void refrescar() { controlador.refrescar(); }

    @Override public void mostrarSala(List<IngresoSala> ingresos, int[] c, int total) {
        modeloSala.cargar(ingresos);
        lblConteo.setText(String.format(" En espera: %d     Rojo: %d     Amarillo: %d     Verde: %d",
                total, c[Prioridad.ALTA.ordinal()], c[Prioridad.MEDIA.ordinal()], c[Prioridad.BAJA.ordinal()]));
    }

    @Override public void limpiarDni() { tfDni.setText(""); }

    @Override public void abrirIngreso(Paciente p) {
        dialogoIngreso = new DialogoIngreso(p);
        dialogoIngreso.setVisible(true);
    }

    @Override public void cerrarIngreso() {
        if (dialogoIngreso != null) dialogoIngreso.dispose();
    }

    @Override public void abrirConsulta(IngresoSala ingreso, Consulta consulta, List<Cie10> catalogo) {
        dialogoConsulta = new DialogoConsulta(ingreso, consulta, catalogo);
        dialogoConsulta.setVisible(true);
    }

    @Override public void cerrarConsulta() {
        if (dialogoConsulta != null) dialogoConsulta.dispose();
    }

    private static String edadTexto(Paciente p) {
        return p.getEdad() >= 0 ? p.getEdad() + "" : "s/d";
    }

    private static String formatoEspera(IngresoSala i) {
        Duration d = i.getTiempoEspera();
        long seg = d.getSeconds();
        long h = seg / 3600, m = (seg % 3600) / 60, ss = seg % 60;
        String t = h > 0 ? String.format("%d h %02d min", h, m) : String.format("%d min %02d s", m, ss);
        return i.getEstado() == EstadoIngreso.EN_ATENCION ? t + "  (en atencion)" : t;
    }

    // ---- Registro de la consulta: el modal solo se cierra al guardar ----

    private class DialogoConsulta extends JDialog {

        DialogoConsulta(IngresoSala ing, Consulta c, List<Cie10> catalogo) {
            super(SwingUtilities.getWindowAncestor(PanelAtencion.this),
                    "Registrar consulta - " + c.getPaciente().getNombreCompleto(), Dialog.ModalityType.APPLICATION_MODAL);
            Paciente p = c.getPaciente();
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    JOptionPane.showMessageDialog(DialogoConsulta.this,
                            "Para cerrar esta ventana debe guardar la consulta.",
                            "Consulta en curso", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 4, 4, 4);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;
            int y = 0;

            if (p.tieneAlergias()) {                                      // RF-19, RN-04
                StringBuilder sb = new StringBuilder("/!\\ ALERGIAS: ");
                for (Alergia a : p.getAlergias()) sb.append(a).append("   ");
                JLabel al = Estilo.alerta();
                al.setText(sb.toString());
                al.setVisible(true);
                g.gridx = 0; g.gridy = y++; g.gridwidth = 2; form.add(al, g); g.gridwidth = 1;
            }
            g.gridx = 0; g.gridy = y++; g.gridwidth = 2;
            form.add(Estilo.etiqueta("Paciente: " + p.getNombreCompleto() + "   (" + p.getNombreCobertura() + ")"
                    + "   -   Prioridad " + ing.getPrioridad().getEtiqueta()
                    + "   -   " + c.getProfesional().getResumen()), g);
            g.gridwidth = 1;

            JTextField tfMotivo = new JTextField(30);
            JTextArea taEval = area();
            JTextArea taIndic = area();
            JComboBox<Cie10> cbCie = new JComboBox<>(catalogo.toArray(new Cie10[0]));
            cbCie.setMaximumRowCount(12);
            JTextField tfFarmaco = new JTextField(14);
            JTextField tfDosis = new JTextField(8);
            JTextField tfFrec = new JTextField(8);
            JTextField tfDuracion = new JTextField(7);

            y = fila(form, g, y, "Motivo:", tfMotivo);
            y = fila(form, g, y, "Evaluacion:", new JScrollPane(taEval));
            y = fila(form, g, y, "Indicaciones:", new JScrollPane(taIndic));
            y = fila(form, g, y, "Diagnostico (CIE-10):", cbCie);
            JPanel med = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            med.setOpaque(false);
            med.add(tfFarmaco);
            med.add(new JLabel("dosis:")); med.add(tfDosis);
            med.add(new JLabel("frec.:")); med.add(tfFrec);
            med.add(new JLabel("durac.:")); med.add(tfDuracion);
            fila(form, g, y, "Medicacion:", med);

            JButton bGuardar = Estilo.boton("Guardar consulta", true);
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            acciones.setOpaque(false);
            acciones.add(bGuardar);

            JPanel cont = new JPanel(new BorderLayout(10, 10));
            cont.setBackground(Estilo.PANEL);
            cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            cont.add(form, BorderLayout.CENTER);
            cont.add(acciones, BorderLayout.SOUTH);
            setContentPane(cont);

            // la vista arma el DTO con lo ingresado y se lo entrega al controlador
            bGuardar.addActionListener(e -> controlador.guardarConsulta(ing, c, new DatosConsulta(
                    tfMotivo.getText(), taEval.getText(), taIndic.getText(), (Cie10) cbCie.getSelectedItem(),
                    tfFarmaco.getText(), tfDosis.getText(), tfFrec.getText(), tfDuracion.getText())));

            pack();
            setLocationRelativeTo(PanelAtencion.this);
        }

        private JTextArea area() {
            JTextArea t = new JTextArea(3, 30);
            t.setLineWrap(true);
            t.setWrapStyleWord(true);
            return t;
        }

        private int fila(JPanel form, GridBagConstraints g, int y, String etiqueta, Component c) {
            g.gridx = 0; g.gridy = y; form.add(Estilo.etiqueta(etiqueta), g);
            g.gridx = 1; form.add(c, g);
            return y + 1;
        }
    }

    // ---- Ingreso: datos del paciente + triage ----

    private class DialogoIngreso extends JDialog {
        private final List<JLabel> opciones = new ArrayList<>();
        private Prioridad elegida;

        DialogoIngreso(Paciente p) {
            super(SwingUtilities.getWindowAncestor(PanelAtencion.this), "Ingreso a sala de espera",
                    Dialog.ModalityType.APPLICATION_MODAL);

            JPanel datos = new JPanel(new GridBagLayout());
            datos.setOpaque(false);
            datos.setBorder(Estilo.card("Datos del paciente"));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(3, 4, 3, 14);
            g.anchor = GridBagConstraints.WEST;
            String[][] filas = {
                    {"Paciente:", p.getNombreCompleto()},
                    {"DNI:", p.getDni()},
                    {"Edad:", p.getEdad() >= 0 ? p.getEdad() + " anios" : "s/d"},
                    {"Sexo:", String.valueOf(p.getSexo())},
                    {"Obra social:", p.getNombreCobertura()
                            + (p.getNroAfiliado() == null ? "" : "  (" + p.getNroAfiliado() + ")")}};
            int y = 0;
            for (String[] f : filas) {
                g.gridx = 0; g.gridy = y; datos.add(Estilo.etiqueta(f[0]), g);
                JLabel v = new JLabel(f[1]);
                v.setFont(Estilo.BASE);
                g.gridx = 1; g.weightx = 1; datos.add(v, g);
                g.weightx = 0;
                y++;
            }
            if (p.tieneAlergias()) {
                StringBuilder sb = new StringBuilder("/!\\ ALERGIAS: ");
                for (Alergia a : p.getAlergias()) sb.append(a.getSustancia()).append("   ");
                JLabel al = Estilo.alerta();
                al.setText(sb.toString());
                al.setVisible(true);
                g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
                g.insets = new Insets(8, 4, 3, 4);
                datos.add(al, g);
            }

            JPanel triage = new JPanel(new GridLayout(1, 3, 10, 0));
            triage.setOpaque(false);
            triage.setBorder(Estilo.card("Triage"));
            triage.add(opcion(Prioridad.BAJA));
            triage.add(opcion(Prioridad.MEDIA));
            triage.add(opcion(Prioridad.ALTA));

            JButton bIngresar = Estilo.boton("Ingresar paciente", true);
            JButton bLimpiar = Estilo.boton("Limpiar", false);
            JButton bCancelar = Estilo.boton("Cancelar", false);
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            acciones.setOpaque(false);
            acciones.add(bCancelar); acciones.add(bLimpiar); acciones.add(bIngresar);

            bCancelar.addActionListener(e -> dispose());
            bLimpiar.addActionListener(e -> seleccionar(null));
            bIngresar.addActionListener(e -> controlador.ingresar(p, elegida));
            getRootPane().registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            JPanel cont = new JPanel(new BorderLayout(10, 10));
            cont.setBackground(Estilo.PANEL);
            cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            cont.add(datos, BorderLayout.NORTH);
            cont.add(triage, BorderLayout.CENTER);
            cont.add(acciones, BorderLayout.SOUTH);
            setContentPane(cont);
            pack();
            setResizable(false);
            setLocationRelativeTo(PanelAtencion.this);
        }

        private JLabel opcion(Prioridad pr) {
            JLabel l = new JLabel("<html><center><b>" + pr.getColor() + "</b><br>Prioridad "
                    + pr.getEtiqueta().toLowerCase() + "</center></html>", SwingConstants.CENTER);
            l.setOpaque(true);
            l.setFont(Estilo.BASE);
            l.setPreferredSize(new Dimension(150, 64));
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.putClientProperty(Prioridad.class, pr);
            l.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { seleccionar(pr); }
            });
            opciones.add(l);
            pintar(l, false);
            return l;
        }

        private void seleccionar(Prioridad pr) {
            elegida = pr;
            for (JLabel l : opciones) pintar(l, l.getClientProperty(Prioridad.class) == pr);
        }

        private void pintar(JLabel l, boolean sel) {
            Prioridad pr = (Prioridad) l.getClientProperty(Prioridad.class);
            Color c = Estilo.colorTriage(pr);
            l.setBackground(sel ? c : Estilo.fondoTriage(pr));
            l.setForeground(sel ? Color.WHITE : Estilo.DARK);
            l.setBorder(sel ? BorderFactory.createLineBorder(Estilo.DARK, 3)
                    : BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(c, 1),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }
    }

    // ---- Modelo y render de la tabla ----

    private static class ModeloSala extends AbstractTableModel {
        private List<IngresoSala> filas = new ArrayList<>();

        void cargar(List<IngresoSala> l) { filas = l; fireTableDataChanged(); }
        IngresoSala get(int fila) { return filas.get(fila); }
        void actualizarTiempos() { if (!filas.isEmpty()) fireTableRowsUpdated(0, filas.size() - 1); }

        @Override public int getRowCount() { return filas.size(); }
        @Override public int getColumnCount() { return COLUMNAS.length; }
        @Override public String getColumnName(int c) { return COLUMNAS[c]; }

        @Override public Object getValueAt(int fila, int col) {
            IngresoSala i = filas.get(fila);
            Paciente p = i.getPaciente();
            return switch (col) {
                case 0 -> i.getId();
                case 1 -> p.getNombreCompleto() + "   -   DNI " + p.getDni() + "   -   " + p.getNombreCobertura();
                case 2 -> edadTexto(p);
                case 3 -> i.getPrioridad().getEtiqueta();
                case 4 -> formatoEspera(i);
                default -> "Atender";
            };
        }
    }

    /** Pinta cada fila con el color de su triage (fondo claro) y el boton Atender. */
    private class RenderSala extends DefaultTableCellRenderer {
        private final JPanel celdaBoton = new JPanel(new GridBagLayout());

        RenderSala() {
            JButton b = Estilo.boton("Atender", true);
            b.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));
            celdaBoton.add(b);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int fila, int col) {
            Prioridad pr = modeloSala.get(fila).getPrioridad();
            Color fondo = Estilo.fondoTriage(pr);
            if (col == COL_ATENDER) {
                celdaBoton.setBackground(fondo);
                return celdaBoton;
            }
            super.getTableCellRendererComponent(t, v, false, false, fila, col);
            setBackground(fondo);
            setForeground(Estilo.DARK);
            setFont(col == 3 ? Estilo.H3 : Estilo.BASE);
            setBorder(col == 0
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 6, 0, 0, Estilo.colorTriage(pr)),
                            BorderFactory.createEmptyBorder(0, 8, 0, 8))
                    : BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }
}
