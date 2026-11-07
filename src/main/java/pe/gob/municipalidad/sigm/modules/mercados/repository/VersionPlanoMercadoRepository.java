package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.VersionPlanoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VersionPlanoMercadoRepository extends JpaRepository<VersionPlanoMercado, UUID> {

    List<VersionPlanoMercado> findByPlanoIdOrderByNumeroVersionDesc(UUID planoId);

    Optional<VersionPlanoMercado> findByPlanoIdAndNumeroVersion(UUID planoId, Integer numeroVersion);

    Optional<VersionPlanoMercado> findByPlanoIdAndEstado(UUID planoId, EstadoVersionPlano estado);

    List<VersionPlanoMercado> findByPlanoIdAndEstadoOrderByNumeroVersionDesc(UUID planoId, EstadoVersionPlano estado);

    Optional<VersionPlanoMercado> findTopByPlanoIdOrderByNumeroVersionDesc(UUID planoId);

    @Query("SELECT v FROM VersionPlanoMercado v " +
           "JOIN FETCH v.plano p " +
           "JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH v.elementos e " +
           "WHERE v.id = :id")
    Optional<VersionPlanoMercado> findByIdConElementos(@Param("id") UUID id);

    @Query("SELECT v FROM VersionPlanoMercado v " +
           "JOIN FETCH v.plano p " +
           "WHERE p.id = :planoId AND v.estado = 'PUBLICADA'")
    Optional<VersionPlanoMercado> findVersionPublicadaPorPlano(@Param("planoId") UUID planoId);
}
