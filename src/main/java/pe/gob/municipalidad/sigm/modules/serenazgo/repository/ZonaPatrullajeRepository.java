package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ZonaPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZonaPatrullajeRepository extends JpaRepository<ZonaPatrullaje, UUID>, JpaSpecificationExecutor<ZonaPatrullaje> {

    @Query(value = "SELECT nextval('seq_serenazgo_zona')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    List<ZonaPatrullaje> findBySectorIdAndEstado(UUID sectorId, EstadoSectorPatrullaje estado);

    List<ZonaPatrullaje> findBySectorId(UUID sectorId);

    Optional<ZonaPatrullaje> findBySectorIdAndCodigo(UUID sectorId, String codigo);

    long countByEstado(EstadoSectorPatrullaje estado);

    long countByTieneDelimitacionFalse();

    long countByTieneDelimitacionTrue();

    List<ZonaPatrullaje> findByTieneDelimitacionTrue();

    boolean existsBySectorIdAndCodigo(UUID sectorId, String codigo);

    boolean existsBySectorIdAndCodigoAndIdNot(UUID sectorId, String codigo, UUID id);

    boolean existsBySectorIdAndNombreIgnoreCase(UUID sectorId, String nombre);

    boolean existsBySectorIdAndNombreIgnoreCaseAndIdNot(UUID sectorId, String nombre, UUID id);
}
