package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.UnidadMovilSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnidadMovilSerenazgoRepository extends JpaRepository<UnidadMovilSerenazgo, UUID>, JpaSpecificationExecutor<UnidadMovilSerenazgo> {

    @Query(value = "SELECT nextval('seq_serenazgo_unidad')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<UnidadMovilSerenazgo> findByCodigoInterno(String codigoInterno);

    Optional<UnidadMovilSerenazgo> findByPlacaIgnoreCase(String placa);

    List<UnidadMovilSerenazgo> findByEstadoOperativo(EstadoUnidadMovil estadoOperativo);

    List<UnidadMovilSerenazgo> findByTipoUnidadAndEstadoOperativo(TipoUnidadMovil tipoUnidad, EstadoUnidadMovil estadoOperativo);

    Page<UnidadMovilSerenazgo> findByEstadoOperativo(EstadoUnidadMovil estadoOperativo, Pageable pageable);

    long countByEstadoOperativo(EstadoUnidadMovil estadoOperativo);

    boolean existsByCodigoInterno(String codigoInterno);

    boolean existsByCodigoInternoAndIdNot(String codigoInterno, UUID id);

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, UUID id);
}
