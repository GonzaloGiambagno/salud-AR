package saludar.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import saludar.controlador.PrincipalController;
import saludar.controlador.PrincipalVista;
import saludar.modelo.Usuario;

/** Vista principal: encabezado con el usuario en sesion y las tres pestanias. */
public class VentanaPrincipal extends JFrame implements PrincipalVista {

    private final JTabbedPane tabs = new JTabbedPane();
    private PrincipalController controlador;

    public VentanaPrincipal(Usuario usuario, PanelVista... pestanias) {
        super("Salud-AR  -  Historia Clinica Electronica");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(1040, 720));

        // ---- Encabezado ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Estilo.DARK);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JPanel izq = new JPanel();
        izq.setOpaque(false);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Salud-AR");
        titulo.setFont(Estilo.H1);
        titulo.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Sistema de Historia Clinica Electronica");
        sub.setForeground(new Color(0xbf, 0xe3, 0xdc));
        sub.setFont(Estilo.BASE);
        izq.add(titulo);
        izq.add(sub);

        String cuenta = usuario.getNombreUsuario() + " (" + usuario.getRol() + ")";
        JLabel lblUsuario = new JLabel((usuario.getProfesional() != null
                ? usuario.getProfesional().getResumen() + "   |   " + cuenta : cuenta) + "   ");
        lblUsuario.setForeground(new Color(0xd3, 0xe6, 0xe2));
        lblUsuario.setFont(Estilo.H3);
        JButton bSalir = Estilo.boton("Cerrar sesion", false);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        der.setOpaque(false);
        der.add(lblUsuario);
        der.add(bSalir);
        header.add(izq, BorderLayout.WEST);
        header.add(der, BorderLayout.EAST);

        // ---- Pestanias: se tratan por la interfaz Refrescable ----
        String[] titulos = {"  Pacientes  ", "  Sala de espera  ", "  Historia clinica  "};
        tabs.setFont(Estilo.H2);
        for (int i = 0; i < pestanias.length; i++) tabs.addTab(titulos[i], pestanias[i]);
        tabs.addChangeListener(e -> ((Refrescable) tabs.getSelectedComponent()).refrescar());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);

        bSalir.addActionListener(e -> controlador.cerrarSesion());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { controlador.salir(); }
        });
    }

    @Override public void setControlador(PrincipalController controlador) { this.controlador = controlador; }

    /** Muestra la ventana y carga los datos de la pestania inicial. */
    public void mostrar() {
        setVisible(true);
        ((Refrescable) tabs.getSelectedComponent()).refrescar();
    }

    @Override public void cerrar() { dispose(); }

    @Override public void mostrarInfo(String titulo, String mensaje) { Mensajes.info(this, titulo, mensaje); }
    @Override public void mostrarAviso(String titulo, String mensaje) { Mensajes.aviso(this, titulo, mensaje); }
    @Override public void mostrarError(String titulo, String mensaje) { Mensajes.error(this, titulo, mensaje); }
    @Override public boolean confirmar(String titulo, String pregunta) { return Mensajes.confirmar(this, titulo, pregunta); }
}
