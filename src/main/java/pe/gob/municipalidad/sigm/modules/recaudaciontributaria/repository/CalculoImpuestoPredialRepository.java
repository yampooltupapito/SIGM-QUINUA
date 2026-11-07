package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoPredial;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalculoImpuestoPredialRepository extends JpaRepository<CalculoImpuestoPredial, UUID>, JpaSpecificationExecutor<CalculoImpuestoPredial> {

    Optional<CalculoImpuestoPredial> findByCodigoCalculoIgnoreCase(String codigoCalculo);

    List<CalculoImpuestoPredial> findByContribuyenteIdOrderByFechaCalculoDesc(UUID contribuyenteId);

    List<CalculoImpuestoPredial> findByPeriodoId(UUID periodoId);

    List<CalculoImpuestoPredial> findByPeriodoIdOrderByFechaCalculoDesc(UUID periodoId);

    List<CalculoImpuestoPredial> findByContribuyenteIdAndPeriodoIdOrderByVersionCalculoDesc(UUID contribuyenteId, UUID periodoId);

    Optional<CalculoImpuestoPredial> findFirstByContribuyenteIdAndPeriodoIdAndEstadoOrderByFechaCalculoDesc(
            UUID contribuyenteId, UUID periodoId, EstadoCalculoPredial estado
    );

    long countByEstado(EstadoCalculoPredial estado);

    long countByTipoCalculo(TipoCalculoPredial tipoCalculo);

    long countByPeriodoId(UUID periodoId);

    @Query("SELECT COALESCE(SUM(c.impuestoAnualDeterminado), 0) FROM CalculoImpuestoPredial c WHERE c.estado = :estado")
    BigDecimal sumTotalImpuestoPorEstado(@Param("estado") EstadoCalculoPredial estado);

    @Query("SELECT COALESCE(SUM(c.baseImponibleNeta), 0) FROM CalculoImpuestoPredial c WHERE c.estado = :estado")
    BigDecimal sumTotalBaseImponiblePorEstado(@Param("estado") EstadoCalculoPredial estado);
}
