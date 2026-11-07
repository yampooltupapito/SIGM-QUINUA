package pe.gob.municipalidad.sigm.modules.portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalContacto;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PortalContactoRepository extends JpaRepository<PortalContacto, Long> {

    Optional<PortalContacto> findByCodigo(String codigo);

    Page<PortalContacto> findByEstadoOrderByCreatedAtDesc(String estado, Pageable pageable);

    Page<PortalContacto> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByEstado(String estado);

    long countByRemitenteCorreoAndCreatedAtAfter(String correo, LocalDateTime after);

    long countByIpOrigenAndCreatedAtAfter(String ipOrigen, LocalDateTime after);

    @Query("SELECT COUNT(c) FROM PortalContacto c WHERE c.createdAt >= :inicioDia")
    long countConsultasHoy(@Param("inicioDia") LocalDateTime inicioDia);
}
