package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ReporteEstadisticoSerenazgo;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReporteEstadisticoSerenazgoRepository extends JpaRepository<ReporteEstadisticoSerenazgo, UUID>, JpaSpecificationExecutor<ReporteEstadisticoSerenazgo> {

    @Query(value = "SELECT nextval('seq_serenazgo_reportes_estadisticos')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<ReporteEstadisticoSerenazgo> findByCodigo(String codigo);

    Page<ReporteEstadisticoSerenazgo> findAllByOrderByFechaGeneracionDesc(Pageable pageable);
}
