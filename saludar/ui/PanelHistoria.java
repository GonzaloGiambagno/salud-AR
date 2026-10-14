package saludar.ui;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import saludar.excepciones.PacienteNoEncontradoException;
import saludar.modelo.Alergia;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;
import saludar.servicio.ConsultaService;
import saludar.servicio.PacienteService;

/** Historia clinica: alerta de alergias, linea de tiempo y detalle de la consulta. */
public class PanelHistoria extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PacienteService pacienteService;
    private final ConsultaService consultaService;
    private final JTextField tfDni = new JTextField(9);
    private final JLabel lblPaciente = Estilo.etiqueta(" ");
    private final JLabel lblAlerta = Estilo.alerta();
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextArea detalle = new JTextArea(6, 20);
    private List<Consulta> actuales = java.util.Collections.emptyList();

    public PanelHistoria(PacienteService ps, ConsultaService cs) {
        this.pacienteService = ps; this.consultaService = cs;
        setLayout(new BorderLayout(12, 12));
        setBackground(Estilo.PANEL);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        top.add(Estilo.etiqueta("DNI del paciente:"));
        top.add(tfDni);
        JButton bVer = Estilo.boton("Ver historia clinica", true);
        top.add(bVer);

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
        split.setResizeWeight(0.6);
        split.setBorder(null);

        add(norte, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        bVer.addActionListener(e -> ver());
    }

    /** Carga la historia de un DNI (util para pruebas/demostracion). */
    public void cargarPorDni(String dni) {
        tfDni.setText(dni); ver();
        if (!actuales.isEmpty()) tabla.setRowSelectionInterval(0, 0);
    }

    private void ver() {
        modelo.setRowCount(0);
        detalle.setText("");
        lblAlerta.setVisible(false);
        lblPaciente.setText(" ");
        try {
            Paciente p = pacienteService.buscarPorDni(tfDni.getText().trim());
            lblPaciente.setText("Paciente: " + p.getNombreCompleto() + "  -  DNI " + p.getDni()
                    + "  -  " + (p.getEdad() >= 0 ? p.getEdad() + " anios" : "edad s/d")
                    + "  -  " + p.getObraSocial());
            if (p.tieneAlergias()) {
                StringBuilder sb = new StringBuilder("/!\\ ALERGIAS: ");
                for (Alergia a : p.getAlergias()) sb.append(a).append("   ");
                lblAlerta.setText(sb.toString());
                lblAlerta.setVisible(true);
            }
            actuales = consultaService.historiaDe(p);
            if (actuales.isEmpty()) {
                modelo.addRow(new Object[]{"-", "-", "-", "(sin consultas registradas)", "-", "-"});
            }
            for (Consulta c : actuales) {
                modelo.addRow(new Object[]{c.getId(), c.getFechaHora().format(FMT),
                        c.getProfesional().getNombreCompleto(), c.getMotivo(),
                        c.getEstado(), c.getDiagnosticoPrincipal()});
            }
        } catch (PacienteNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sin resultados", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void verDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= actuales.size()) { detalle.setText(""); return; }
        Consulta c = actuales.get(fila);
        StringBuilder sb = new StringBuilder();
        sb.append("Consulta #").append(c.getId()).append("   [").append(c.getEstado()).append("]\n");
        sb.append("Fecha: ").append(c.getFechaHora().format(FMT)).append("\n");
        sb.append("Profesional: ").append(c.getProfesional().getResumen()).append("\n");
        sb.append("Motivo: ").append(c.getMotivo()).append("\n");
        sb.append("Evaluacion: ").append(c.getEvaluacion() == null ? "-" : c.getEvaluacion()).append("\n");
        sb.append("Diagnosticos: ");
        if (c.getDiagnosticos().isEmpty()) sb.append("-");
        else c.getDiagnosticos().forEach(d -> sb.append("\n   - ").append(d));
        sb.append("\nMedicacion: ").append(c.getMedicacionResumen());
        detalle.setText(sb.toString());
        detalle.setCaretPosition(0);
    }
}
