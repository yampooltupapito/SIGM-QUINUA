package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalculoArbitrioMunicipalRepository extends JpaRepository<CalculoArbitrioMunicipal, UUID>, JpaSpecificationExecutor<CalculoArbitrioMunicipal> {

    Optional<CalculoArbitrioMunicipal> findByCodigoCalculoIgnoreCase(String codigo);

    List<CalculoArbitrioMunicipal> findByContribuyenteIdOrderByFechaCalculoDesc(UUID contribuyenteId);

    List<CalculoArbitrioMunicipal> findByPredioIdOrderByFechaCalculoDesc(UUID predioId);

    List<CalculoArbitrioMunicipal> findByPeriodoId(UUID periodoId);

    org.springframework.data.domain.Page<CalculoArbitrioMunicipal> findByEstado(EstadoCalculoArbitrio estado, org.springframework.data.domain.Pageable pageable);

    List<CalculoArbitrioMunicipal> findByEstado(EstadoCalculoArbitrio estado);

    @Query("SELECT c FROM CalculoArbitrioMunicipal c WHERE c.predio.id = :predioId AND c.periodo.id = :periodoId AND c.estado = 'APROBADO'")
    Optional<CalculoArbitrioMunicipal> findAprobadoPorPredioYPeriodo(@Param("predioId") UUID predioId, @Param("periodoId") UUID periodoId);

    @Query("SELECT COUNT(c) FROM CalculoArbitrioMunicipal c WHERE c.estado = :estado")
    long countByEstado(@Param("estado") EstadoCalculoArbitrio estado);
}
