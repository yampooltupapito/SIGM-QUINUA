package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal de Contribuyente Tributario Municipal de la Municipalidad Distrital de Quinua.
 * Da soporte a personas naturales, personas jurídicas, sucesiones y sociedades conyugales.
 */
@Entity
@Table(name = "tributaria_contribuyentes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_tributaria_contribuyente_doc", columnNames = {"tipo_documento", "numero_documento"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContribuyenteTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_contribuyente", nullable = false, unique = true, length = 50)
    private String codigoContribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contribuyente", nullable = false, length = 40)
    private TipoContribuyente tipoContribuyente;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    @Column(name = "nombre_razon_social", nullable = false, length = 255)
    private String nombreRazonSocial;

    @Column(name = "nombre_comercial", length = 255)
    private String nombreComercial;

    @Column(name = "nombres", length = 150)
    private String nombres;

    @Column(name = "primer_apellido", length = 100)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    @Column(name = "representante_legal_nombre", length = 255)
    private String representanteLegalNombre;

    @Column(name = "representante_legal_documento", length = 30)
    private String representanteLegalDocumento;

    @Column(name = "domicilio_fiscal", length = 255)
    private String domicilioFiscal;

    @Column(name = "referencia_domicilio", columnDefinition = "TEXT")
    private String referenciaDomicilio;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "telefono_alternativo", length = 50)
    private String telefonoAlternativo;

    @Column(name = "correo", length = 150)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_contacto_preferido", nullable = false, length = 40)
    @Builder.Default
    private MedioContactoPreferido medioContactoPreferido = MedioContactoPreferido.CORREO_ELECTRONICO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoContribuyente estado = EstadoContribuyente.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false, length = 40)
    @Builder.Default
    private EstadoVerificacionDocumento estadoVerificacion = EstadoVerificacionDocumento.NO_VERIFICADO;

    @Column(name = "motivo_cambio_estado", columnDefinition = "TEXT")
    private String motivoCambioEstado;

    @Column(name = "fecha_cambio_estado")
    private OffsetDateTime fechaCambioEstado;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDate fechaRegistro = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_registro_id")
    private Oficina oficinaRegistro;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "contribuyente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RepresentanteContribuyente> representantes = new ArrayList<>();

    @OneToMany(mappedBy = "contribuyente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DomicilioContribuyente> domicilios = new ArrayList<>();

    @OneToMany(mappedBy = "contribuyente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContactoContribuyente> contactos = new ArrayList<>();

    @OneToMany(mappedBy = "contribuyente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistorialEstadoContribuyente> historialEstados = new ArrayList<>();

    public String getNumeroDocumentoEnmascarado() {
        if (numeroDocumento == null || numeroDocumento.length() < 4) {
            return "***";
        }
        int len = numeroDocumento.length();
        return numeroDocumento.substring(0, 2) + "*".repeat(len - 4) + numeroDocumento.substring(len - 2);
    }

    public String getCorreoEnmascarado() {
        if (correo == null || !correo.contains("@")) {
            return correo;
        }
        String[] parts = correo.split("@");
        String name = parts[0];
        if (name.length() <= 2) {
            return name + "***@" + parts[1];
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + parts[1];
    }

    public String getTelefonoEnmascarado() {
        if (telefono == null || telefono.length() < 4) {
            return telefono;
        }
        int len = telefono.length();
        return telefono.substring(0, 2) + "*".repeat(len - 4) + telefono.substring(len - 2);
    }
}
