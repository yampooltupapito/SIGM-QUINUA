package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.NotificacionAdministrativa;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoNotificacion;

import java.util.List;

@Repository
public interface NotificacionAdministrativaRepository extends JpaRepository<NotificacionAdministrativa, Long> {

    List<NotificacionAdministrativa> findByExpedienteIdOrderByCreatedAtDesc(Long expedienteId);

    List<NotificacionAdministrativa> findByExpedienteIdAndEsLegalmenteValidaTrue(Long expedienteId);

    Page<NotificacionAdministrativa> findByEstadoNotificacion(EstadoNotificacion estado, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificacionAdministrativa n WHERE n.estadoNotificacion = :estado")
    long countByEstado(@Param("estado") EstadoNotificacion estado);

    @Query("SELECT COUNT(n) FROM NotificacionAdministrativa n WHERE n.esLegalmenteValida = false AND n.estadoNotificacion = 'PENDIENTE'")
    long countPendientesNotificacion();
}
