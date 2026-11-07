package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectorPatrullajeRepository extends JpaRepository<SectorPatrullaje, UUID>, JpaSpecificationExecutor<SectorPatrullaje> {

    @Query(value = "SELECT nextval('seq_serenazgo_sector')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<SectorPatrullaje> findByCodigo(String codigo);

    List<SectorPatrullaje> findByEstado(EstadoSectorPatrullaje estado);

    List<SectorPatrullaje> findByEstadoOrderByCodigoAsc(EstadoSectorPatrullaje estado);

    long countByEstado(EstadoSectorPatrullaje estado);

    long countByTieneDelimitacionTrue();

    List<SectorPatrullaje> findByTieneDelimitacionTrue();

    List<SectorPatrullaje> findByOficinaResponsableId(Long oficinaId);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, UUID id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
}
