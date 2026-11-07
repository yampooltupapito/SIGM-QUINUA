package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionArbitrio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionArbitrioMunicipalRepository extends JpaRepository<ConfiguracionArbitrioMunicipal, UUID>, JpaSpecificationExecutor<ConfiguracionArbitrioMunicipal> {

    Optional<ConfiguracionArbitrioMunicipal> findByCodigoConfiguracionIgnoreCase(String codigo);

    List<ConfiguracionArbitrioMunicipal> findByPeriodoId(UUID periodoId);

    List<ConfiguracionArbitrioMunicipal> findByPeriodoIdAndEstado(UUID periodoId, EstadoConfiguracionArbitrio estado);

    @Query("SELECT c FROM ConfiguracionArbitrioMunicipal c WHERE c.periodo.anio = :anio AND c.estado = 'ACTIVO'")
    List<ConfiguracionArbitrioMunicipal> findActivasPorAnio(@Param("anio") Integer anio);

    @Query("SELECT c FROM ConfiguracionArbitrioMunicipal c WHERE c.servicio.id = :servicioId AND c.periodo.id = :periodoId ORDER BY c.versionConfiguracion DESC")
    List<ConfiguracionArbitrioMunicipal> findByServicioAndPeriodoOrderByVersionDesc(@Param("servicioId") UUID servicioId, @Param("periodoId") UUID periodoId);

    @Query("SELECT COUNT(c) FROM ConfiguracionArbitrioMunicipal c WHERE c.estado = :estado")
    long countByEstado(@Param("estado") EstadoConfiguracionArbitrio estado);
}
