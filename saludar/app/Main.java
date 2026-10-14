package saludar.app;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import saludar.excepciones.ConsultaCerradaException;
import saludar.excepciones.DniDuplicadoException;
import saludar.excepciones.DniInvalidoException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.modelo.Alergia;
import saludar.modelo.Consulta;
import saludar.modelo.Paciente;
import saludar.modelo.Profesional;
import saludar.modelo.Severidad;
import saludar.persistencia.PacienteRepositorio;
import saludar.persistencia.PacienteRepositorioMemoria;
import saludar.servicio.Auditoria;
import saludar.servicio.ConsultaService;
import saludar.servicio.PacienteService;
import saludar.servicio.SalaDeEspera;

/**
 * Punto de entrada del prototipo Salud-AR (consola).
 * Ofrece un MENU DE SELECCION y coordina los servicios del sistema.
 */
public class Main {

    private final Scanner sc = new Scanner(System.in);
    private final PacienteRepositorio repo = new PacienteRepositorioMemoria();
    private final PacienteService pacienteService = new PacienteService(repo);
    private final ConsultaService consultaService = new ConsultaService();
    private final SalaDeEspera sala = new SalaDeEspera();
    private final Auditoria auditoria = new Auditoria();
    private Profesional medico;

    public static void main(String[] args) {
        new Main().ejecutar();
    }

    private void ejecutar() {
        cargarDatosDemo();
        System.out.println("============================================================");
        System.out.println("     Salud-AR  -  Historia Clinica Electronica (prototipo)   ");
        System.out.println("============================================================");

        int opcion = -1;
        while (opcion != 0) {                 // ESTRUCTURA DE REPETICION (while)
            mostrarMenu();
            opcion = leerEntero("Opcion: ");
            System.out.println();
            try {
                switch (opcion) {             // ESTRUCTURA CONDICIONAL (switch)
                    case 1 -> registrarPaciente();
                    case 2 -> listarPacientes();
                    case 3 -> buscarPaciente();
                    case 4 -> encolarPaciente();
                    case 5 -> atenderSiguiente();
                    case 6 -> verHistoriaClinica();
                    case 7 -> verAlertas();
                    case 8 -> verAuditoria();
                    case 0 -> System.out.println("Cerrando el sistema. Hasta luego.");
                    default -> System.out.println("[!] Opcion invalida. Intente nuevamente.");
                }
            } catch (DniDuplicadoException | DniInvalidoException
                     | PacienteNoEncontradoException | ConsultaCerradaException e) {
                // MANEJO DE EXCEPCIONES de negocio
                System.out.println("[ERROR] " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void mostrarMenu() {
        System.out.println("------------------------------------------------------------");
        System.out.println(" 1) Registrar paciente         5) Atender siguiente");
        System.out.println(" 2) Listar pacientes           6) Ver historia clinica");
        System.out.println(" 3) Buscar paciente por DNI     7) Ver alertas de alergias");
        System.out.println(" 4) Enviar a sala de espera     8) Ver auditoria");
        System.out.println(" 0) Salir");
        System.out.println("------------------------------------------------------------");
    }

    // --- Opcion 1 ---
    private void registrarPaciente() throws DniDuplicadoException, DniInvalidoException {
        String dni = leerTexto("DNI (7-8 digitos): ");
        String apellido = leerTexto("Apellido: ");
        String nombre = leerTexto("Nombre: ");
        Paciente p = pacienteService.registrar(dni, apellido, nombre);
        auditoria.registrar("Alta de paciente: " + p.getNombreCompleto());
        System.out.println("[OK] Paciente registrado -> " + p.getResumen());
    }

    // --- Opcion 2 ---  (POLIMORFISMO: cada Paciente se imprime con su getResumen)
    private void listarPacientes() {
        List<Paciente> lista = pacienteService.listarOrdenadosPorApellido();
        if (lista.isEmpty()) { System.out.println("No hay pacientes cargados."); return; }
        System.out.println("Pacientes (ordenados por apellido): " + lista.size());
        for (Paciente p : lista) {            // for-each
            System.out.println("  - " + p);
        }
    }

    // --- Opcion 3 ---
    private void buscarPaciente() throws PacienteNoEncontradoException {
        String dni = leerTexto("DNI a buscar: ");
        Paciente p = pacienteService.buscarPorDni(dni);   // busqueda binaria
        System.out.println("[OK] Encontrado -> " + p.getResumen());
    }

    // --- Opcion 4 ---
    private void encolarPaciente() throws PacienteNoEncontradoException {
        String dni = leerTexto("DNI del paciente a enviar a sala: ");
        Paciente p = pacienteService.buscarPorDni(dni);
        if (sala.estaEnSala(p)) {
            System.out.println("[!] " + p.getNombreCompleto() + " ya se encuentra en la sala de espera.");
            return;
        }
        sala.encolar(p);
        auditoria.registrar("Ingreso a sala de espera: " + p.getNombreCompleto());
        System.out.println("[OK] " + p.getNombreCompleto() + " en espera. En cola: " + sala.enEspera());
    }

    // --- Opcion 5 ---
    private void atenderSiguiente() throws ConsultaCerradaException {
        if (!sala.hayPacientes()) { System.out.println("La sala de espera esta vacia."); return; }
        Paciente p = sala.atenderSiguiente();            // desencola (FIFO)
        System.out.println("Atendiendo a: " + p.getNombreCompleto());
        if (p.tieneAlergias()) {
            System.out.println("  /!\\ ALERGIAS: " + p.getAlergias());
        }
        String motivo = leerTexto("Motivo de la consulta: ");
        Consulta c = consultaService.registrarConsulta(p, medico, motivo, "Evaluacion registrada.");
        auditoria.registrar("Consulta #" + c.getId() + " registrada a " + p.getNombreCompleto());
        System.out.println("[OK] Consulta #" + c.getId() + " [" + c.getEstado() + "].");
        // Demostracion de la inviolabilidad: intentar editar una consulta cerrada.
        consultaService.editarMotivo(c, "cambio no permitido");
    }

    // --- Opcion 6 ---
    private void verHistoriaClinica() throws PacienteNoEncontradoException {
        String dni = leerTexto("DNI del paciente: ");
        Paciente p = pacienteService.buscarPorDni(dni);
        List<Consulta> hc = consultaService.historiaDe(p);
        System.out.println("Historia clinica de " + p.getNombreCompleto() + ":");
        if (hc.isEmpty()) { System.out.println("  (sin consultas registradas)"); return; }
        for (Consulta c : hc) {
            System.out.printf("  #%d  %s  motivo: %s  dx: %s%n",
                    c.getId(), c.getEstado(), c.getMotivo(), c.getDiagnosticoPrincipal());
        }
    }

    // --- Opcion 7 ---
    private void verAlertas() throws PacienteNoEncontradoException {
        String dni = leerTexto("DNI del paciente: ");
        Paciente p = pacienteService.buscarPorDni(dni);
        if (p.tieneAlergias()) {
            System.out.println("/!\\ ALERTA DE ALERGIAS de " + p.getNombreCompleto() + ":");
            for (Alergia a : p.getAlergias()) {
                System.out.println("   - " + a.getSustancia() + " | " + a.getTipoReaccion()
                        + " | " + a.getSeveridad());
            }
        } else {
            System.out.println(p.getNombreCompleto() + " no tiene alergias registradas.");
        }
    }

    // --- Opcion 8 ---  (PILA)
    private void verAuditoria() {
        if (auditoria.vacia()) { System.out.println("Sin eventos de auditoria."); return; }
        System.out.println("Ultimos eventos (mas reciente primero):");
        for (String evento : auditoria.ultimos(5)) {
            System.out.println("  " + evento);
        }
    }

    // ---------- utilidades de entrada (manejo de excepciones de formato) ----------
    private int leerEntero(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;   // entrada no numerica -> opcion invalida
        }
    }

    private String leerTexto(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ---------- datos de demostracion ----------
    private void cargarDatosDemo() {
        medico = new Profesional(1, "Perez", "Juan", "MP-12345", "Clinica Medica");

        Paciente maria = new Paciente(0, "28455120", "Gimenez", "Maria",
                LocalDate.of(1979, 4, 12), 'F', "OSDE", "210-445512");
        maria.agregarAlergia(new Alergia("Penicilina", "Anafilaxia", Severidad.GRAVE));
        maria.agregarAlergia(new Alergia("AINE", "Urticaria", Severidad.MODERADA));

        Paciente carlos = new Paciente(0, "30987654", "Lopez", "Carlos",
                LocalDate.of(1984, 11, 3), 'M', "Swiss Medical", "SW-7788");
        Paciente ana = new Paciente(0, "25111222", "Acosta", "Ana",
                LocalDate.of(1972, 1, 20), 'F', "PAMI", "PAMI-9001");

        pacienteService.registrar(maria);
        pacienteService.registrar(carlos);
        pacienteService.registrar(ana);
        auditoria.registrar("Sistema iniciado con 3 pacientes de ejemplo.");
    }
}
