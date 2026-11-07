package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CierreExpediente;

import java.util.Optional;

@Repository
public interface CierreExpedienteRepository extends JpaRepository<CierreExpediente, Long> {

    Optional<CierreExpediente> findFirstByExpedienteIdOrderByFechaCierreDesc(Long expedienteId);

    Page<CierreExpediente> findByOficinaCierreId(Long oficinaId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CierreExpediente c WHERE (:oficinaId IS NULL OR c.oficinaCierre.id = :oficinaId)")
    long countCierres(@Param("oficinaId") Long oficinaId);
}
