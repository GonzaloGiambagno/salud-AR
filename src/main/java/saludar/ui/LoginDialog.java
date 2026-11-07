package saludar.ui;

import java.awt.*;
import javax.swing.*;
import saludar.controlador.LoginController;
import saludar.controlador.LoginVista;

/** Vista de inicio de sesion (CU-01). La validacion la resuelve LoginController. */
public class LoginDialog extends JDialog implements LoginVista {

    private final JTextField tfUsuario = new JTextField(16);
    private final JPasswordField pfClave = new JPasswordField(16);
    private final JLabel lblError = new JLabel(" ");
    private LoginController controlador;

    public LoginDialog() {
        super((Frame) null, "Salud-AR - Iniciar sesion", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setBackground(Estilo.DARK);
        marca.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel titulo = new JLabel("Salud-AR");
        titulo.setFont(Estilo.H1);
        titulo.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Historia Clinica Electronica");
        sub.setFont(Estilo.BASE);
        sub.setForeground(new Color(0xbf, 0xe3, 0xdc));
        marca.add(titulo);
        marca.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilo.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 8, 22));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 4, 5, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; form.add(Estilo.etiqueta("Usuario:"), g);
        g.gridx = 1; form.add(tfUsuario, g);
        g.gridx = 0; g.gridy = 1; form.add(Estilo.etiqueta("Contrasenia:"), g);
        g.gridx = 1; form.add(pfClave, g);
        lblError.setForeground(Estilo.RED);
        lblError.setFont(Estilo.BASE);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; form.add(lblError, g);

        JButton bIngresar = Estilo.boton("Ingresar", true);
        JButton bSalir = Estilo.boton("Salir", false);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acciones.setBackground(Estilo.PANEL);
        acciones.setBorder(BorderFactory.createEmptyBorder(0, 22, 18, 22));
        acciones.add(bSalir);
        acciones.add(bIngresar);

        JPanel cont = new JPanel(new BorderLayout());
        cont.add(marca, BorderLayout.NORTH);
        cont.add(form, BorderLayout.CENTER);
        cont.add(acciones, BorderLayout.SOUTH);
        setContentPane(cont);

        // la vista solo reenvia el evento al controlador
        bIngresar.addActionListener(e -> controlador.ingresar(tfUsuario.getText(), pfClave.getPassword()));
        bSalir.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(bIngresar);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    @Override public void setControlador(LoginController controlador) { this.controlador = controlador; }

    @Override public void mostrarErrorCredenciales(String mensaje) {
        lblError.setText(mensaje);
        pfClave.setText("");
        pfClave.requestFocusInWindow();
    }

    @Override public void cerrar() { dispose(); }

    /** Muestra el dialogo (modal) hasta que se inicia sesion o se cancela. */
    public void mostrar() { setVisible(true); }

    @Override public void mostrarInfo(String titulo, String mensaje) { Mensajes.info(this, titulo, mensaje); }
    @Override public void mostrarAviso(String titulo, String mensaje) { Mensajes.aviso(this, titulo, mensaje); }
    @Override public void mostrarError(String titulo, String mensaje) { Mensajes.error(this, titulo, mensaje); }
    @Override public boolean confirmar(String titulo, String pregunta) { return Mensajes.confirmar(this, titulo, pregunta); }
}
