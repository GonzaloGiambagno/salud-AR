package saludar.app;

import java.time.LocalDate;
import saludar.modelo.Alergia;
import saludar.modelo.Paciente;
import saludar.modelo.Profesional;
import saludar.modelo.Severidad;
import saludar.servicio.PacienteService;

/** Carga de datos de ejemplo para la demostracion. */
public final class DatosDemo {
    private DatosDemo() { }

    public static Profesional cargar(PacienteService pacienteService) {
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

        return new Profesional(1, "Perez", "Juan", "MP-12345", "Clinica Medica");
    }
}
