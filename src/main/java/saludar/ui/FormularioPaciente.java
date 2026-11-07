package saludar.ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import javax.swing.*;
import saludar.excepciones.ValidacionException;
import saludar.modelo.ObraSocial;
import saludar.modelo.Paciente;
import saludar.servicio.DatosPaciente;

/**
 * Formulario de datos personales y de cobertura del paciente. Se reutiliza
 * en el alta (pestania Pacientes) y en la modificacion (dialogo de edicion).
 */
public class FormularioPaciente extends JPanel {

    /** Opcion "Particular" del combo: equivale a no tener cobertura. */
    private static final ObraSocial PARTICULAR = new ObraSocial(0, "Particular", "PART");

    private final JTextField tfDni = new JTextField(9);
    private final JTextField tfApellido = new JTextField(12);
    private final JTextField tfNombre = new JTextField(12);
    private final CampoFecha cfNac = new CampoFecha();
    private final JTextField tfEdad = new JTextField(9);
    private final JComboBox<Character> cbSexo = new JComboBox<>(new Character[]{'F', 'M', 'X'});
    private final JTextField tfTelefono = new JTextField(9);
    private final JTextField tfEmail = new JTextField(12);
    private final JTextField tfDomicilio = new JTextField(12);
    private final JComboBox<ObraSocial> cbObra = new JComboBox<>();
    private final JTextField tfAfiliado = new JTextField(9);
    private final JTextField tfPlan = new JTextField(12);

    public FormularioPaciente() {
        super(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        campo(g, 0, 0, "DNI:", tfDni);
        campo(g, 2, 0, "Apellido:", tfApellido);
        campo(g, 4, 0, "Nombre:", tfNombre);
        campo(g, 0, 1, "Nacimiento:", cfNac);
        campo(g, 2, 1, "Edad:", tfEdad);
        campo(g, 4, 1, "Sexo:", cbSexo);
        campo(g, 0, 2, "Telefono:", tfTelefono);
        campo(g, 2, 2, "Email:", tfEmail);
        campo(g, 4, 2, "Domicilio:", tfDomicilio);
        campo(g, 0, 3, "Obra social:", cbObra);
        campo(g, 2, 3, "N.° afiliado:", tfAfiliado);
        campo(g, 4, 3, "Plan:", tfPlan);

        // la edad no se carga: se calcula a partir de la fecha de nacimiento
        tfEdad.setEditable(false);
        tfEdad.setFocusable(false);
        tfEdad.setBackground(Estilo.PANEL);
        cfNac.alCambiar(this::calcularEdad);
        cbObra.addActionListener(e -> habilitarCobertura());
        cbObra.addItem(PARTICULAR);
    }

    private void campo(GridBagConstraints g, int x, int y, String etiqueta, JComponent c) {
        g.gridx = x; g.gridy = y; add(Estilo.etiqueta(etiqueta), g);
        g.gridx = x + 1; add(c, g);
    }

    /** Carga el combo con el padron de obras sociales leido de la base. */
    public void setObrasSociales(List<ObraSocial> obras) {
        Object seleccion = cbObra.getSelectedItem();
        cbObra.removeAllItems();
        cbObra.addItem(PARTICULAR);
        for (ObraSocial o : obras) cbObra.addItem(o);
        if (seleccion != null) cbObra.setSelectedItem(seleccion);
    }

    private void habilitarCobertura() {
        boolean particular = cbObra.getSelectedItem() == null || PARTICULAR.equals(cbObra.getSelectedItem());
        tfAfiliado.setEnabled(!particular);
        tfPlan.setEnabled(!particular);
        if (particular) { tfAfiliado.setText(""); tfPlan.setText(""); }
    }

    private void calcularEdad() {
        try {
            LocalDate f = cfNac.getFecha();
            if (f == null || f.isAfter(LocalDate.now())) { tfEdad.setText(""); return; }
            int edad = Period.between(f, LocalDate.now()).getYears();
            tfEdad.setText(edad == 1 ? "1 anio" : edad + " anios");
        } catch (IllegalArgumentException ex) {
            tfEdad.setText("");          // fecha incompleta o invalida
        }
    }

    /** Arma el DTO con lo ingresado. La validacion de negocio la hace el servicio. */
    public DatosPaciente getDatos() throws ValidacionException {
        LocalDate nac;
        try {
            nac = cfNac.getFecha();
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
        ObraSocial obra = (ObraSocial) cbObra.getSelectedItem();
        if (PARTICULAR.equals(obra)) obra = null;
        return new DatosPaciente(tfDni.getText(), tfApellido.getText(), tfNombre.getText(), nac,
                (Character) cbSexo.getSelectedItem(), tfDomicilio.getText(), tfTelefono.getText(),
                tfEmail.getText(), obra, tfAfiliado.getText(), tfPlan.getText());
    }

    /** Completa el formulario con un paciente existente (modo modificacion: el DNI no se edita). */
    public void cargar(Paciente p) {
        tfDni.setText(p.getDni());
        tfDni.setEditable(false);
        tfApellido.setText(p.getApellido());
        tfNombre.setText(p.getNombre());
        cfNac.setFecha(p.getFechaNacimiento());
        cbSexo.setSelectedItem(p.getSexo());
        tfTelefono.setText(nulo(p.getTelefono()));
        tfEmail.setText(nulo(p.getEmail()));
        tfDomicilio.setText(nulo(p.getDomicilio()));
        cbObra.setSelectedItem(p.getObraSocial() == null ? PARTICULAR : p.getObraSocial());
        tfAfiliado.setText(nulo(p.getNroAfiliado()));
        tfPlan.setText(nulo(p.getPlan()));
    }

    public void limpiar() {
        JTextField[] campos = {tfDni, tfApellido, tfNombre, tfTelefono, tfEmail, tfDomicilio, tfAfiliado, tfPlan};
        for (JTextField t : campos) t.setText("");          // ARREGLO de campos a limpiar
        cfNac.setFecha(null);
        cbSexo.setSelectedIndex(0);
        cbObra.setSelectedItem(PARTICULAR);
        tfDni.requestFocusInWindow();
    }

    private static String nulo(String s) { return s == null ? "" : s; }
}
