package saludar.persistencia.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.ObraSocial;
import saludar.modelo.Paciente;
import saludar.persistencia.PacienteDAO;

/** Implementacion MySQL del DAO de pacientes (tablas paciente y cobertura, vista v_paciente). */
public class PacienteDAOMySQL extends DAOBaseMySQL<Paciente> implements PacienteDAO {

    private static final String SELECT_BASE = "SELECT * FROM v_paciente ";

    private static final String INSERT_PACIENTE =
            "INSERT INTO paciente (dni, apellido, nombre, fecha_nac, sexo, domicilio, telefono, email) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_PACIENTE =
            "UPDATE paciente SET apellido = ?, nombre = ?, fecha_nac = ?, sexo = ?, "
          + "domicilio = ?, telefono = ?, email = ? WHERE id_paciente = ?";
    // Alta o modificacion de la cobertura en una sola sentencia (clave unica id_paciente)
    private static final String UPSERT_COBERTURA =
            "INSERT INTO cobertura (id_paciente, id_obra_social, nro_afiliado, plan) VALUES (?, ?, ?, ?) AS nueva "
          + "ON DUPLICATE KEY UPDATE id_obra_social = nueva.id_obra_social, "
          + "nro_afiliado = nueva.nro_afiliado, plan = nueva.plan";
    private static final String DELETE_COBERTURA = "DELETE FROM cobertura WHERE id_paciente = ?";

    @Override
    protected Paciente mapear(ResultSet rs) throws SQLException {
        return mapearPaciente(rs);
    }

    /** Mapeo reutilizable por otros DAO que leen columnas de v_paciente. */
    static Paciente mapearPaciente(ResultSet rs) throws SQLException {
        Paciente p = new Paciente(rs.getInt("id_paciente"), rs.getString("dni"),
                rs.getString("apellido"), rs.getString("nombre"),
                rs.getObject("fecha_nac", LocalDate.class), rs.getString("sexo").charAt(0));
        p.setContacto(rs.getString("domicilio"), rs.getString("telefono"), rs.getString("email"));
        Integer idObra = enteroONulo(rs, "id_obra_social");
        ObraSocial obra = idObra == null ? null : new ObraSocial(idObra, rs.getString("obra_social"), null);
        p.setCobertura(obra, rs.getString("nro_afiliado"), rs.getString("plan"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    /** Alta del paciente y de su cobertura en una transaccion (RNF-10). */
    @Override
    public int insertar(Paciente p) throws PersistenciaException {
        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                int id = insertar(con, INSERT_PACIENTE, p.getDni(), p.getApellido(), p.getNombre(),
                        p.getFechaNacimiento(), p.getSexo(), p.getDomicilio(), p.getTelefono(), p.getEmail());
                guardarCobertura(con, id, p);
                con.commit();
                p.setId(id);
                return id;
            } catch (SQLException e) {
                deshacer(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "registrar el paciente");
        }
    }

    @Override
    public void actualizar(Paciente p) throws PersistenciaException {
        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                int filas = ejecutar(con, UPDATE_PACIENTE, p.getApellido(), p.getNombre(),
                        p.getFechaNacimiento(), p.getSexo(), p.getDomicilio(), p.getTelefono(),
                        p.getEmail(), p.getId());
                if (filas == 0) throw new SQLException("No existe el paciente con id " + p.getId());
                guardarCobertura(con, p.getId(), p);
                con.commit();
            } catch (SQLException e) {
                deshacer(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "actualizar el paciente");
        }
    }

    private static void guardarCobertura(Connection con, int idPaciente, Paciente p) throws SQLException {
        if (p.getObraSocial() == null) {
            ejecutar(con, DELETE_COBERTURA, idPaciente);            // pasa a ser particular
        } else {
            ejecutar(con, UPSERT_COBERTURA, idPaciente, p.getObraSocial().getId(),
                    p.getNroAfiliado(), p.getPlan());
        }
    }

    @Override
    public void darDeBaja(int idPaciente) throws PersistenciaException {
        int filas = ejecutarActualizacion(
                "UPDATE paciente SET activo = FALSE WHERE id_paciente = ? AND activo = TRUE", idPaciente);
        if (filas == 0) {
            throw new PersistenciaException("El paciente no existe o ya estaba dado de baja.");
        }
    }

    @Override
    public Paciente buscarPorId(int idPaciente) throws PersistenciaException {
        return consultarUno(SELECT_BASE + "WHERE id_paciente = ?", idPaciente);
    }

    @Override
    public Paciente buscarPorDni(String dni) throws PersistenciaException {
        return consultarUno(SELECT_BASE + "WHERE dni = ?", dni);
    }

    @Override
    public boolean existeDni(String dni) throws PersistenciaException {
        Long cantidad = consultarValor("SELECT COUNT(*) FROM paciente WHERE dni = ?", Long.class, dni);
        return cantidad != null && cantidad > 0;
    }

    @Override
    public List<Paciente> listarActivos() throws PersistenciaException {
        return consultarLista(SELECT_BASE + "WHERE activo = TRUE ORDER BY id_paciente");
    }

    @Override
    public List<Paciente> buscarPorApellido(String prefijo) throws PersistenciaException {
        return consultarLista(SELECT_BASE + "WHERE activo = TRUE AND apellido LIKE ? ORDER BY apellido, nombre",
                prefijo + "%");
    }
}
