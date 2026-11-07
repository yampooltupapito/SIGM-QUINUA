package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DerivacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface DerivacionExpedienteRepository extends JpaRepository<DerivacionExpediente, Long>, JpaSpecificationExecutor<DerivacionExpediente> {

    List<DerivacionExpediente> findByExpedienteIdOrderByFechaDerivacionDesc(Long expedienteId);

    List<DerivacionExpediente> findByOficinaDestinoIdAndEstado(Long oficinaId, EstadoDerivacion estado);

    Page<DerivacionExpediente> findByOficinaDestinoIdAndEstado(Long oficinaId, EstadoDerivacion estado, Pageable pageable);

    Page<DerivacionExpediente> findByOficinaOrigenId(Long oficinaOrigenId, Pageable pageable);

    Optional<DerivacionExpediente> findFirstByExpedienteIdAndEstado(Long expedienteId, EstadoDerivacion estado);

    long countByOficinaDestinoIdAndEstado(Long oficinaId, EstadoDerivacion estado);

    long countByOficinaOrigenId(Long oficinaId);

    long countByEstado(EstadoDerivacion estado);

    @Query("SELECT COUNT(d) FROM DerivacionExpediente d WHERE d.oficinaDestino.id = :oficinaId")
    long countTotalRecibidasByOficinaId(@Param("oficinaId") Long oficinaId);

    @Query("SELECT COUNT(d) FROM DerivacionExpediente d WHERE " +
           "(:oficinaId IS NULL OR d.oficinaOrigen.id = :oficinaId) AND " +
           "d.fechaDerivacion >= :fechaDesde AND d.fechaDerivacion <= :fechaHasta")
    long countEnviadas(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT COUNT(d) FROM DerivacionExpediente d WHERE " +
           "(:oficinaId IS NULL OR d.oficinaDestino.id = :oficinaId) AND " +
           "d.fechaDerivacion >= :fechaDesde AND d.fechaDerivacion <= :fechaHasta")
    long countRecibidas(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT d.estado, COUNT(d) FROM DerivacionExpediente d WHERE " +
           "(:oficinaId IS NULL OR d.oficinaDestino.id = :oficinaId) AND " +
           "d.fechaDerivacion >= :fechaDesde AND d.fechaDerivacion <= :fechaHasta " +
           "GROUP BY d.estado")
    java.util.List<Object[]> countGroupByEstado(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT COUNT(DISTINCT d.expediente.id) FROM DerivacionExpediente d WHERE " +
           "(:oficinaId IS NULL OR d.oficinaDestino.id = :oficinaId OR d.oficinaOrigen.id = :oficinaId) AND " +
           "d.fechaDerivacion >= :fechaDesde AND d.fechaDerivacion <= :fechaHasta")
    long countExpedientesUnicosDerivados(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @Query("SELECT d FROM DerivacionExpediente d WHERE " +
           "(:oficinaId IS NULL OR d.oficinaDestino.id = :oficinaId) AND " +
           "d.fechaRecepcion IS NOT NULL AND " +
           "d.fechaDerivacion >= :fechaDesde AND d.fechaDerivacion <= :fechaHasta")
    java.util.List<DerivacionExpediente> findRecibidasConTiempos(
            @Param("oficinaId") Long oficinaId,
            @Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );
}

