package saludar.app;

import javax.swing.SwingUtilities;
import saludar.controlador.HistoriaController;
import saludar.controlador.LoginController;
import saludar.controlador.PacienteController;
import saludar.controlador.PrincipalController;
import saludar.controlador.SalaEsperaController;
import saludar.modelo.Usuario;
import saludar.persistencia.mysql.MySQLDAOFactory;
import saludar.ui.Estilo;
import saludar.ui.LoginDialog;
import saludar.ui.PanelAtencion;
import saludar.ui.PanelHistoria;
import saludar.ui.PanelPacientes;
import saludar.ui.VentanaPrincipal;


public class MainSwing {

    public static void main(String[] args) {
        Estilo.aplicar();
        Servicios servicios = Servicios.crear(new MySQLDAOFactory());   // modelo sobre MySQL
        SwingUtilities.invokeLater(() -> iniciarSesion(servicios));
    }

    /** Muestra el login; al cerrar sesion se vuelve a esta pantalla. */
    private static void iniciarSesion(Servicios s) {
        LoginDialog vista = new LoginDialog();
        LoginController login = new LoginController(s.autenticacion(), vista);
        vista.mostrar();
        Usuario u = login.getUsuario();
        if (u == null) {
            System.exit(0);
        }
        abrirVentanaPrincipal(s, u);
    }

    private static void abrirVentanaPrincipal(Servicios s, Usuario u) {
        PanelPacientes vistaPacientes = new PanelPacientes();
        new PacienteController(s.pacientes(), vistaPacientes);

        PanelAtencion vistaSala = new PanelAtencion();
        new SalaEsperaController(s.sala(), s.pacientes(), s.consultas(), s.catalogo(), vistaSala);

        PanelHistoria vistaHistoria = new PanelHistoria();
        new HistoriaController(s.pacientes(), s.consultas(), s.exportador(), vistaHistoria);

        VentanaPrincipal ventana = new VentanaPrincipal(u, vistaPacientes, vistaSala, vistaHistoria);
        new PrincipalController(s.autenticacion(), ventana, () -> iniciarSesion(s));
        ventana.mostrar();
    }
}
