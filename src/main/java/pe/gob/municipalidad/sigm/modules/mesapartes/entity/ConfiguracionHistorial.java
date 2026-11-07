package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_configuracion_historial")
public class ConfiguracionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_tipo", nullable = false, length = 50)
    private TipoEntidadConfiguracion entidadTipo;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Column(name = "codigo_referencia", length = 100)
    private String codigoReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cambio", nullable = false, length = 30)
    private TipoCambioConfiguracion tipoCambio;

    @Column(name = "version_anterior")
    private Integer versionAnterior;

    @Column(name = "version_nueva")
    private Integer versionNueva;

    @Column(length = 500)
    private String motivo;

    @Column(name = "cambios_json", columnDefinition = "TEXT")
    private String cambiosJson;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @CreationTimestamp
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private OffsetDateTime fechaCambio;

    public ConfiguracionHistorial() {
    }

    public ConfiguracionHistorial(Long id, TipoEntidadConfiguracion entidadTipo, Long entidadId,
                                  String codigoReferencia, TipoCambioConfiguracion tipoCambio,
                                  Integer versionAnterior, Integer versionNueva, String motivo,
                                  String cambiosJson, Long usuarioId, String username,
                                  String ipOrigen, OffsetDateTime fechaCambio) {
        this.id = id;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.codigoReferencia = codigoReferencia;
        this.tipoCambio = tipoCambio;
        this.versionAnterior = versionAnterior;
        this.versionNueva = versionNueva;
        this.motivo = motivo;
        this.cambiosJson = cambiosJson;
        this.usuarioId = usuarioId;
        this.username = username;
        this.ipOrigen = ipOrigen;
        this.fechaCambio = fechaCambio;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoEntidadConfiguracion getEntidadTipo() {
        return entidadTipo;
    }

    public void setEntidadTipo(TipoEntidadConfiguracion entidadTipo) {
        this.entidadTipo = entidadTipo;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getCodigoReferencia() {
        return codigoReferencia;
    }

    public void setCodigoReferencia(String codigoReferencia) {
        this.codigoReferencia = codigoReferencia;
    }

    public TipoCambioConfiguracion getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(TipoCambioConfiguracion tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public Integer getVersionAnterior() {
        return versionAnterior;
    }

    public void setVersionAnterior(Integer versionAnterior) {
        this.versionAnterior = versionAnterior;
    }

    public Integer getVersionNueva() {
        return versionNueva;
    }

    public void setVersionNueva(Integer versionNueva) {
        this.versionNueva = versionNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getCambiosJson() {
        return cambiosJson;
    }

    public void setCambiosJson(String cambiosJson) {
        this.cambiosJson = cambiosJson;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public OffsetDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(OffsetDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public static class Builder {
        private Long id;
        private TipoEntidadConfiguracion entidadTipo;
        private Long entidadId;
        private String codigoReferencia;
        private TipoCambioConfiguracion tipoCambio;
        private Integer versionAnterior;
        private Integer versionNueva;
        private String motivo;
        private String cambiosJson;
        private Long usuarioId;
        private String username;
        private String ipOrigen;
        private OffsetDateTime fechaCambio;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder entidadTipo(TipoEntidadConfiguracion entidadTipo) {
            this.entidadTipo = entidadTipo;
            return this;
        }

        public Builder entidadId(Long entidadId) {
            this.entidadId = entidadId;
            return this;
        }

        public Builder codigoReferencia(String codigoReferencia) {
            this.codigoReferencia = codigoReferencia;
            return this;
        }

        public Builder tipoCambio(TipoCambioConfiguracion tipoCambio) {
            this.tipoCambio = tipoCambio;
            return this;
        }

        public Builder versionAnterior(Integer versionAnterior) {
            this.versionAnterior = versionAnterior;
            return this;
        }

        public Builder versionNueva(Integer versionNueva) {
            this.versionNueva = versionNueva;
            return this;
        }

        public Builder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public Builder cambiosJson(String cambiosJson) {
            this.cambiosJson = cambiosJson;
            return this;
        }

        public Builder usuarioId(Long usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder ipOrigen(String ipOrigen) {
            this.ipOrigen = ipOrigen;
            return this;
        }

        public Builder fechaCambio(OffsetDateTime fechaCambio) {
            this.fechaCambio = fechaCambio;
            return this;
        }

        public ConfiguracionHistorial build() {
            return new ConfiguracionHistorial(id, entidadTipo, entidadId, codigoReferencia,
                    tipoCambio, versionAnterior, versionNueva, motivo, cambiosJson, usuarioId,
                    username, ipOrigen, fechaCambio);
        }
    }
}
