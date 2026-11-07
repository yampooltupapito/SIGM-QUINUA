package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoPredio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PredioTributarioRepository extends JpaRepository<PredioTributario, UUID>, JpaSpecificationExecutor<PredioTributario> {

    @Query(value = "SELECT nextval('seq_tributaria_predio')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<PredioTributario> findByCodigoPredio(String codigoPredio);

    Optional<PredioTributario> findByCodigoCatastral(String codigoCatastral);

    Page<PredioTributario> findByEstado(EstadoPredio estado, Pageable pageable);

    Page<PredioTributario> findByTipoPredio(TipoPredio tipoPredio, Pageable pageable);

    @Query("SELECT p FROM PredioTributario p WHERE " +
           "(:query IS NULL OR :query = '' " +
           "OR LOWER(p.codigoPredio) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.direccion) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(COALESCE(p.codigoCatastral, '')) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(COALESCE(p.sectorNombre, '')) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(COALESCE(p.comunidadCentroPoblado, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<PredioTributario> buscarPredios(@Param("query") String query, Pageable pageable);

    boolean existsByCodigoPredio(String codigoPredio);

    boolean existsByCodigoCatastral(String codigoCatastral);

    boolean existsByCodigoCatastralIgnoreCase(String codigoCatastral);

    boolean existsByCodigoCatastralAndIdNot(String codigoCatastral, UUID id);

    boolean existsByCodigoCatastralIgnoreCaseAndIdNot(String codigoCatastral, UUID id);

    long countByTipoPredio(TipoPredio tipoPredio);

    long countByEstado(EstadoPredio estado);

    @Query("SELECT p FROM PredioTributario p WHERE " +
           "LOWER(TRIM(p.direccion)) LIKE LOWER(CONCAT('%', TRIM(:direccion), '%')) " +
           "AND (:excludeId IS NULL OR p.id <> :excludeId)")
    List<PredioTributario> buscarPotencialesDuplicadosDireccion(
            @Param("direccion") String direccion,
            @Param("excludeId") UUID excludeId);
}
