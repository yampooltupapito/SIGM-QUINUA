package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConciliacionCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConciliacionCaja;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConciliacionCajaMunicipalRepository extends JpaRepository<ConciliacionCajaMunicipal, UUID>, JpaSpecificationExecutor<ConciliacionCajaMunicipal> {

    @Query(value = "SELECT nextval('seq_tributaria_conciliacion_caja')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<ConciliacionCajaMunicipal> findByCodigoConciliacion(String codigoConciliacion);

    Optional<ConciliacionCajaMunicipal> findBySesionCajaId(UUID sesionCajaId);

    Page<ConciliacionCajaMunicipal> findByEstado(EstadoConciliacionCaja estado, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ConciliacionCajaMunicipal c WHERE c.estado = 'PENDIENTE'")
    long countPendientes();

    @Query("SELECT COUNT(c) FROM ConciliacionCajaMunicipal c WHERE c.estado = 'CON_DIFERENCIAS' OR c.estado = 'OBSERVADA'")
    long countConDiferencias();
}
