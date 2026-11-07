package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionConceptoTributarioRepository extends JpaRepository<ConfiguracionConceptoTributario, UUID>, JpaSpecificationExecutor<ConfiguracionConceptoTributario> {

    List<ConfiguracionConceptoTributario> findByConceptoIdOrderByNumeroVersionDesc(UUID conceptoId);

    List<ConfiguracionConceptoTributario> findByConceptoIdAndPeriodoIdOrderByNumeroVersionDesc(UUID conceptoId, UUID periodoId);

    Optional<ConfiguracionConceptoTributario> findByConceptoIdAndPeriodoIdAndNumeroVersion(UUID conceptoId, UUID periodoId, Integer numeroVersion);

    Optional<ConfiguracionConceptoTributario> findFirstByConceptoIdAndPeriodoIdOrderByNumeroVersionDesc(UUID conceptoId, UUID periodoId);

    List<ConfiguracionConceptoTributario> findByConceptoIdAndPeriodoIdAndEstado(UUID conceptoId, UUID periodoId, EstadoConfiguracionConcepto estado);

    long countByEstado(EstadoConfiguracionConcepto estado);

    @Query("SELECT COUNT(c) > 0 FROM ConfiguracionConceptoTributario c " +
           "WHERE c.concepto.id = :conceptoId " +
           "AND c.periodo.id = :periodoId " +
           "AND c.estado = 'ACTIVO' " +
           "AND c.id <> :excludeId " +
           "AND (:sectorId IS NULL OR c.sector.id IS NULL OR c.sector.id = :sectorId) " +
           "AND ((:fechaFin IS NULL AND (c.fechaFinVigencia IS NULL OR c.fechaFinVigencia >= :fechaInicio)) OR " +
           "     (:fechaFin IS NOT NULL AND c.fechaInicioVigencia <= :fechaFin AND (c.fechaFinVigencia IS NULL OR c.fechaFinVigencia >= :fechaInicio)))")
    boolean existeSolapamientoActivo(@Param("conceptoId") UUID conceptoId,
                                    @Param("periodoId") UUID periodoId,
                                    @Param("fechaInicio") LocalDate fechaInicio,
                                    @Param("fechaFin") LocalDate fechaFin,
                                    @Param("sectorId") UUID sectorId,
                                    @Param("excludeId") UUID excludeId);
}
