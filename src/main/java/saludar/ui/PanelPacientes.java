package saludar.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import saludar.controlador.PacienteController;
import saludar.controlador.PacienteVista;
import saludar.excepciones.ValidacionException;
import saludar.modelo.Alergia;
import saludar.modelo.ObraSocial;
import saludar.modelo.Paciente;
import saludar.modelo.Severidad;
import saludar.servicio.DatosPaciente;

/**
 * Vista de pacientes: alta, listado con buscador, detalle, modificacion y baja.
 * Solo arma la pantalla, muestra datos y reenvia los eventos a PacienteController.
 */
public class PanelPacientes extends PanelVista implements PacienteVista {

    private static final String[] COLUMNAS = {"DNI", "Apellido", "Nombre", "Sexo", "Obra social", "Edad"};

    private PacienteController controlador;
    private final FormularioPaciente formAlta = new FormularioPaciente();
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextField tfBuscar = new JTextField(10);
    private final JLabel lblListado = Estilo.etiqueta(" ");
    private final List<Paciente> filas = new ArrayList<>();   // paciente de cada fila de la tabla
    private JDialog dialogoEdicion;
    private FormularioPaciente formEdicion;

    public PanelPacientes() {
        // ---- Alta ----
        JButton bAlta = Estilo.boton("Registrar paciente", true);
        JButton bLimpiar = Estilo.boton("Limpiar", false);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bp.setOpaque(false);
        bp.add(bLimpiar);
        bp.add(bAlta);
        JPanel alta = new JPanel(new BorderLayout(0, 6));
        alta.setOpaque(false);
        alta.setBorder(Estilo.card("Registrar paciente"));
        alta.add(formAlta, BorderLayout.CENTER);
        alta.add(bp, BorderLayout.SOUTH);

        // ---- Listado con buscador ----
        modelo = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        Estilo.estilizarTabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int fila = tabla.getSelectedRow();
                if (e.getClickCount() == 2 && fila >= 0) controlador.verDetalle(filas.get(fila));
            }
        });

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        busq.setOpaque(false);
        busq.add(Estilo.etiqueta("Buscar (DNI o apellido):"));
        busq.add(tfBuscar);
        JButton bBuscar = Estilo.boton("Buscar", false);
        JButton bRefrescar = Estilo.boton("Ver todos", false);
        busq.add(bBuscar);
        busq.add(bRefrescar);
        JLabel ayuda = new JLabel("   Doble clic para ver el detalle");
        ayuda.setFont(Estilo.BASE);
        ayuda.setForeground(Color.GRAY);
        busq.add(ayuda);

        JPanel listado = new JPanel(new BorderLayout(0, 8));
        listado.setOpaque(false);
        listado.setBorder(Estilo.card("Pacientes"));
        listado.add(busq, BorderLayout.NORTH);
        listado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        lblListado.setFont(Estilo.BASE);
        listado.add(lblListado, BorderLayout.SOUTH);

        add(alta, BorderLayout.NORTH);
        add(listado, BorderLayout.CENTER);

        // ---- Eventos: se reenvian al controlador ----
        bAlta.addActionListener(e -> controlador.registrar());
        bLimpiar.addActionListener(e -> formAlta.limpiar());
        bRefrescar.addActionListener(e -> { tfBuscar.setText(""); controlador.refrescar(); });
        bBuscar.addActionListener(e -> controlador.buscar(tfBuscar.getText()));
        tfBuscar.addActionListener(e -> controlador.buscar(tfBuscar.getText()));
    }

    @Override public void setControlador(PacienteController controlador) { this.controlador = controlador; }

    @Override public void refrescar() { controlador.refrescar(); }

    @Override public void mostrarObrasSociales(List<ObraSocial> obras) { formAlta.setObrasSociales(obras); }

    @Override public void mostrarPacientes(List<Paciente> pacientes, String resumen) {
        modelo.setRowCount(0);
        filas.clear();
        for (Paciente p : pacientes) {
            filas.add(p);
            modelo.addRow(new Object[]{p.getDni(), p.getApellido(), p.getNombre(), p.getSexo(),
                    p.getNombreCobertura(), p.getEdad() >= 0 ? p.getEdad() : "s/d"});
        }
        lblListado.setText(" " + resumen);
    }

    @Override public DatosPaciente leerDatosAlta() throws ValidacionException { return formAlta.getDatos(); }

    @Override public void limpiarAlta() { formAlta.limpiar(); }

    @Override public void seleccionar(Paciente p) {
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i).getId() == p.getId()) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                return;
            }
        }
    }

    @Override public AccionPaciente mostrarDetalle(Paciente p) {
        JPanel datos = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 14);
        g.anchor = GridBagConstraints.WEST;
        String[][] campos = {
                {"Nombre:", p.getNombre()},
                {"Apellido:", p.getApellido()},
                {"Obra social:", p.getNombreCobertura()},
                {"Edad:", p.getEdad() >= 0 ? p.getEdad() + " anios" : "s/d"}};
        for (int i = 0; i < campos.length; i++) {
            g.gridx = 0; g.gridy = i; datos.add(Estilo.etiqueta(campos[i][0]), g);
            JLabel v = new JLabel(campos[i][1]);
            v.setFont(Estilo.BASE);
            g.gridx = 1; datos.add(v, g);
        }
        Object[] opciones = {"Cerrar", "Modificar", "Dar de baja", "Agregar alergia"};
        int op = JOptionPane.showOptionDialog(this, datos, "Detalle del paciente",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        return switch (op) {
            case 1 -> AccionPaciente.MODIFICAR;
            case 2 -> AccionPaciente.DAR_DE_BAJA;
            case 3 -> AccionPaciente.AGREGAR_ALERGIA;
            default -> AccionPaciente.CERRAR;
        };
    }

    @Override public void abrirEdicion(Paciente p, List<ObraSocial> obras) {
        formEdicion = new FormularioPaciente();
        formEdicion.setObrasSociales(obras);
        formEdicion.cargar(p);
        dialogoEdicion = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Modificar paciente - DNI " + p.getDni(), Dialog.ModalityType.APPLICATION_MODAL);
        JButton bGuardar = Estilo.boton("Guardar cambios", true);
        JButton bCancelar = Estilo.boton("Cancelar", false);
        bGuardar.addActionListener(e -> controlador.guardarModificacion(p));
        bCancelar.addActionListener(e -> cerrarEdicion());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acciones.setOpaque(false);
        acciones.add(bCancelar);
        acciones.add(bGuardar);
        JPanel cont = new JPanel(new BorderLayout(10, 10));
        cont.setBackground(Estilo.PANEL);
        cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        cont.add(formEdicion, BorderLayout.CENTER);
        cont.add(acciones, BorderLayout.SOUTH);
        dialogoEdicion.setContentPane(cont);
        dialogoEdicion.pack();
        dialogoEdicion.setLocationRelativeTo(this);
        dialogoEdicion.setVisible(true);
    }

    @Override public DatosPaciente leerDatosEdicion() throws ValidacionException { return formEdicion.getDatos(); }

    @Override public void cerrarEdicion() {
        if (dialogoEdicion != null) dialogoEdicion.dispose();
    }

    @Override public Alergia pedirAlergia(Paciente p) {
        JTextField sust = new JTextField();
        JTextField reac = new JTextField();
        JComboBox<Severidad> sev = new JComboBox<>(Severidad.values());
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(Estilo.etiqueta("Sustancia:")); form.add(sust);
        form.add(Estilo.etiqueta("Tipo de reaccion:")); form.add(reac);
        form.add(Estilo.etiqueta("Severidad:")); form.add(sev);
        int op = JOptionPane.showConfirmDialog(this, form, "Agregar alergia a " + p.getNombreCompleto(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return null;
        return new Alergia(sust.getText().trim(), reac.getText().trim(), (Severidad) sev.getSelectedItem());
    }
}
