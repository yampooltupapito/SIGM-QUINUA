package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PlanoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPlano;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanoMercadoRepository extends JpaRepository<PlanoMercado, UUID> {

    Optional<PlanoMercado> findByMercadoId(UUID mercadoId);

    List<PlanoMercado> findAllByMercadoId(UUID mercadoId);

    Optional<PlanoMercado> findByMercadoIdAndEstado(UUID mercadoId, EstadoPlano estado);

    Optional<PlanoMercado> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, UUID id);

    boolean existsByMercadoId(UUID mercadoId);

    @Query("SELECT p FROM PlanoMercado p " +
           "JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH p.versionActual va " +
           "WHERE p.id = :id")
    Optional<PlanoMercado> findByIdConDetalle(@Param("id") UUID id);

    @Query("SELECT p FROM PlanoMercado p " +
           "JOIN FETCH p.mercado m " +
           "LEFT JOIN FETCH p.versionActual va " +
           "WHERE p.mercado.id = :mercadoId AND p.estado = 'ACTIVO'")
    Optional<PlanoMercado> findActivoPorMercadoConDetalle(@Param("mercadoId") UUID mercadoId);

    Page<PlanoMercado> findByEstado(EstadoPlano estado, Pageable pageable);
}
