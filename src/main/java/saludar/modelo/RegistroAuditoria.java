package saludar.modelo;

import java.time.LocalDateTime;

/** Fila del registro de auditoria (RF-23): quien, que y cuando. */
public class RegistroAuditoria {
    private final LocalDateTime fechaHora;
    private final String usuario;
    private final String accion;
    private final String entidad;
    private final Integer idRegistro;
    private final String detalle;

    public RegistroAuditoria(LocalDateTime fechaHora, String usuario, String accion,
                             String entidad, Integer idRegistro, String detalle) {
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
        this.idRegistro = idRegistro;
        this.detalle = detalle;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getUsuario() { return usuario; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public Integer getIdRegistro() { return idRegistro; }
    public String getDetalle() { return detalle; }
}
