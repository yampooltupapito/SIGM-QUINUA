package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificacionTributariaRepository extends JpaRepository<NotificacionTributaria, UUID>, JpaSpecificationExecutor<NotificacionTributaria> {

    Optional<NotificacionTributaria> findByCodigoNotificacion(String codigoNotificacion);

    Optional<NotificacionTributaria> findByIdempotencyKey(String idempotencyKey);

    boolean existsByCodigoNotificacion(String codigoNotificacion);

    List<NotificacionTributaria> findByContribuyenteIdOrderByFechaCreacionDesc(UUID contribuyenteId);

    List<NotificacionTributaria> findByLoteId(UUID loteId);

    long countByEstadoAdministrativo(EstadoAdministrativoNotificacion estado);

    long countByEstadoEntrega(EstadoEntregaNotificacion estado);

    long countByEstadoValidezLegal(EstadoValidezLegalNotificacion estado);

    @Query("SELECT n FROM NotificacionTributaria n WHERE n.codigoNotificacion = :term OR n.destinatarioNombre LIKE %:term% OR n.destinatarioNumeroDoc = :term")
    List<NotificacionTributaria> buscarPorTermino(@Param("term") String term, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificacionTributaria n")
    long countTotalNotificaciones();
}

