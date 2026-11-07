package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialArbitrio;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tributaria_historial_arbitrio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialArbitrioMunicipal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "entidad_tipo", nullable = false, length = 40)
    private String entidadTipo; // 'CONFIGURACION' o 'CALCULO'

    @Column(name = "entidad_id", nullable = false)
    private UUID entidadId;

    @Column(name = "codigo_referencia", length = 60)
    private String codigoReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionHistorialArbitrio accion;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "nuevo_estado", nullable = false, length = 30)
    private String nuevoEstado;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();
}
