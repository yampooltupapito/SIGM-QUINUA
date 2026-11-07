package pe.gob.municipalidad.sigm.modules.mercados.dto;

public class OficinaSimpleResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String sigla;

    public OficinaSimpleResponse() {
    }

    public OficinaSimpleResponse(Long id, String codigo, String nombre, String sigla) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.sigla = sigla;
    }

    public static OficinaSimpleResponseBuilder builder() {
        return new OficinaSimpleResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public static class OficinaSimpleResponseBuilder {
        private Long id;
        private String codigo;
        private String nombre;
        private String sigla;

        public OficinaSimpleResponseBuilder id(Long id) { this.id = id; return this; }
        public OficinaSimpleResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public OficinaSimpleResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public OficinaSimpleResponseBuilder sigla(String sigla) { this.sigla = sigla; return this; }

        public OficinaSimpleResponse build() {
            return new OficinaSimpleResponse(id, codigo, nombre, sigla);
        }
    }
}
