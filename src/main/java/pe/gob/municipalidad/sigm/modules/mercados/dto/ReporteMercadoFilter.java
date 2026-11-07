package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.PeriodoConsulta;

import java.time.LocalDate;
import java.util.UUID;

public class ReporteMercadoFilter {

    private UUID mercadoId;
    private UUID sectorId;
    private UUID pabellonId;
    private UUID zonaId;
    private Long tipoPuestoId;
    private String estado;
    private String tipo;
    private String termino;
    private PeriodoConsulta periodo = PeriodoConsulta.MES_ACTUAL;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    public ReporteMercadoFilter() {
    }

    public ReporteMercadoFilter(UUID mercadoId, UUID sectorId, UUID pabellonId, UUID zonaId,
                                Long tipoPuestoId, String estado, String tipo, String termino,
                                PeriodoConsulta periodo, LocalDate fechaDesde, LocalDate fechaHasta) {
        this.mercadoId = mercadoId;
        this.sectorId = sectorId;
        this.pabellonId = pabellonId;
        this.zonaId = zonaId;
        this.tipoPuestoId = tipoPuestoId;
        this.estado = estado;
        this.tipo = tipo;
        this.termino = termino;
        this.periodo = periodo != null ? periodo : PeriodoConsulta.MES_ACTUAL;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public LocalDate[] resolverRangoEfectivo() {
        if (periodo != null) {
            return periodo.resolverRango(fechaDesde, fechaHasta);
        }
        return PeriodoConsulta.MES_ACTUAL.resolverRango(fechaDesde, fechaHasta);
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public UUID getSectorId() { return sectorId; }
    public void setSectorId(UUID sectorId) { this.sectorId = sectorId; }

    public UUID getPabellonId() { return pabellonId; }
    public void setPabellonId(UUID pabellonId) { this.pabellonId = pabellonId; }

    public UUID getZonaId() { return zonaId; }
    public void setZonaId(UUID zonaId) { this.zonaId = zonaId; }

    public Long getTipoPuestoId() { return tipoPuestoId; }
    public void setTipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTermino() { return termino; }
    public void setTermino(String termino) { this.termino = termino; }

    public PeriodoConsulta getPeriodo() { return periodo; }
    public void setPeriodo(PeriodoConsulta periodo) { this.periodo = periodo; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }

    public LocalDate getFechaInicio() { return fechaDesde; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaDesde = fechaInicio; }

    public LocalDate getFechaFin() { return fechaHasta; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaHasta = fechaFin; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID mercadoId;
        private UUID sectorId;
        private UUID pabellonId;
        private UUID zonaId;
        private Long tipoPuestoId;
        private String estado;
        private String tipo;
        private String termino;
        private PeriodoConsulta periodo = PeriodoConsulta.MES_ACTUAL;
        private LocalDate fechaDesde;
        private LocalDate fechaHasta;

        public Builder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public Builder sectorId(UUID sectorId) { this.sectorId = sectorId; return this; }
        public Builder pabellonId(UUID pabellonId) { this.pabellonId = pabellonId; return this; }
        public Builder zonaId(UUID zonaId) { this.zonaId = zonaId; return this; }
        public Builder tipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; return this; }
        public Builder estado(String estado) { this.estado = estado; return this; }
        public Builder tipo(String tipo) { this.tipo = tipo; return this; }
        public Builder termino(String termino) { this.termino = termino; return this; }
        public Builder periodo(PeriodoConsulta periodo) { this.periodo = periodo; return this; }
        public Builder fechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; return this; }
        public Builder fechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; return this; }
        public Builder fechaInicio(LocalDate fechaInicio) { this.fechaDesde = fechaInicio; return this; }
        public Builder fechaFin(LocalDate fechaFin) { this.fechaHasta = fechaFin; return this; }

        public ReporteMercadoFilter build() {
            return new ReporteMercadoFilter(mercadoId, sectorId, pabellonId, zonaId, tipoPuestoId,
                    estado, tipo, termino, periodo, fechaDesde, fechaHasta);
        }
    }
}
