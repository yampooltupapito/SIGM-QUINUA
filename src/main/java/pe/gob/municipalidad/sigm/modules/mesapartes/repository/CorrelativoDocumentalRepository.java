package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CorrelativoDocumental;

import java.util.Optional;

@Repository
public interface CorrelativoDocumentalRepository extends JpaRepository<CorrelativoDocumental, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CorrelativoDocumental c WHERE c.tipoDocumento = :tipoDocumento AND (:oficinaId IS NULL AND c.oficina IS NULL OR c.oficina.id = :oficinaId) AND c.anio = :anio")
    Optional<CorrelativoDocumental> findForUpdate(
            @Param("tipoDocumento") String tipoDocumento,
            @Param("oficinaId") Long oficinaId,
            @Param("anio") Integer anio
    );

    @Query("SELECT c FROM CorrelativoDocumental c WHERE c.tipoDocumento = :tipoDocumento AND (:oficinaId IS NULL AND c.oficina IS NULL OR c.oficina.id = :oficinaId) AND c.anio = :anio")
    Optional<CorrelativoDocumental> findByTipoDocumentoAndOficinaIdAndAnio(
            @Param("tipoDocumento") String tipoDocumento,
            @Param("oficinaId") Long oficinaId,
            @Param("anio") Integer anio
    );
}
