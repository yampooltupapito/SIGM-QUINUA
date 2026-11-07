package pe.gob.municipalidad.sigm.common.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionProgramacion;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NotificacionProgramacionRepository extends JpaRepository<NotificacionProgramacion, Long> {

    @Query("SELECT p FROM NotificacionProgramacion p WHERE p.ejecutada = false AND p.cancelada = false " +
            "AND p.fechaProgramada <= :ahora ORDER BY p.fechaProgramada ASC")
    List<NotificacionProgramacion> findProgramadasPendientesDeEjecucion(@Param("ahora") OffsetDateTime ahora, Pageable pageable);

    Page<NotificacionProgramacion> findByUsuarioIdOrderByFechaProgramadaDesc(Long usuarioId, Pageable pageable);

    Page<NotificacionProgramacion> findByOficinaIdOrderByFechaProgramadaDesc(Long oficinaId, Pageable pageable);

    List<NotificacionProgramacion> findByEjecutadaFalseAndCanceladaFalseOrderByFechaProgramadaAsc();
}
