package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAlertasResponse {

    private long totalAlertas;
    private long recibidas;
    private long pendientes;
    private long enRevision;
    private long requierenInformacion;
    private long clasificadas;
    private long vinculadas;
    private long cerradas;
    private long descartadas;

    private long prioridadBaja;
    private long prioridadMedia;
    private long prioridadAlta;
    private long prioridadCritica;

    private long hoy;
    private long estaSemana;
    private long esteMes;

    // Convenience getters for templates
    public long getTotalRecibidas() { return recibidas; }
    public long getTotalPendientes() { return pendientes; }
    public long getTotalEnRevision() { return enRevision; }
    public long getTotalRequierenInformacion() { return requierenInformacion; }
    public long getTotalClasificadas() { return clasificadas; }
    public long getTotalVinculadas() { return vinculadas; }
    public long getTotalCerradas() { return cerradas; }
    public long getTotalDescartadas() { return descartadas; }
    public long getTotalCriticas() { return prioridadCritica; }
    public long getTotalAltas() { return prioridadAlta; }
}
