package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.EquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipamientoSerenazgoRepository extends JpaRepository<EquipamientoSerenazgo, UUID>, JpaSpecificationExecutor<EquipamientoSerenazgo> {

    @Query(value = "SELECT nextval('seq_serenazgo_equipamiento')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<EquipamientoSerenazgo> findByCodigoInterno(String codigoInterno);

    List<EquipamientoSerenazgo> findByTipoEquipoAndEstado(TipoEquipamientoSerenazgo tipoEquipo, EstadoEquipamientoSerenazgo estado);

    List<EquipamientoSerenazgo> findByResponsableId(UUID responsableId);

    List<EquipamientoSerenazgo> findByUnidadMovilId(UUID unidadMovilId);

    List<EquipamientoSerenazgo> findByEstado(EstadoEquipamientoSerenazgo estado);

    Page<EquipamientoSerenazgo> findByEstado(EstadoEquipamientoSerenazgo estado, Pageable pageable);

    long countByEstado(EstadoEquipamientoSerenazgo estado);

    boolean existsByCodigoInterno(String codigoInterno);

    boolean existsByCodigoInternoAndIdNot(String codigoInterno, UUID id);
}
