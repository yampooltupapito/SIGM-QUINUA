package pe.gob.municipalidad.sigm.modules.mercados.dto;

public class CategoriaComercialResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String estado;
    private Integer ordenVisual;

    public CategoriaComercialResponse() {
    }

    public CategoriaComercialResponse(Long id, String codigo, String nombre, String descripcion, String estado, Integer ordenVisual) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.ordenVisual = ordenVisual;
    }

    public static CategoriaComercialResponseBuilder builder() {
        return new CategoriaComercialResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public static class CategoriaComercialResponseBuilder {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String estado;
        private Integer ordenVisual;

        public CategoriaComercialResponseBuilder id(Long id) { this.id = id; return this; }
        public CategoriaComercialResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public CategoriaComercialResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public CategoriaComercialResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public CategoriaComercialResponseBuilder estado(String estado) { this.estado = estado; return this; }
        public CategoriaComercialResponseBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }

        public CategoriaComercialResponse build() {
            return new CategoriaComercialResponse(id, codigo, nombre, descripcion, estado, ordenVisual);
        }
    }
}
