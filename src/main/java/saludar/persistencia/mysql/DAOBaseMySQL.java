package saludar.persistencia.mysql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import saludar.config.ConexionBD;
import saludar.excepciones.PersistenciaException;

/**
 * Clase ABSTRACTA base de todos los DAO MySQL. Aplica el patron TEMPLATE
 * METHOD: resuelve una sola vez lo que es igual para todas las tablas
 * (abrir y cerrar la conexion, preparar la sentencia, asignar parametros,
 * recorrer el ResultSet y traducir los errores) y deja a cada subclase solo
 * el paso que cambia: como convertir una fila en un objeto, con mapear().
 *
 * Todas las sentencias son PreparedStatement (RNF-03: previene inyeccion SQL).
 *
 * @param <T> tipo de objeto del dominio que maneja el DAO
 */
public abstract class DAOBaseMySQL<T> {

    // Codigos de error de MySQL que se traducen a mensajes comprensibles
    private static final int ER_DUPLICADO = 1062;
    private static final int ER_FK_PADRE = 1451;
    private static final int ER_FK_HIJO = 1452;
    private static final int ER_SIGNAL_TRIGGER = 1644;
    private static final int ER_CHECK = 3819;
    private static final int ER_SIN_PERMISO = 1142;

    protected abstract T mapear(ResultSet rs) throws SQLException;

    protected Connection conectar() throws PersistenciaException {
        return ConexionBD.getInstancia().getConexion();
    }

    /** Ejecuta un SELECT y devuelve todas las filas mapeadas en un ArrayList. */
    protected List<T> consultarLista(String sql, Object... params) throws PersistenciaException {
        List<T> lista = new ArrayList<>();      // tamanio desconocido de antemano: ArrayList
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw traducir(e, "consultar la base de datos");
        }
        return lista;
    }

    /** Ejecuta un SELECT que devuelve a lo sumo una fila; null si no hay resultados. */
    protected T consultarUno(String sql, Object... params) throws PersistenciaException {
        List<T> lista = consultarLista(sql, params);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Ejecuta un SELECT de un unico valor (COUNT, un texto, etc.). */
    protected <R> R consultarValor(String sql, Class<R> tipo, Object... params) throws PersistenciaException {
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1, tipo) : null;
            }
        } catch (SQLException e) {
            throw traducir(e, "consultar la base de datos");
        }
    }

    /** Ejecuta un INSERT, UPDATE o DELETE; devuelve la cantidad de filas afectadas. */
    protected int ejecutarActualizacion(String sql, Object... params) throws PersistenciaException {
        try (Connection con = conectar()) {
            return ejecutar(con, sql, params);
        } catch (SQLException e) {
            throw traducir(e, "actualizar la base de datos");
        }
    }

    /** Ejecuta un INSERT y devuelve la clave autogenerada. */
    protected int insertarRegistro(String sql, Object... params) throws PersistenciaException {
        try (Connection con = conectar()) {
            return insertar(con, sql, params);
        } catch (SQLException e) {
            throw traducir(e, "guardar en la base de datos");
        }
    }

    // ---- Variantes sobre una conexion dada: se usan dentro de transacciones ----

    protected static int ejecutar(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            asignarParametros(ps, params);
            return ps.executeUpdate();
        }
    }

    protected static int insertar(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, params);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("El motor no devolvio la clave generada.");
                return claves.getInt(1);
            }
        }
    }

    /** Deshace la transaccion en curso sin ocultar el error original. */
    protected static void deshacer(Connection con, SQLException original) {
        try {
            con.rollback();
        } catch (SQLException e) {
            original.addSuppressed(e);
        }
    }

    /**
     * Asigna los parametros de la sentencia. Recibe un ARREGLO (parametros
     * variables Object...) porque cada consulta tiene una cantidad fija y
     * conocida de parametros, y se recorre por indice: la posicion i del
     * arreglo corresponde al signo ? numero i+1 de la sentencia.
     */
    protected static void asignarParametros(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object v = params[i];
            int pos = i + 1;
            if (v == null) {
                ps.setNull(pos, Types.NULL);
            } else if (v instanceof LocalDate d) {
                ps.setDate(pos, Date.valueOf(d));
            } else if (v instanceof LocalDateTime t) {
                ps.setTimestamp(pos, Timestamp.valueOf(t));
            } else if (v instanceof Enum<?> e) {
                ps.setString(pos, e.name());
            } else if (v instanceof Character c) {
                ps.setString(pos, String.valueOf(c));
            } else {
                ps.setObject(pos, v);
            }
        }
    }

    /** Lee un entero que puede ser NULL en la base. */
    protected static Integer enteroONulo(ResultSet rs, String columna) throws SQLException {
        int v = rs.getInt(columna);
        return rs.wasNull() ? null : v;
    }

    /**
     * Traduce una SQLException a una PersistenciaException con un mensaje
     * comprensible, conservando la excepcion original como causa.
     */
    protected static PersistenciaException traducir(SQLException e, String operacion) {
        String mensaje;
        switch (e.getErrorCode()) {
            case ER_SIGNAL_TRIGGER -> mensaje = e.getMessage();      // regla de negocio del motor
            case ER_DUPLICADO      -> mensaje = "Ya existe un registro con esos datos (valor duplicado).";
            case ER_FK_PADRE, ER_FK_HIJO -> mensaje = "La operacion viola la integridad referencial de los datos.";
            case ER_CHECK          -> mensaje = "Un dato no cumple las restricciones de la base de datos.";
            case ER_SIN_PERMISO    -> mensaje = "El usuario de la base de datos no tiene permiso para esta operacion.";
            default -> {
                String estado = e.getSQLState();
                if (estado != null && estado.startsWith("08")) {     // clase 08: error de conexion
                    mensaje = "Se perdio la conexion con la base de datos.";
                } else {
                    mensaje = "Error al " + operacion + ": " + e.getMessage();
                }
            }
        }
        return new PersistenciaException(mensaje, e);
    }
}
