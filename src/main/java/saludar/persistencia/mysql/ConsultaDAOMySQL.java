package saludar.persistencia.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import saludar.excepciones.PersistenciaException;
import saludar.modelo.Consulta;
import saludar.modelo.Diagnostico;
import saludar.modelo.EstadoConsulta;
import saludar.modelo.Medicacion;
import saludar.modelo.Paciente;
import saludar.modelo.Profesional;
import saludar.persistencia.ConsultaDAO;

public class ConsultaDAOMySQL extends DAOBaseMySQL<Consulta> implements ConsultaDAO {

    private static final String INSERT_CONSULTA =
            "INSERT INTO consulta (id_paciente, id_profesional, fecha_hora, motivo, evaluacion, "
          + "indicaciones, estado, id_consulta_rectificada) VALUES (?, ?, ?, ?, ?, ?, 'BORRADOR', ?)";
    private static final String INSERT_DIAGNOSTICO =
            "INSERT INTO diagnostico (id_consulta, codigo_cie10, principal) VALUES (?, ?, ?)";
    private static final String INSERT_MEDICACION =
            "INSERT INTO medicacion (id_consulta, farmaco, dosis, frecuencia, duracion) VALUES (?, ?, ?, ?, ?)";
    private static final String CERRAR_CONSULTA =
            "UPDATE consulta SET estado = 'CERRADA' WHERE id_consulta = ? AND estado = 'BORRADOR'";
    private static final String FINALIZAR_INGRESO =
            "UPDATE ingreso_sala SET estado = 'FINALIZADO', fecha_hora_fin = NOW(), id_consulta = ? "
          + "WHERE id_ingreso = ? AND estado <> 'FINALIZADO'";
    private static final String INSERT_AUDITORIA =
            "INSERT INTO auditoria (id_usuario, entidad, id_registro, accion, detalle) "
          + "VALUES (?, 'consulta', ?, 'CIERRE', ?)";

    private static final String SELECT_HISTORIA =
            "SELECT c.id_consulta, c.fecha_hora, c.motivo, c.evaluacion, c.indicaciones, c.estado, "
          + "c.id_consulta_rectificada, pr.id_profesional, pr.apellido AS prof_apellido, "
          + "pr.nombre AS prof_nombre, pr.matricula, pr.especialidad, v.* "
          + "FROM consulta c "
          + "JOIN profesional pr ON pr.id_profesional = c.id_profesional "
          + "JOIN v_paciente v   ON v.id_paciente = c.id_paciente "
          + "WHERE c.id_paciente = ? ORDER BY c.fecha_hora DESC, c.id_consulta DESC";
    private static final String SELECT_DIAGNOSTICOS =
            "SELECT d.codigo_cie10, ci.descripcion, d.principal FROM diagnostico d "
          + "JOIN cie10 ci ON ci.codigo_cie10 = d.codigo_cie10 "
          + "WHERE d.id_consulta = ? ORDER BY d.principal DESC, d.id_diagnostico";
    private static final String SELECT_MEDICACION =
            "SELECT farmaco, dosis, frecuencia, duracion FROM medicacion WHERE id_consulta = ? ORDER BY id_medicacion";

    @Override
    protected Consulta mapear(ResultSet rs) throws SQLException {
        Profesional prof = new Profesional(rs.getInt("id_profesional"), rs.getString("prof_apellido"),
                rs.getString("prof_nombre"), rs.getString("matricula"), rs.getString("especialidad"));
        Paciente pac = PacienteDAOMySQL.mapearPaciente(rs);
        Consulta c = new Consulta(rs.getInt("id_consulta"), pac, prof,
                rs.getObject("fecha_hora", LocalDateTime.class), rs.getString("motivo"),
                EstadoConsulta.valueOf(rs.getString("estado")));
        c.setEvaluacion(rs.getString("evaluacion"));
        c.setIndicaciones(rs.getString("indicaciones"));
        c.setIdConsultaRectificada(enteroONulo(rs, "id_consulta_rectificada"));
        return c;
    }

    /**
     * Transaccion de varias tablas (RNF-10): consulta + diagnosticos +
     * medicacion + cierre + fin del ingreso a sala + auditoria. Si cualquier
     * paso falla se hace ROLLBACK y no queda nada a medias.
     */
    @Override
    public int registrar(Consulta c, Integer idIngresoSala, Integer idUsuario) throws PersistenciaException {
        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                int id = insertar(con, INSERT_CONSULTA, c.getPaciente().getId(), c.getProfesional().getId(),
                        c.getFechaHora(), c.getMotivo(), c.getEvaluacion(), c.getIndicaciones(),
                        c.getIdConsultaRectificada());
                for (Diagnostico d : c.getDiagnosticos()) {
                    ejecutar(con, INSERT_DIAGNOSTICO, id, d.getCodigoCie10(), d.isPrincipal());
                }
                for (Medicacion m : c.getMedicaciones()) {
                    ejecutar(con, INSERT_MEDICACION, id, m.getFarmaco(), m.getDosis(),
                            m.getFrecuencia(), m.getDuracion());
                }
                ejecutar(con, CERRAR_CONSULTA, id);
                if (idIngresoSala != null && ejecutar(con, FINALIZAR_INGRESO, id, idIngresoSala) == 0) {
                    throw new SQLException("El ingreso a sala #" + idIngresoSala + " ya fue finalizado.");
                }
                ejecutar(con, INSERT_AUDITORIA, idUsuario, id, "Consulta cerrada - Dx " + c.getDiagnosticoPrincipal());
                con.commit();
                c.setId(id);
                c.cerrar();
                return id;
            } catch (SQLException e) {
                deshacer(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "registrar la consulta");
        }
    }

    @Override
    public List<Consulta> listarPorPaciente(Paciente p) throws PersistenciaException {
        List<Consulta> historia = consultarLista(SELECT_HISTORIA, p.getId());
        if (historia.isEmpty()) return historia;
        // Detalle de cada consulta: se reutilizan dos sentencias preparadas
        // sobre una misma conexion, cambiando solo el parametro.
        try (Connection con = conectar();
             PreparedStatement psDiag = con.prepareStatement(SELECT_DIAGNOSTICOS);
             PreparedStatement psMed = con.prepareStatement(SELECT_MEDICACION)) {
            for (Consulta c : historia) {
                psDiag.setInt(1, c.getId());
                try (ResultSet rs = psDiag.executeQuery()) {
                    while (rs.next()) {
                        c.agregarDiagnostico(new Diagnostico(rs.getString("codigo_cie10"),
                                rs.getString("descripcion"), rs.getBoolean("principal")));
                    }
                }
                psMed.setInt(1, c.getId());
                try (ResultSet rs = psMed.executeQuery()) {
                    while (rs.next()) {
                        c.agregarMedicacion(new Medicacion(rs.getString("farmaco"), rs.getString("dosis"),
                                rs.getString("frecuencia"), rs.getString("duracion")));
                    }
                }
            }
        } catch (SQLException e) {
            throw traducir(e, "leer la historia clinica");
        }
        return historia;
    }

    @Override
    public void actualizarMotivo(int idConsulta, String motivo) throws PersistenciaException {
        // Si la consulta esta cerrada, el trigger trg_consulta_inviolable rechaza el UPDATE
        ejecutarActualizacion("UPDATE consulta SET motivo = ? WHERE id_consulta = ?", motivo, idConsulta);
    }
}
