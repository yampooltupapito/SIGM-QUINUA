package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ficha técnica oficial de un indicador del catálogo de Seguridad Ciudadana.
 * Garantiza definiciones y fuentes de cálculo transparentes y verificables.
 */
@Entity
@Table(name = "serenazgo_catalogo_indicadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoIndicadorSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "modulo_origen", nullable = false, length = 50)
    private String moduloOrigen;

    @Column(name = "definicion", nullable = false, columnDefinition = "TEXT")
    private String definicion;

    @Column(name = "fuente_datos", nullable = false, length = 100)
    private String fuenteDatos;

    @Column(name = "formula_calculo", nullable = false, columnDefinition = "TEXT")
    private String formulaCalculo;

    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida;

    @Column(name = "frecuencia_calculo", nullable = false, length = 50)
    @Builder.Default
    private String frecuenciaCalculo = "CONSULTA_PERIODICA";

    @Column(name = "limitaciones_interpretacion", nullable = false, columnDefinition = "TEXT")
    private String limitacionesInterpretacion;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
