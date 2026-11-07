package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.time.LocalDate;

public class MercadoFilter {
    private String termino;
    private EstadoMercado estado;
    private Long oficinaId;
    private String distrito;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String sortBy = "nombre";
    private String sortDir = "asc";
    private int page = 0;
    private int size = 10;

    public MercadoFilter() {
    }

    public MercadoFilter(String termino, EstadoMercado estado, Long oficinaId, String distrito,
                         LocalDate fechaDesde, LocalDate fechaHasta, String sortBy,
                         String sortDir, int page, int size) {
        this.termino = termino;
        this.estado = estado;
        this.oficinaId = oficinaId;
        this.distrito = distrito;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.sortBy = sortBy != null ? sortBy : "nombre";
        this.sortDir = sortDir != null ? sortDir : "asc";
        this.page = page;
        this.size = size > 0 ? size : 10;
    }

    public static MercadoFilterBuilder builder() {
        return new MercadoFilterBuilder();
    }

    public String getTermino() { return termino; }
    public void setTermino(String termino) { this.termino = termino; }

    public EstadoMercado getEstado() { return estado; }
    public void setEstado(EstadoMercado estado) { this.estado = estado; }

    public Long getOficinaId() { return oficinaId; }
    public void setOficinaId(Long oficinaId) { this.oficinaId = oficinaId; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public static class MercadoFilterBuilder {
        private String termino;
        private EstadoMercado estado;
        private Long oficinaId;
        private String distrito;
        private LocalDate fechaDesde;
        private LocalDate fechaHasta;
        private String sortBy = "nombre";
        private String sortDir = "asc";
        private int page = 0;
        private int size = 10;

        public MercadoFilterBuilder termino(String termino) { this.termino = termino; return this; }
        public MercadoFilterBuilder estado(EstadoMercado estado) { this.estado = estado; return this; }
        public MercadoFilterBuilder oficinaId(Long oficinaId) { this.oficinaId = oficinaId; return this; }
        public MercadoFilterBuilder distrito(String distrito) { this.distrito = distrito; return this; }
        public MercadoFilterBuilder fechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; return this; }
        public MercadoFilterBuilder fechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; return this; }
        public MercadoFilterBuilder sortBy(String sortBy) { this.sortBy = sortBy; return this; }
        public MercadoFilterBuilder sortDir(String sortDir) { this.sortDir = sortDir; return this; }
        public MercadoFilterBuilder page(int page) { this.page = page; return this; }
        public MercadoFilterBuilder size(int size) { this.size = size; return this; }

        public MercadoFilter build() {
            return new MercadoFilter(termino, estado, oficinaId, distrito, fechaDesde, fechaHasta, sortBy, sortDir, page, size);
        }
    }
}
