package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Registro histórico inmutable de cambios de ubicación física (sector, pabellón, zona) de un puesto.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_puestos_cambios_ubicacion")
public class PuestoCambioUbicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_anterior_id")
    private SectorMercado sectorAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_nuevo_id")
    private SectorMercado sectorNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pabellon_anterior_id")
    private PabellonMercado pabellonAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pabellon_nuevo_id")
    private PabellonMercado pabellonNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_anterior_id")
    private ZonaComercial zonaAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_nueva_id")
    private ZonaComercial zonaNueva;

    @Column(name = "ubicacion_ref_anterior", length = 255)
    private String ubicacionRefAnterior;

    @Column(name = "ubicacion_ref_nueva", length = 255)
    private String ubicacionRefNueva;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "usuario_responsable", nullable = false, length = 100)
    private String usuarioResponsable;

    @Column(name = "usuario_username", length = 100)
    private String usuarioUsername;

    @Builder.Default
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private OffsetDateTime fechaCambio = OffsetDateTime.now();
}
