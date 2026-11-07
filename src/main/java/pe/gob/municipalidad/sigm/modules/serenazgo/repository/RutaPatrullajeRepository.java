package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.RutaPatrullaje;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RutaPatrullajeRepository extends JpaRepository<RutaPatrullaje, UUID>, JpaSpecificationExecutor<RutaPatrullaje> {

    @Query(value = "SELECT nextval('seq_serenazgo_rutas_patrullaje')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<RutaPatrullaje> findByCodigo(String codigo);

    List<RutaPatrullaje> findBySectorId(UUID sectorId);

    List<RutaPatrullaje> findBySectorIdAndEstado(UUID sectorId, String estado);

    List<RutaPatrullaje> findByEstadoOrderByNombreAsc(String estado);

    boolean existsByCodigo(String codigo);
}
