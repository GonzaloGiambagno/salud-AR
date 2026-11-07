package saludar.controlador;

/**
 * Contrato comun de todas las vistas (la V de MVC). Los controladores solo
 * conocen estas interfaces y nunca a las clases Swing concretas, de modo que
 * la logica de control no depende de la tecnologia de interfaz.
 */
public interface Vista {
    void mostrarInfo(String titulo, String mensaje);

    /** Error que el usuario puede corregir (validacion, regla de negocio, permiso). */
    void mostrarAviso(String titulo, String mensaje);

    /** Error del sistema (base de datos, archivos). */
    void mostrarError(String titulo, String mensaje);

    boolean confirmar(String titulo, String pregunta);
}
