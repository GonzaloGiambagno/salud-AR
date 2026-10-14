package saludar.excepciones;

/** Se lanza al intentar modificar una consulta ya cerrada (Ley 26.529). */
public class ConsultaCerradaException extends Exception {
    public ConsultaCerradaException(int idConsulta) {
        super("La consulta #" + idConsulta + " esta CERRADA y no puede modificarse.");
    }
}
