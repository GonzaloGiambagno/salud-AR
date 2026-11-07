package saludar.servicio;

import java.time.LocalDate;
import java.util.List;
import saludar.excepciones.DniDuplicadoException;
import saludar.excepciones.DniInvalidoException;
import saludar.excepciones.OperacionNoPermitidaException;
import saludar.excepciones.PacienteNoEncontradoException;
import saludar.excepciones.PersistenciaException;
import saludar.excepciones.ValidacionException;
import saludar.modelo.Alergia;
import saludar.modelo.ObraSocial;
import saludar.modelo.Paciente;
import saludar.modelo.Rol;
import saludar.persistencia.AlergiaDAO;
import saludar.persistencia.ObraSocialDAO;
import saludar.persistencia.PacienteDAO;
import saludar.seguridad.Sesion;
import saludar.util.Busqueda;
import saludar.util.Ordenamiento;

/**
 * Logica de negocio de pacientes (CU-02, CU-03, CU-06). Recibe los DAO por
 * constructor (INYECCION DE DEPENDENCIAS) y opera contra las INTERFACES, no
 * contra una implementacion concreta.
 */
public class PacienteService {

    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);

    private final PacienteDAO pacienteDAO;
    private final AlergiaDAO alergiaDAO;
    private final ObraSocialDAO obraSocialDAO;
    private final AuditoriaService auditoria;

    public PacienteService(PacienteDAO pacienteDAO, AlergiaDAO alergiaDAO,
                           ObraSocialDAO obraSocialDAO, AuditoriaService auditoria) {
        this.pacienteDAO = pacienteDAO;
        this.alergiaDAO = alergiaDAO;
        this.obraSocialDAO = obraSocialDAO;
        this.auditoria = auditoria;
    }

    // ---------------------------------------------------------------- alta / modificacion

    /** Alta de paciente con validaciones (RF-05 a RF-08). */
    public Paciente registrar(DatosPaciente d)
            throws DniInvalidoException, DniDuplicadoException, ValidacionException, PersistenciaException {
        String dni = d.dni() == null ? "" : d.dni().trim();
        if (!dni.matches("\\d{7,8}")) {
            throw new DniInvalidoException("El DNI debe contener 7 u 8 digitos numericos.");
        }
        if (pacienteDAO.existeDni(dni)) {
            throw new DniDuplicadoException(dni);                       // RF-07, RN-01
        }
        Paciente p = construir(dni, d);
        int id = pacienteDAO.insertar(p);
        auditoria.registrar("paciente", id, "ALTA", "Alta de paciente DNI " + dni);
        return p;
    }

    /** Modificacion de datos personales y de cobertura (RF-09). El DNI no se modifica. */
    public Paciente actualizar(Paciente existente, DatosPaciente d) throws ValidacionException, PersistenciaException {
        Paciente p = construir(existente.getDni(), d);
        p.setId(existente.getId());
        pacienteDAO.actualizar(p);
        auditoria.registrar("paciente", p.getId(), "MODIFICACION", "Modificacion de datos DNI " + p.getDni());
        return p;
    }

    /** Baja logica: se conserva la historia clinica (RF-10, RN-07). */
    public void darDeBaja(Paciente p) throws PersistenciaException {
        pacienteDAO.darDeBaja(p.getId());
        p.setActivo(false);
        auditoria.registrar("paciente", p.getId(), "BAJA_LOGICA", "Baja logica de paciente DNI " + p.getDni());
    }

    private Paciente construir(String dni, DatosPaciente d) throws ValidacionException {
        LocalDate nac = d.fechaNacimiento();
        if (nac == null) throw new ValidacionException("La fecha de nacimiento es obligatoria.");
        if (nac.isAfter(LocalDate.now())) throw new ValidacionException("La fecha de nacimiento no puede ser futura.");
        if (nac.isBefore(FECHA_MINIMA)) throw new ValidacionException("La fecha de nacimiento no es valida.");
        String email = vacioANulo(d.email());
        if (email != null && !email.matches(REGEX_EMAIL)) {
            throw new ValidacionException("El correo electronico no tiene un formato valido.");
        }
        Paciente p;
        try {
            p = new Paciente(dni, texto(d.apellido()), texto(d.nombre()), nac, d.sexo());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());               // apellido/nombre/sexo
        }
        p.setContacto(vacioANulo(d.domicilio()), vacioANulo(d.telefono()), email);
        p.setCobertura(d.obraSocial(), vacioANulo(d.nroAfiliado()), vacioANulo(d.plan()));
        return p;
    }

    private static String texto(String s) { return s == null ? null : s.trim(); }

    private static String vacioANulo(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    // ---------------------------------------------------------------- consultas

    /**
     * Padron de pacientes activos ordenado por apellido. La base devuelve un
     * ArrayList; se pasa a un ARREGLO para ordenarlo con el algoritmo propio.
     */
    public Paciente[] listarOrdenadosPorApellido() throws PersistenciaException {
        List<Paciente> lista = pacienteDAO.listarActivos();
        Paciente[] v = lista.toArray(new Paciente[0]);
        Ordenamiento.ordenarPorApellido(v);            // insercion
        return v;
    }

    /**
     * Busqueda por DNI: BUSQUEDA BINARIA sobre el arreglo de pacientes activos
     * ordenado por DNI. Devuelve el paciente con sus alergias cargadas.
     */
    public Paciente buscarPorDni(String dni) throws PacienteNoEncontradoException, PersistenciaException {
        String criterio = dni == null ? "" : dni.trim();
        if (!criterio.matches("\\d{1,9}")) throw new PacienteNoEncontradoException("DNI " + criterio);
        Paciente[] v = pacienteDAO.listarActivos().toArray(new Paciente[0]);
        Ordenamiento.ordenarPorDni(v);                 // seleccion
        Paciente p = Busqueda.binariaPorDni(v, criterio);
        if (p == null) throw new PacienteNoEncontradoException("DNI " + criterio);
        cargarAlergias(p);
        return p;
    }

    /** Busqueda por apellido resuelta en MySQL con LIKE (RF-11). */
    public List<Paciente> buscarPorApellido(String prefijo) throws PersistenciaException {
        return pacienteDAO.buscarPorApellido(prefijo == null ? "" : prefijo.trim());
    }

    /** Paciente por id con sus alergias (para la alerta al atender). */
    public Paciente obtener(int idPaciente) throws PacienteNoEncontradoException, PersistenciaException {
        Paciente p = pacienteDAO.buscarPorId(idPaciente);
        if (p == null) throw new PacienteNoEncontradoException("id " + idPaciente);
        cargarAlergias(p);
        return p;
    }

    public List<ObraSocial> obrasSociales() throws PersistenciaException {
        return obraSocialDAO.listar();
    }

    // ---------------------------------------------------------------- alergias

    private void cargarAlergias(Paciente p) throws PersistenciaException {
        p.setAlergias(alergiaDAO.listarPorPaciente(p.getId()));
    }

    /** Registro de una alergia: solo el medico (CU-06). */
    public void agregarAlergia(Paciente p, Alergia a)
            throws OperacionNoPermitidaException, ValidacionException, PersistenciaException {
        Sesion.getInstancia().exigirRol("registrar alergias", Rol.MEDICO);
        if (a.getSustancia() == null || a.getSustancia().isBlank()) {
            throw new ValidacionException("La sustancia es obligatoria.");
        }
        int id = alergiaDAO.insertar(p.getId(), a);
        p.agregarAlergia(a);
        auditoria.registrar("alergia", id, "ALTA",
                "Alergia a " + a.getSustancia() + " (" + a.getSeveridad() + ") - DNI " + p.getDni());
    }
}
