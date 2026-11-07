package saludar.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import saludar.excepciones.PersistenciaException;

/**
 * Punto unico de acceso a la base de datos MySQL.la config se lee una sola vez y todas las clases DAO obtienen sus
 * conexiones desde la misma instancia.
 */
public final class ConexionBD {

    private static final Path ARCHIVO_EXTERNO = Path.of("config", "db.properties");
    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String clave;

    private ConexionBD(Properties p) {
        this.url = p.getProperty("db.url");
        this.usuario = p.getProperty("db.usuario");
        this.clave = p.getProperty("db.clave");
    }

    /** Devuelve la unica instancia, creandola la primera vez. */
    public static synchronized ConexionBD getInstancia() throws PersistenciaException {
        if (instancia == null) {
            instancia = new ConexionBD(cargarConfiguracion());
        }
        return instancia;
    }

    /** Abre una conexion nueva. Quien la pide es responsable de cerrarla. */
    public Connection getConexion() throws PersistenciaException {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar con la base de datos MySQL ("
                    + url + ").\nVerifique que el servidor este iniciado y que los datos de "
                    + ARCHIVO_EXTERNO + " sean correctos.\nDetalle: " + e.getMessage(), e);
        }
    }

    public String getUrl() { return url; }

    private static Properties cargarConfiguracion() throws PersistenciaException {
        Properties p = new Properties();
        try (InputStream in = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(in);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer la configuracion por defecto (db.properties).", e);
        }
        if (Files.exists(ARCHIVO_EXTERNO)) {
            try (Reader r = Files.newBufferedReader(ARCHIVO_EXTERNO, StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException e) {
                throw new PersistenciaException("No se pudo leer el archivo " + ARCHIVO_EXTERNO + ".", e);
            }
        }
        String[] claves = {"url", "usuario", "clave"};          // ARREGLO de claves configurables
        for (String c : claves) {
            String valor = System.getProperty("saludar.db." + c);
            if (valor != null) p.setProperty("db." + c, valor);
        }
        if (p.getProperty("db.url") == null) {
            throw new PersistenciaException("Falta la propiedad db.url en la configuracion.");
        }
        return p;
    }
}
