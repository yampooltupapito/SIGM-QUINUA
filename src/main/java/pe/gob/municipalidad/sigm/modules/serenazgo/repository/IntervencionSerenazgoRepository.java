package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIntervencionSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntervencionSerenazgoRepository extends JpaRepository<IntervencionSerenazgo, UUID>, JpaSpecificationExecutor<IntervencionSerenazgo> {

    @Query(value = "SELECT nextval('seq_serenazgo_intervencion')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<IntervencionSerenazgo> findByCodigo(String codigo);

    List<IntervencionSerenazgo> findByIncidenciaId(UUID incidenciaId);

    List<IntervencionSerenazgo> findByAtencionId(UUID atencionId);

    List<IntervencionSerenazgo> findByResponsableId(UUID responsableId);

    Page<IntervencionSerenazgo> findByEstado(EstadoIntervencionSerenazgo estado, Pageable pageable);

    long countByEstado(EstadoIntervencionSerenazgo estado);

    boolean existsByCodigo(String codigo);

    @Query("SELECT COUNT(i) FROM IntervencionSerenazgo i WHERE i.estado = :estado")
    long contarPorEstado(@Param("estado") EstadoIntervencionSerenazgo estado);
}
