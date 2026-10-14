package saludar.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import saludar.modelo.Paciente;

/**
 * Implementacion REAL con JDBC sobre MySQL (misma interfaz que la version en
 * memoria: POLIMORFISMO). Usa PreparedStatement para prevenir inyeccion SQL
 * y opera sobre el esquema "saludar" definido en el TP2 (01_schema.sql).
 *
 * No se ejecuta en la demo de consola para no exigir un MySQL en linea, pero
 * queda incluida y documentada en el repositorio como camino de produccion.
 */
public class PacienteRepositorioMySQL implements PacienteRepositorio {

    private final String url;
    private final String usuario;
    private final String clave;

    public PacienteRepositorioMySQL(String url, String usuario, String clave) {
        this.url = url;         // ej: jdbc:mysql://localhost:3306/saludar?useSSL=true
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public Paciente guardar(Paciente p) {
        String sql = "INSERT INTO paciente (dni, apellido, nombre, sexo, activo) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getDni());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getNombre());
            ps.setString(4, String.valueOf(p.getSexo()));
            ps.setBoolean(5, p.isActivo());
            ps.executeUpdate();
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paciente> listar() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT dni, apellido, nombre FROM paciente WHERE activo = TRUE "
                   + "ORDER BY apellido";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Paciente(rs.getString("dni"),
                                       rs.getString("apellido"),
                                       rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pacientes: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Paciente buscarPorDni(String dni) {
        String sql = "SELECT dni, apellido, nombre FROM paciente WHERE dni = ?";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Paciente(rs.getString("dni"),
                                        rs.getString("apellido"),
                                        rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en la busqueda: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean existeDni(String dni) {
        return buscarPorDni(dni) != null;
    }

    @Override
    public int cantidad() {
        return listar().size();
    }
}
