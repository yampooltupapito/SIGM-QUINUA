package pe.gob.municipalidad.sigm.common.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionEnvio;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionEnvioRepository extends JpaRepository<NotificacionEnvio, Long> {

    Optional<NotificacionEnvio> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT e FROM NotificacionEnvio e WHERE e.estado = 'PENDIENTE' " +
            "AND (e.proximoIntento IS NULL OR e.proximoIntento <= :ahora) " +
            "AND e.intentos < e.maxIntentos ORDER BY e.createdAt ASC")
    List<NotificacionEnvio> findEnviosPendientesParaProcesar(@Param("ahora") OffsetDateTime ahora, Pageable pageable);

    Page<NotificacionEnvio> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId, Pageable pageable);

    Page<NotificacionEnvio> findByEstadoOrderByCreatedAtDesc(String estado, Pageable pageable);
}
