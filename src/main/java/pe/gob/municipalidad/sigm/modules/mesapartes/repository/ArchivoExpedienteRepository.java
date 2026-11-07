package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ArchivoExpediente;

import java.util.Optional;

@Repository
public interface ArchivoExpedienteRepository extends JpaRepository<ArchivoExpediente, Long> {

    Optional<ArchivoExpediente> findFirstByExpedienteIdOrderByFechaArchivoDesc(Long expedienteId);

    Page<ArchivoExpediente> findByOficinaArchivoId(Long oficinaId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM ArchivoExpediente a WHERE (:oficinaId IS NULL OR a.oficinaArchivo.id = :oficinaId)")
    long countArchivos(@Param("oficinaId") Long oficinaId);
}
