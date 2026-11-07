package pe.gob.municipalidad.sigm.modules.mercados.dto;

public class TipoPuestoResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean permiteInstalacionFija;
    private String estado;

    public TipoPuestoResponse() {
    }

    public TipoPuestoResponse(Long id, String codigo, String nombre, String descripcion, Boolean permiteInstalacionFija, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permiteInstalacionFija = permiteInstalacionFija;
        this.estado = estado;
    }

    public static TipoPuestoResponseBuilder builder() {
        return new TipoPuestoResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getPermiteInstalacionFija() { return permiteInstalacionFija; }
    public void setPermiteInstalacionFija(Boolean permiteInstalacionFija) { this.permiteInstalacionFija = permiteInstalacionFija; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public static class TipoPuestoResponseBuilder {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Boolean permiteInstalacionFija;
        private String estado;

        public TipoPuestoResponseBuilder id(Long id) { this.id = id; return this; }
        public TipoPuestoResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public TipoPuestoResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public TipoPuestoResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public TipoPuestoResponseBuilder permiteInstalacionFija(Boolean permiteInstalacionFija) { this.permiteInstalacionFija = permiteInstalacionFija; return this; }
        public TipoPuestoResponseBuilder estado(String estado) { this.estado = estado; return this; }

        public TipoPuestoResponse build() {
            return new TipoPuestoResponse(id, codigo, nombre, descripcion, permiteInstalacionFija, estado);
        }
    }
}
