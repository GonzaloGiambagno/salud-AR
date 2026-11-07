package saludar.app;

import saludar.persistencia.DAOFactory;
import saludar.servicio.AuditoriaService;
import saludar.servicio.AutenticacionService;
import saludar.servicio.CatalogoCie10;
import saludar.servicio.ConsultaService;
import saludar.servicio.ExportadorHistoriaClinica;
import saludar.servicio.PacienteService;
import saludar.servicio.SalaDeEspera;

/**
 * Agrupa los servicios de la aplicacion y los arma a partir de una fabrica
 * de DAO: es el unico lugar donde se decide que tecnologia de persistencia
 * se usa (composition root).
 */
public record Servicios(AutenticacionService autenticacion, PacienteService pacientes,
                        ConsultaService consultas, SalaDeEspera sala, CatalogoCie10 catalogo,
                        AuditoriaService auditoria, ExportadorHistoriaClinica exportador) {

    public static Servicios crear(DAOFactory f) {
        AuditoriaService auditoria = new AuditoriaService(f.auditoria());
        return new Servicios(
                new AutenticacionService(f.usuarios(), auditoria),
                new PacienteService(f.pacientes(), f.alergias(), f.obrasSociales(), auditoria),
                new ConsultaService(f.consultas()),
                new SalaDeEspera(f.ingresosSala(), auditoria),
                new CatalogoCie10(f.cie10()),
                auditoria,
                new ExportadorHistoriaClinica());
    }
}
