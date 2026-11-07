package saludar.ui;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import saludar.controlador.HistoriaController;
import saludar.controlador.HistoriaVista;
import saludar.modelo.Alergia;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;

/** Vista de la historia clinica (CU-07): alerta de alergias, linea de tiempo y detalle. */
public class PanelHistoria extends PanelVista implements HistoriaVista {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private HistoriaController controlador;
    private final JTextField tfDni = new JTextField(9);
    private final JLabel lblPaciente = Estilo.etiqueta(" ");
    private final JLabel lblAlerta = Estilo.alerta();
    private final JButton bExportar = Estilo.boton("Exportar a archivo", false);
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextArea detalle = new JTextArea(6, 20);
    private List<Consulta> consultas = Collections.emptyList();

    public PanelHistoria() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        top.add(Estilo.etiqueta("DNI del paciente:"));
        top.add(tfDni);
        JButton bVer = Estilo.boton("Ver historia clinica", true);
        top.add(bVer);
        top.add(bExportar);
        bExportar.setEnabled(false);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        top.setAlignmentX(LEFT_ALIGNMENT);
        lblPaciente.setAlignmentX(LEFT_ALIGNMENT);
        lblPaciente.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        lblAlerta.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(top);
        norte.add(lblPaciente);
        norte.add(lblAlerta);

        modelo = new DefaultTableModel(new Object[]{"#", "Fecha", "Profesional", "Motivo", "Estado", "Diagnostico"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        Estilo.estilizarTabla(tabla);
        int[] anchos = {40, 125, 120, 300, 95, 260};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tabla.getSelectionModel().addListSelectionListener(e -> verDetalle());
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(Estilo.card("Linea de tiempo de consultas"));

        detalle.setEditable(false);
        detalle.setFont(Estilo.BASE);
        detalle.setBackground(Color.WHITE);
        detalle.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane scrollDet = new JScrollPane(detalle);
        scrollDet.setBorder(Estilo.card("Detalle de la consulta seleccionada"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabla, scrollDet);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        add(norte, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        bVer.addActionListener(e -> controlador.ver(tfDni.getText()));
        tfDni.addActionListener(e -> controlador.ver(tfDni.getText()));
        bExportar.addActionListener(e -> controlador.exportar());
    }

    @Override public void setControlador(HistoriaController controlador) { this.controlador = controlador; }

    @Override public void refrescar() { controlador.refrescar(); }

    /** Carga la historia de un DNI y selecciona la consulta mas reciente (util para demostracion). */
    public void cargarPorDni(String dni) {
        tfDni.setText(dni);
        controlador.ver(dni);
        if (!consultas.isEmpty()) tabla.setRowSelectionInterval(0, 0);
    }

    @Override public void limpiarHistoria() {
        modelo.setRowCount(0);
        detalle.setText("");
        lblAlerta.setVisible(false);
        lblPaciente.setText(" ");
        consultas = Collections.emptyList();
        bExportar.setEnabled(false);
    }

    @Override public void mostrarHistoria(Paciente p, List<Consulta> historia) {
        consultas = historia;
        lblPaciente.setText("Paciente: " + p.getNombreCompleto() + "  -  DNI " + p.getDni()
                + "  -  " + (p.getEdad() >= 0 ? p.getEdad() + " anios" : "edad s/d")
                + "  -  " + p.getNombreCobertura());
        if (p.tieneAlergias()) {                                        // RF-19
            StringBuilder sb = new StringBuilder("/!\\ ALERGIAS: ");
            for (Alergia a : p.getAlergias()) sb.append(a).append("   ");
            lblAlerta.setText(sb.toString());
            lblAlerta.setVisible(true);
        }
        bExportar.setEnabled(true);
        if (historia.isEmpty()) {
            modelo.addRow(new Object[]{"-", "-", "-", "(sin consultas registradas)", "-", "-"});
        }
        for (Consulta c : historia) {
            modelo.addRow(new Object[]{c.getId(), c.getFechaHora().format(FMT),
                    c.getProfesional().getNombreCompleto(), c.getMotivo(),
                    c.getEstado(), c.getDiagnosticoPrincipal()});
        }
    }

    /** Muestra el detalle de la consulta seleccionada (presentacion de datos ya cargados). */
    private void verDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= consultas.size()) { detalle.setText(""); return; }
        Consulta c = consultas.get(fila);
        StringBuilder sb = new StringBuilder();
        sb.append("Consulta #").append(c.getId()).append("   [").append(c.getEstado()).append("]");
        if (c.getIdConsultaRectificada() != null) {
            sb.append("   - rectifica la consulta #").append(c.getIdConsultaRectificada());
        }
        sb.append("\nFecha: ").append(c.getFechaHora().format(FMT)).append("\n");
        sb.append("Profesional: ").append(c.getProfesional().getResumen()).append("\n");
        sb.append("Motivo: ").append(c.getMotivo()).append("\n");
        sb.append("Evaluacion: ").append(c.getEvaluacion() == null ? "-" : c.getEvaluacion()).append("\n");
        sb.append("Indicaciones: ").append(c.getIndicaciones() == null ? "-" : c.getIndicaciones()).append("\n");
        sb.append("Diagnosticos: ");
        if (c.getDiagnosticos().isEmpty()) sb.append("-");
        else c.getDiagnosticos().forEach(d -> sb.append("\n   - ").append(d));
        sb.append("\nMedicacion: ").append(c.getMedicacionResumen());
        detalle.setText(sb.toString());
        detalle.setCaretPosition(0);
    }

    @Override public Path elegirDestinoExportacion(String nombreSugerido) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar historia clinica");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo de texto (*.txt)", "txt"));
        fc.setSelectedFile(new File(nombreSugerido));
        return fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile().toPath() : null;
    }
}
