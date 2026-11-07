package saludar.controlador;

import java.util.List;
import saludar.excepciones.ValidacionException;
import saludar.modelo.Alergia;
import saludar.modelo.ObraSocial;
import saludar.modelo.Paciente;
import saludar.servicio.DatosPaciente;

/** Vista de gestion de pacientes (CU-02, CU-03, CU-06). */
public interface PacienteVista extends Vista {

    /** Acciones que el usuario puede elegir desde el detalle de un paciente. */
    enum AccionPaciente { CERRAR, MODIFICAR, DAR_DE_BAJA, AGREGAR_ALERGIA }

    void setControlador(PacienteController controlador);

    void mostrarObrasSociales(List<ObraSocial> obras);

    void mostrarPacientes(List<Paciente> pacientes, String resumen);

    /** Datos del formulario de alta. Lanza ValidacionException si la fecha no es valida. */
    DatosPaciente leerDatosAlta() throws ValidacionException;

    void limpiarAlta();

    void seleccionar(Paciente p);

    /** Muestra los datos personales y devuelve la accion elegida. */
    AccionPaciente mostrarDetalle(Paciente p);

    /** Abre el formulario de modificacion; al confirmar llama a guardarModificacion() del controlador. */
    void abrirEdicion(Paciente p, List<ObraSocial> obras);

    DatosPaciente leerDatosEdicion() throws ValidacionException;

    void cerrarEdicion();

    /** Pide los datos de una alergia; devuelve null si el usuario cancela. */
    Alergia pedirAlergia(Paciente p);
}
