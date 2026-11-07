package saludar.excepciones;

/**
 * Error al acceder a la base de datos MySQL. Envuelve la SQLException original
 * (disponible con getCause()) y expone un mensaje comprensible para el usuario,
 * de modo que las capas superiores no dependan de JDBC.
 */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje) { super(mensaje); }
    public PersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
