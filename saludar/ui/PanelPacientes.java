package saludar.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.Period;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import saludar.excepciones.DniDuplicadoException;
import saludar.excepciones.DniInvalidoException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.modelo.Alergia;
import saludar.modelo.Paciente;
import saludar.modelo.Severidad;
import saludar.servicio.PacienteService;

/** ABM de pacientes: alta con cobertura, listado con buscador y detalle. */
public class PanelPacientes extends JPanel {

    private final PacienteService service;
    private final DefaultTableModel modelo;
    private final JTable tabla;

    private final JTextField tfDni = new JTextField(9);
    private final JTextField tfApellido = new JTextField(12);
    private final JTextField tfNombre = new JTextField(12);
    private final CampoFecha cfNac = new CampoFecha();
    private final JTextField tfEdad = new JTextField(9);
    private final JComboBox<Character> cbSexo = new JComboBox<>(new Character[]{'F', 'M', 'X'});
    private final JComboBox<String> cbObra = new JComboBox<>(
            new String[]{"OSDE", "Swiss Medical", "PAMI", "IOMA", "Particular"});
    private final JTextField tfAfiliado = new JTextField(10);
    private final JTextField tfBuscar = new JTextField(9);

    public PanelPacientes(PacienteService service) {
        this.service = service;
        setLayout(new BorderLayout(12, 12));
        setBackground(Estilo.PANEL);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ---- Alta (form en grilla) ----
        JPanel alta = new JPanel(new GridBagLayout());
        alta.setOpaque(false);
        alta.setBorder(Estilo.card("Registrar paciente"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;
        int y = 0;
        addCampo(alta, g, 0, y, "DNI:", tfDni);
        addCampo(alta, g, 2, y, "Apellido:", tfApellido);
        addCampo(alta, g, 4, y, "Nombre:", tfNombre);
        y++;
        addCampo(alta, g, 0, y, "Nacimiento:", cfNac);
        addCampo(alta, g, 2, y, "Edad:", tfEdad);
        addCampo(alta, g, 4, y, "Sexo:", cbSexo);
        y++;
        addCampo(alta, g, 0, y, "Obra social:", cbObra);
        addCampo(alta, g, 2, y, "N.° afiliado:", tfAfiliado);
        JButton bAlta = Estilo.boton("Registrar paciente", true);
        JButton bLimpiar = Estilo.boton("Limpiar", false);
        g.gridx = 4; g.gridy = y; g.gridwidth = 2; g.anchor = GridBagConstraints.EAST;
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); bp.setOpaque(false);
        bp.add(bLimpiar); bp.add(bAlta);
        alta.add(bp, g);
        g.gridwidth = 1; g.anchor = GridBagConstraints.WEST;

        // la edad no se carga: se calcula a partir de la fecha de nacimiento
        tfEdad.setEditable(false);
        tfEdad.setFocusable(false);
        tfEdad.setBackground(Estilo.PANEL);
        cfNac.alCambiar(this::calcularEdad);

        // ---- Listado con buscador ----
        modelo = new DefaultTableModel(new Object[]{"DNI", "Apellido", "Nombre",
                "Sexo", "Obra social", "Edad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        Estilo.estilizarTabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) mostrarDetalle(seleccionado());
            }
        });

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        busq.setOpaque(false);
        busq.add(Estilo.etiqueta("Buscar por DNI:"));
        busq.add(tfBuscar);
        JButton bBuscar = Estilo.boton("Buscar", false);
        JButton bRefrescar = Estilo.boton("Refrescar", false);
        busq.add(bBuscar); busq.add(bRefrescar);
        JLabel ayuda = new JLabel("   Doble clic para ver el detalle");
        ayuda.setFont(Estilo.BASE);
        ayuda.setForeground(Color.GRAY);
        busq.add(ayuda);

        JPanel listado = new JPanel(new BorderLayout(0, 8));
        listado.setOpaque(false);
        listado.setBorder(Estilo.card("Pacientes (ordenados por apellido)"));
        listado.add(busq, BorderLayout.NORTH);
        listado.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(alta, BorderLayout.NORTH);
        add(listado, BorderLayout.CENTER);

        bAlta.addActionListener(e -> registrar());
        bLimpiar.addActionListener(e -> limpiar());
        bRefrescar.addActionListener(e -> { tfBuscar.setText(""); refrescar(); });
        bBuscar.addActionListener(e -> buscar());
        tfBuscar.addActionListener(e -> buscar());

        refrescar();
    }

    private void addCampo(JPanel p, GridBagConstraints g, int x, int y, String lbl, JComponent c) {
        g.gridx = x; g.gridy = y; p.add(Estilo.etiqueta(lbl), g);
        g.gridx = x + 1; g.gridy = y; p.add(c, g);
    }

    private void calcularEdad() {
        try {
            LocalDate f = cfNac.getFecha();
            if (f == null || f.isAfter(LocalDate.now())) { tfEdad.setText(""); return; }
            tfEdad.setText(edadTexto(Period.between(f, LocalDate.now()).getYears()));
        } catch (IllegalArgumentException ex) {
            tfEdad.setText("");          // fecha incompleta o invalida
        }
    }

    private static String edadTexto(int edad) {
        return edad < 0 ? "s/d" : edad == 1 ? "1 anio" : edad + " anios";
    }

    private void registrar() {
        try {
            Paciente p = service.registrar(tfDni.getText().trim(),
                    tfApellido.getText().trim(), tfNombre.getText().trim(), cfNac.getFecha(),
                    (Character) cbSexo.getSelectedItem(), (String) cbObra.getSelectedItem(),
                    tfAfiliado.getText().trim());
            JOptionPane.showMessageDialog(this, "Paciente registrado:\n" + p.getResumen(),
                    "Alta exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiar();
            refrescar();
        } catch (DniDuplicadoException | DniInvalidoException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        tfDni.setText(""); tfApellido.setText(""); tfNombre.setText("");
        cfNac.setFecha(null); tfAfiliado.setText(""); cbSexo.setSelectedIndex(0); cbObra.setSelectedIndex(0);
    }

    private void refrescar() {
        modelo.setRowCount(0);
        for (Paciente p : service.listarOrdenadosPorApellido()) {
            String edad = p.getEdad() >= 0 ? p.getEdad() + "" : "s/d";
            modelo.addRow(new Object[]{p.getDni(), p.getApellido(), p.getNombre(),
                    p.getSexo(), p.getObraSocial(), edad});
        }
    }

    private Paciente seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return null;
        String dni = (String) modelo.getValueAt(fila, 0);
        try { return service.buscarPorDni(dni); }
        catch (PacienteNoEncontradoException e) { return null; }
    }

    /** Detalle con los datos personales del paciente. */
    private void mostrarDetalle(Paciente p) {
        if (p == null) return;
        JPanel datos = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 14);
        g.anchor = GridBagConstraints.WEST;
        String[][] filas = {
                {"Nombre:", p.getNombre()},
                {"Apellido:", p.getApellido()},
                {"Obra social:", p.getObraSocial()},
                {"Edad:", edadTexto(p.getEdad())}};
        for (int i = 0; i < filas.length; i++) {
            g.gridx = 0; g.gridy = i; datos.add(Estilo.etiqueta(filas[i][0]), g);
            JLabel v = new JLabel(filas[i][1]);
            v.setFont(Estilo.BASE);
            g.gridx = 1; datos.add(v, g);
        }
        Object[] opciones = {"Cerrar", "Agregar alergia"};
        int op = JOptionPane.showOptionDialog(this, datos, "Detalle del paciente",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (op == 1) agregarAlergia(p);
    }

    private void agregarAlergia(Paciente p) {
        JTextField sust = new JTextField();
        JTextField reac = new JTextField();
        JComboBox<Severidad> sev = new JComboBox<>(Severidad.values());
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(Estilo.etiqueta("Sustancia:")); form.add(sust);
        form.add(Estilo.etiqueta("Tipo de reaccion:")); form.add(reac);
        form.add(Estilo.etiqueta("Severidad:")); form.add(sev);
        int op = JOptionPane.showConfirmDialog(this, form, "Agregar alergia a " + p.getNombreCompleto(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION && !sust.getText().isBlank()) {
            p.agregarAlergia(new Alergia(sust.getText().trim(), reac.getText().trim(),
                    (Severidad) sev.getSelectedItem()));
        }
    }

    /** Busqueda por DNI: selecciona al paciente en el listado y abre su detalle. */
    private void buscar() {
        try {
            Paciente p = service.buscarPorDni(tfBuscar.getText().trim());
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (p.getDni().equals(modelo.getValueAt(i, 0))) {
                    tabla.setRowSelectionInterval(i, i);
                    tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                    break;
                }
            }
            mostrarDetalle(p);
        } catch (PacienteNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sin resultados", JOptionPane.WARNING_MESSAGE);
        }
    }
}
