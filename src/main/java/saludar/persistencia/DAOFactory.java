package saludar.persistencia;

/**
 * Patron ABSTRACT FACTORY: crea la familia completa de DAO de una misma
 * tecnologia. La aplicacion solo conoce esta interfaz; cambiar MySQL por
 * otro motor implica escribir otra fabrica, sin tocar servicios ni interfaz.
 */
public interface DAOFactory {
    PacienteDAO pacientes();
    AlergiaDAO alergias();
    ObraSocialDAO obrasSociales();
    Cie10DAO cie10();
    ConsultaDAO consultas();
    IngresoSalaDAO ingresosSala();
    UsuarioDAO usuarios();
    AuditoriaDAO auditoria();
}
