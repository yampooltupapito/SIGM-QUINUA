package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContribuyenteTributarioRepository extends JpaRepository<ContribuyenteTributario, UUID>, JpaSpecificationExecutor<ContribuyenteTributario> {

    @Query(value = "SELECT nextval('seq_tributaria_contribuyente')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<ContribuyenteTributario> findByCodigoContribuyente(String codigoContribuyente);

    Optional<ContribuyenteTributario> findByNumeroDocumento(String numeroDocumento);

    Optional<ContribuyenteTributario> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    Page<ContribuyenteTributario> findByEstado(EstadoContribuyente estado, Pageable pageable);

    @Query("SELECT c FROM ContribuyenteTributario c WHERE " +
           "(:query IS NULL OR :query = '' " +
           "OR LOWER(c.nombreRazonSocial) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.numeroDocumento) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.codigoContribuyente) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ContribuyenteTributario> buscarContribuyentes(@Param("query") String query, Pageable pageable);

    boolean existsByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(String tipoDocumento, String numeroDocumento, UUID id);

    boolean existsByCodigoContribuyente(String codigoContribuyente);

    long countByTipoContribuyente(TipoContribuyente tipoContribuyente);

    long countByEstado(EstadoContribuyente estado);

    @Query("SELECT c FROM ContribuyenteTributario c WHERE " +
           "LOWER(TRIM(c.nombreRazonSocial)) LIKE LOWER(CONCAT('%', TRIM(:nombre), '%')) " +
           "AND (:excludeId IS NULL OR c.id <> :excludeId)")
    List<ContribuyenteTributario> buscarPotencialesDuplicadosNombre(
            @Param("nombre") String nombre,
            @Param("excludeId") UUID excludeId);
}
