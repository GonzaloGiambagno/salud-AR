package saludar.ui;

import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;

import saludar.app.DatosDemo;
import saludar.modelo.Consulta;
import saludar.modelo.Diagnostico;
import saludar.modelo.Medicacion;
import saludar.modelo.Paciente;
import saludar.modelo.Prioridad;
import saludar.modelo.Profesional;
import saludar.persistencia.PacienteRepositorio;
import saludar.persistencia.PacienteRepositorioMemoria;
import saludar.servicio.ConsultaService;
import saludar.servicio.PacienteService;
import saludar.servicio.SalaDeEspera;

/** Ventana principal de Salud-AR (Swing). Reutiliza la capa de servicios. */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        super("Salud-AR  -  Historia Clinica Electronica");

        PacienteRepositorio repo = new PacienteRepositorioMemoria();
        PacienteService pacienteService = new PacienteService(repo);
        ConsultaService consultaService = new ConsultaService();
        SalaDeEspera sala = new SalaDeEspera();
        Profesional medico = DatosDemo.cargar(pacienteService);
        try {
            LocalDateTime ahora = LocalDateTime.now();
            sala.ingresar(pacienteService.buscarPorDni("30987654"), Prioridad.BAJA, ahora.minusMinutes(25));
            sala.ingresar(pacienteService.buscarPorDni("28455120"), Prioridad.ALTA, ahora.minusMinutes(12));
            sala.ingresar(pacienteService.buscarPorDni("25111222"), Prioridad.MEDIA, ahora.minusMinutes(4));
        } catch (Exception ignore) { }
        try {
            Paciente maria = pacienteService.buscarPorDni("28455120");
            Consulta c1 = consultaService.crearBorrador(maria, medico, "Control anual", "Paciente estable, sin cambios.");
            c1.agregarDiagnostico(new Diagnostico("Z00.0", "Examen medico general", true));
            consultaService.cerrar(c1);
            Consulta c2 = consultaService.crearBorrador(maria, medico, "Cefalea de 3 dias", "Lucida, afebril, sin foco neurologico.");
            c2.agregarDiagnostico(new Diagnostico("R51", "Cefalea", true));
            c2.agregarMedicacion(new Medicacion("Paracetamol", "500 mg", "c/8 h", "3 dias"));
            consultaService.cerrar(c2);
        } catch (Exception ignore) { }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));

        // ---- Encabezado ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Estilo.DARK);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JPanel izq = new JPanel(); izq.setOpaque(false);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Salud-AR");
        titulo.setFont(Estilo.H1); titulo.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Sistema de Historia Clinica Electronica");
        sub.setForeground(new Color(0xbf, 0xe3, 0xdc)); sub.setFont(Estilo.BASE);
        izq.add(titulo); izq.add(sub);
        JLabel prof = new JLabel(medico.getResumen() + "   ");
        prof.setForeground(new Color(0xd3, 0xe6, 0xe2)); prof.setFont(Estilo.H3);
        header.add(izq, BorderLayout.WEST);
        header.add(prof, BorderLayout.EAST);

        // ---- Pestanias ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Estilo.H2);
        tabs.addTab("  Pacientes  ", new PanelPacientes(pacienteService));
        tabs.addTab("  Sala de espera  ",new PanelAtencion(pacienteService, consultaService, sala, medico));
        tabs.addTab("  Historia clinica  ", new PanelHistoria(pacienteService, consultaService));

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }
}
