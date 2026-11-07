package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDocumentoPredio;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Documento, título, constancia o plano sustentatorio del predio municipal.
 */
@Entity
@Table(name = "tributaria_documentos_predio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPredio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private PredioTributario predio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 50)
    @Builder.Default
    private TipoDocumentoPredio tipoDocumento = TipoDocumentoPredio.DECLARACION_JURADA;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;

    @Column(name = "numero_folio", length = 50)
    private String numeroFolio;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "entidad_emisora", length = 150)
    private String entidadEmisora;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
