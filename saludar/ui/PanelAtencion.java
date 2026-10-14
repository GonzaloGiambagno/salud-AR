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

import saludar.excepciones.ConsultaCerradaException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.modelo.*;
import saludar.servicio.CatalogoCie10;
import saludar.servicio.ConsultaService;
import saludar.servicio.PacienteService;
import saludar.servicio.SalaDeEspera;

/** Sala de espera con triage y registro de consulta con diagnostico y medicacion. */
public class PanelAtencion extends JPanel {

    private static final String[] COLUMNAS = {"ID", "Paciente", "Edad", "Prioridad", "Tiempo en espera", ""};
    private static final int COL_ATENDER = 5;

    private final PacienteService pacienteService;
    private final ConsultaService consultaService;
    private final SalaDeEspera sala;
    private final Profesional medico;
    private final CatalogoCie10 catalogo = new CatalogoCie10();

    private final JTextField tfDni = new JTextField(9);
    private final ModeloSala modeloSala = new ModeloSala();
    private final JTable tabla = new JTable(modeloSala);

    public PanelAtencion(PacienteService ps, ConsultaService cs, SalaDeEspera sala, Profesional medico) {
        this.pacienteService = ps; this.consultaService = cs; this.sala = sala; this.medico = medico;
        setLayout(new BorderLayout(12, 12));
        setBackground(Estilo.PANEL);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ---- Ingreso por DNI ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setBorder(Estilo.card("Ingreso a la sala de espera"));
        top.setOpaque(false);
        top.add(Estilo.etiqueta("DNI del paciente:"));
        top.add(tfDni);
        JButton bBuscar = Estilo.boton("Buscar paciente", true);
        top.add(bBuscar);
        bBuscar.addActionListener(e -> buscar());
        tfDni.addActionListener(e -> buscar());

        // ---- Tabla de la sala ----
        Estilo.estilizarTabla(tabla);
        tabla.setRowHeight(38);
        tabla.setRowSelectionAllowed(false);
        tabla.setFocusable(false);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderSala());
        int[] anchos = {50, 380, 60, 90, 140, 120};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila >= 0 && tabla.columnAtPoint(e.getPoint()) == COL_ATENDER) atender(modeloSala.get(fila));
            }
        });
        tabla.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean enBoton = tabla.rowAtPoint(e.getPoint()) >= 0
                        && tabla.columnAtPoint(e.getPoint()) == COL_ATENDER;
                tabla.setCursor(Cursor.getPredefinedCursor(enBoton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(Estilo.card("Sala de espera"));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // actualiza el tiempo en espera cada segundo
        new Timer(1000, e -> modeloSala.actualizarTiempos()).start();
        refrescar();
    }

    private void refrescar() { modeloSala.cargar(sala.getIngresos()); }

    private void buscar() {
        String dni = tfDni.getText().trim();
        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el DNI del paciente.");
            return;
        }
        Paciente p;
        try {
            p = pacienteService.buscarPorDni(dni);
        } catch (PacienteNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sin resultados", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sala.estaEnSala(p)) {
            JOptionPane.showMessageDialog(this, p.getNombreCompleto() + " ya se encuentra en la sala de espera.");
            return;
        }
        if (new DialogoIngreso(p).mostrar()) {
            tfDni.setText("");
            refrescar();
        }
    }

    private static String edadTexto(Paciente p) {
        return p.getEdad() >= 0 ? p.getEdad() + "" : "s/d";
    }

    private static String formatoEspera(Duration d) {
        long s = d.getSeconds();
        long h = s / 3600, m = (s % 3600) / 60, seg = s % 60;
        return h > 0 ? String.format("%d h %02d min", h, m) : String.format("%d min %02d s", m, seg);
    }

    // ---- Atencion: el modal solo se cierra al guardar la consulta ----

    private void atender(IngresoSala ing) {
        sala.iniciarAtencion(ing);
        Paciente p = ing.getPaciente();

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Registrar consulta - " + p.getNombreCompleto(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(dlg, "Para cerrar esta ventana debe guardar la consulta.",
                        "Consulta en curso", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // ---- Formulario de consulta ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        if (p.tieneAlergias()) {
            StringBuilder sb = new StringBuilder("/!\\ ALERGIAS: ");
            for (Alergia a : p.getAlergias()) sb.append(a).append("   ");
            JLabel al = Estilo.alerta(); al.setText(sb.toString()); al.setVisible(true);
            g.gridx = 0; g.gridy = y++; g.gridwidth = 2; form.add(al, g); g.gridwidth = 1;
        }

        g.gridx = 0; g.gridy = y; g.gridwidth = 2;
        form.add(Estilo.etiqueta("Paciente: " + p.getNombreCompleto() + "   (" + p.getObraSocial() + ")"
                + "   -   Prioridad " + ing.getPrioridad().getEtiqueta()), g);
        g.gridwidth = 1; y++;

        JTextField tfMotivo = new JTextField(28);
        JTextArea taEval = new JTextArea(3, 28);
        taEval.setLineWrap(true); taEval.setWrapStyleWord(true);
        JComboBox<CatalogoCie10.Entrada> cbCie = new JComboBox<>();
        for (CatalogoCie10.Entrada e : catalogo.todas()) cbCie.addItem(e);
        JTextField tfFarmaco = new JTextField(14);
        JTextField tfDosis = new JTextField(8);
        JTextField tfFrec = new JTextField(8);

        g.gridx = 0; g.gridy = y; form.add(Estilo.etiqueta("Motivo:"), g);
        g.gridx = 1; form.add(tfMotivo, g); y++;
        g.gridx = 0; g.gridy = y; form.add(Estilo.etiqueta("Evaluacion:"), g);
        g.gridx = 1; form.add(new JScrollPane(taEval), g); y++;
        g.gridx = 0; g.gridy = y; form.add(Estilo.etiqueta("Diagnostico (CIE-10):"), g);
        g.gridx = 1; form.add(cbCie, g); y++;
        g.gridx = 0; g.gridy = y; form.add(Estilo.etiqueta("Medicacion:"), g);
        JPanel med = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); med.setOpaque(false);
        med.add(tfFarmaco); med.add(new JLabel("dosis:")); med.add(tfDosis);
        med.add(new JLabel("frec.:")); med.add(tfFrec);
        g.gridx = 1; form.add(med, g); y++;

        JButton bGuardar = Estilo.boton("Guardar consulta", true);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acciones.setOpaque(false);
        acciones.add(bGuardar);

        JPanel cont = new JPanel(new BorderLayout(10, 10));
        cont.setBackground(Estilo.PANEL);
        cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        cont.add(form, BorderLayout.CENTER);
        cont.add(acciones, BorderLayout.SOUTH);
        dlg.setContentPane(cont);

        bGuardar.addActionListener(e -> {
            if (tfMotivo.getText().isBlank()) {
                JOptionPane.showMessageDialog(dlg, "El motivo de la consulta es obligatorio.",
                        "Falta el motivo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Consulta c = consultaService.crearBorrador(p, medico, tfMotivo.getText().trim(), taEval.getText().trim());
            CatalogoCie10.Entrada sel = (CatalogoCie10.Entrada) cbCie.getSelectedItem();
            if (sel != null) c.agregarDiagnostico(catalogo.aDiagnostico(sel, true));
            if (!tfFarmaco.getText().isBlank()) {
                c.agregarMedicacion(new Medicacion(tfFarmaco.getText().trim(),
                        tfDosis.getText().trim(), tfFrec.getText().trim(), ""));
            }
            consultaService.cerrar(c);   // queda CERRADA (inviolable)
            sala.finalizar(ing);         // el paciente pasa a FINALIZADO y sale de la sala
            dlg.dispose();
            refrescar();

            String msg = "Consulta #" + c.getId() + " [" + c.getEstado() + "] registrada.\n"
                    + "Dx: " + c.getDiagnosticoPrincipal() + "\n"
                    + "Medicacion: " + c.getMedicacionResumen();
            try {
                consultaService.editarMotivo(c, "cambio no permitido");
            } catch (ConsultaCerradaException ex) {
                msg += "\n\n(Inviolabilidad: " + ex.getMessage() + ")";
            }
            JOptionPane.showMessageDialog(this, msg, "Consulta registrada", JOptionPane.INFORMATION_MESSAGE);
        });

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ---- Modal de ingreso: datos del paciente + triage ----

    private class DialogoIngreso extends JDialog {
        private final Paciente paciente;
        private final JButton bIngresar = Estilo.boton("Ingresar paciente", true);
        private final List<JLabel> opciones = new ArrayList<>();
        private Prioridad elegida;
        private boolean ingresado;

        DialogoIngreso(Paciente p) {
            super(SwingUtilities.getWindowAncestor(PanelAtencion.this), "Ingreso a sala de espera",
                    Dialog.ModalityType.APPLICATION_MODAL);
            this.paciente = p;

            // datos del paciente
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
                    {"Obra social:", p.getObraSocial() + "  (" + p.getNroAfiliado() + ")"}};
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
                JLabel al = Estilo.alerta(); al.setText(sb.toString()); al.setVisible(true);
                g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
                g.insets = new Insets(8, 4, 3, 4);
                datos.add(al, g);
            }

            // triage
            JPanel triage = new JPanel(new GridLayout(1, 3, 10, 0));
            triage.setOpaque(false);
            triage.setBorder(Estilo.card("Triage"));
            triage.add(opcion(Prioridad.BAJA));
            triage.add(opcion(Prioridad.MEDIA));
            triage.add(opcion(Prioridad.ALTA));

            // botones
            JButton bLimpiar = Estilo.boton("Limpiar", false);
            JButton bCancelar = Estilo.boton("Cancelar", false);
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            acciones.setOpaque(false);
            acciones.add(bCancelar); acciones.add(bLimpiar); acciones.add(bIngresar);

            bCancelar.addActionListener(e -> dispose());
            bLimpiar.addActionListener(e -> seleccionar(null));
            bIngresar.addActionListener(e -> {
                if (elegida == null) {
                    JOptionPane.showMessageDialog(this, "Seleccione un triage (Verde, Amarillo o Rojo).",
                            "Falta el triage", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sala.ingresar(paciente, elegida);
                ingresado = true;
                dispose();
            });
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

        /** Muestra el modal y devuelve true si el paciente quedo ingresado. */
        boolean mostrar() {
            setVisible(true);
            return ingresado;
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
            switch (col) {
                case 0: return i.getId();
                case 1: return p.getNombreCompleto() + "   -   DNI " + p.getDni() + "   -   " + p.getObraSocial();
                case 2: return edadTexto(p);
                case 3: return i.getPrioridad().getEtiqueta();
                case 4: return formatoEspera(i.getTiempoEspera());
                default: return "Atender";
            }
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
