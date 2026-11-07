package saludar.persistencia.mysql;

import saludar.persistencia.AlergiaDAO;
import saludar.persistencia.AuditoriaDAO;
import saludar.persistencia.Cie10DAO;
import saludar.persistencia.ConsultaDAO;
import saludar.persistencia.DAOFactory;
import saludar.persistencia.IngresoSalaDAO;
import saludar.persistencia.ObraSocialDAO;
import saludar.persistencia.PacienteDAO;
import saludar.persistencia.UsuarioDAO;

/** Fabrica concreta de los DAO sobre MySQL. Los DAO no guardan estado: se crean una vez. */
public class MySQLDAOFactory implements DAOFactory {

    private final PacienteDAO pacientes = new PacienteDAOMySQL();
    private final AlergiaDAO alergias = new AlergiaDAOMySQL();
    private final ObraSocialDAO obrasSociales = new ObraSocialDAOMySQL();
    private final Cie10DAO cie10 = new Cie10DAOMySQL();
    private final ConsultaDAO consultas = new ConsultaDAOMySQL();
    private final IngresoSalaDAO ingresosSala = new IngresoSalaDAOMySQL();
    private final UsuarioDAO usuarios = new UsuarioDAOMySQL();
    private final AuditoriaDAO auditoria = new AuditoriaDAOMySQL();

    @Override public PacienteDAO pacientes() { return pacientes; }
    @Override public AlergiaDAO alergias() { return alergias; }
    @Override public ObraSocialDAO obrasSociales() { return obrasSociales; }
    @Override public Cie10DAO cie10() { return cie10; }
    @Override public ConsultaDAO consultas() { return consultas; }
    @Override public IngresoSalaDAO ingresosSala() { return ingresosSala; }
    @Override public UsuarioDAO usuarios() { return usuarios; }
    @Override public AuditoriaDAO auditoria() { return auditoria; }
}
