package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PlantillaInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantillaInspeccionRepository extends JpaRepository<PlantillaInspeccion, UUID> {

    boolean existsByCodigo(String codigo);

    Optional<PlantillaInspeccion> findByCodigo(String codigo);

    List<PlantillaInspeccion> findByEstado(String estado);

    List<PlantillaInspeccion> findByTipoInspeccionAndEstado(TipoInspeccionMercado tipo, String estado);

    @Query("SELECT p FROM PlantillaInspeccion p WHERE " +
           "(:estado IS NULL OR p.estado = :estado) AND " +
           "(:tipo IS NULL OR p.tipoInspeccion = :tipo) AND " +
           "(:q IS NULL OR LOWER(p.codigo) LIKE CAST(:q AS string) OR LOWER(p.nombre) LIKE CAST(:q AS string))")
    Page<PlantillaInspeccion> buscarPaginado(@Param("estado") String estado,
                                            @Param("tipo") TipoInspeccionMercado tipo,
                                            @Param("q") String q,
                                            Pageable pageable);
}
