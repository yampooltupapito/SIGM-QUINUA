package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatrullajeSerenazgoRepository extends JpaRepository<PatrullajeSerenazgo, UUID>, JpaSpecificationExecutor<PatrullajeSerenazgo> {

    @Query(value = "SELECT nextval('seq_serenazgo_patrullaje')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<PatrullajeSerenazgo> findByCodigo(String codigo);

    List<PatrullajeSerenazgo> findByEstado(EstadoPatrullajeSerenazgo estado);

    Page<PatrullajeSerenazgo> findByEstado(EstadoPatrullajeSerenazgo estado, Pageable pageable);

    List<PatrullajeSerenazgo> findBySectorId(UUID sectorId);

    List<PatrullajeSerenazgo> findByResponsableId(UUID responsableId);

    List<PatrullajeSerenazgo> findByRutaId(UUID rutaId);

    long countByEstado(EstadoPatrullajeSerenazgo estado);

    boolean existsByCodigo(String codigo);

    boolean existsByRutaId(UUID rutaId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PatrullajeSerenazgo p " +
           "WHERE p.responsable.id = :personalId AND p.estado IN :estados AND (:patrullajeId IS NULL OR p.id <> :patrullajeId)")
    boolean existePatrullajeActivoParaResponsable(@Param("personalId") UUID personalId,
                                                @Param("estados") Collection<EstadoPatrullajeSerenazgo> estados,
                                                @Param("patrullajeId") UUID patrullajeId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PatrullajeSerenazgo p " +
           "WHERE p.unidadMovil.id = :unidadId AND p.estado IN :estados AND (:patrullajeId IS NULL OR p.id <> :patrullajeId)")
    boolean existePatrullajeActivoParaUnidad(@Param("unidadId") UUID unidadId,
                                            @Param("estados") Collection<EstadoPatrullajeSerenazgo> estados,
                                            @Param("patrullajeId") UUID patrullajeId);

    @Query("SELECT p FROM PatrullajeSerenazgo p " +
           "LEFT JOIN FETCH p.sector " +
           "LEFT JOIN FETCH p.zona " +
           "LEFT JOIN FETCH p.ruta " +
           "LEFT JOIN FETCH p.responsable " +
           "LEFT JOIN FETCH p.unidadMovil " +
           "WHERE p.id = :id")
    Optional<PatrullajeSerenazgo> findByIdWithRelations(@Param("id") UUID id);
}
