package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ElementoPlanoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ElementoPlanoMercadoRepository extends JpaRepository<ElementoPlanoMercado, UUID> {

    List<ElementoPlanoMercado> findByVersionPlanoIdOrderByOrdenCapaAscCoordenadaYAscCoordenadaXAsc(UUID versionPlanoId);

    List<ElementoPlanoMercado> findByVersionPlanoIdAndTipoElemento(UUID versionPlanoId, TipoElementoPlano tipoElemento);

    Optional<ElementoPlanoMercado> findByVersionPlanoIdAndReferenciaEntidadId(UUID versionPlanoId, UUID referenciaEntidadId);

    boolean existsByVersionPlanoIdAndReferenciaEntidadIdAndTipoElemento(UUID versionPlanoId, UUID referenciaEntidadId, TipoElementoPlano tipoElemento);

    boolean existsByVersionPlanoIdAndReferenciaEntidadIdAndTipoElementoAndIdNot(UUID versionPlanoId, UUID referenciaEntidadId, TipoElementoPlano tipoElemento, UUID id);

    long countByVersionPlanoId(UUID versionPlanoId);

    long countByVersionPlanoIdAndTipoElemento(UUID versionPlanoId, TipoElementoPlano tipoElemento);

    @Modifying
    @Query("DELETE FROM ElementoPlanoMercado e WHERE e.versionPlano.id = :versionPlanoId")
    void deleteByVersionPlanoId(@Param("versionPlanoId") UUID versionPlanoId);
}
