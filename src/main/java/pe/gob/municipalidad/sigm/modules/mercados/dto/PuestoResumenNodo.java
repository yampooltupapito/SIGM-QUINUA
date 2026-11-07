package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PuestoResumenNodo {
    private UUID id;
    private String codigo;
    private String numero;
    private BigDecimal areaM2;
    private String estado;
    private String tipoPuestoNombre;

    public PuestoResumenNodo() {
    }

    public PuestoResumenNodo(UUID id, String codigo, String numero, BigDecimal areaM2, String estado, String tipoPuestoNombre) {
        this.id = id;
        this.codigo = codigo;
        this.numero = numero;
        this.areaM2 = areaM2;
        this.estado = estado;
        this.tipoPuestoNombre = tipoPuestoNombre;
    }

    public static PuestoResumenNodoBuilder builder() {
        return new PuestoResumenNodoBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public BigDecimal getAreaM2() { return areaM2; }
    public void setAreaM2(BigDecimal areaM2) { this.areaM2 = areaM2; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoPuestoNombre() { return tipoPuestoNombre; }
    public void setTipoPuestoNombre(String tipoPuestoNombre) { this.tipoPuestoNombre = tipoPuestoNombre; }

    public static class PuestoResumenNodoBuilder {
        private UUID id;
        private String codigo;
        private String numero;
        private BigDecimal areaM2;
        private String estado;
        private String tipoPuestoNombre;

        public PuestoResumenNodoBuilder id(UUID id) { this.id = id; return this; }
        public PuestoResumenNodoBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public PuestoResumenNodoBuilder numero(String numero) { this.numero = numero; return this; }
        public PuestoResumenNodoBuilder areaM2(BigDecimal areaM2) { this.areaM2 = areaM2; return this; }
        public PuestoResumenNodoBuilder estado(String estado) { this.estado = estado; return this; }
        public PuestoResumenNodoBuilder tipoPuestoNombre(String tipoPuestoNombre) { this.tipoPuestoNombre = tipoPuestoNombre; return this; }

        public PuestoResumenNodo build() {
            return new PuestoResumenNodo(id, codigo, numero, areaM2, estado, tipoPuestoNombre);
        }
    }
}
