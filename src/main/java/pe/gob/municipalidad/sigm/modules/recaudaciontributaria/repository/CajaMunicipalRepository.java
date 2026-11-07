package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CajaMunicipalRepository extends JpaRepository<CajaMunicipal, UUID>, JpaSpecificationExecutor<CajaMunicipal> {

    @Query(value = "SELECT nextval('seq_tributaria_caja')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<CajaMunicipal> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<CajaMunicipal> findByEstado(EstadoCajaMunicipal estado);

    Page<CajaMunicipal> findByEstado(EstadoCajaMunicipal estado, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CajaMunicipal c WHERE c.estado = 'ACTIVA'")
    long countCajasActivas();
}
