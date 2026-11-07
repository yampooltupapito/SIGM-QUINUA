package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComercianteRepository extends JpaRepository<Comerciante, UUID>, JpaSpecificationExecutor<Comerciante> {

    Optional<Comerciante> findByPersonaId(Long personaId);

    Optional<Comerciante> findByUsuarioId(Long usuarioId);

    boolean existsByPersonaId(Long personaId);

    Optional<Comerciante> findByPersonaJuridicaId(UUID personaJuridicaId);

    boolean existsByPersonaJuridicaId(UUID personaJuridicaId);

    Optional<Comerciante> findByCodigoComerciante(String codigoComerciante);

    boolean existsByCodigoComerciante(String codigoComerciante);

    boolean existsByCodigoComercianteAndIdNot(String codigoComerciante, UUID id);

    Page<Comerciante> findByEstado(EstadoComerciante estado, Pageable pageable);

    long countByEstado(EstadoComerciante estado);

    long countByTipoComerciante(TipoComerciante tipoComerciante);

    @Query("SELECT c FROM Comerciante c " +
           "LEFT JOIN c.persona p " +
           "LEFT JOIN c.personaJuridica pj " +
           "WHERE LOWER(c.codigoComerciante) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR (p IS NOT NULL AND (LOWER(p.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "    OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "    OR LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :termino, '%')))) " +
           "OR (pj IS NOT NULL AND (LOWER(pj.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "    OR LOWER(pj.ruc) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "    OR (pj.nombreComercial IS NOT NULL AND LOWER(pj.nombreComercial) LIKE LOWER(CONCAT('%', :termino, '%')))))")
    Page<Comerciante> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT c FROM Comerciante c " +
           "LEFT JOIN c.persona p " +
           "LEFT JOIN c.personaJuridica pj " +
           "WHERE ((LOWER(c.codigoComerciante) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "    OR (p IS NOT NULL AND (LOWER(p.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "        OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "        OR LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :termino, '%')))) " +
           "    OR (pj IS NOT NULL AND (LOWER(pj.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "        OR LOWER(pj.ruc) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "        OR (pj.nombreComercial IS NOT NULL AND LOWER(pj.nombreComercial) LIKE LOWER(CONCAT('%', :termino, '%')))))) " +
           "AND c.estado = :estado")
    Page<Comerciante> buscarPorTerminoYEstado(
            @Param("termino") String termino,
            @Param("estado") EstadoComerciante estado,
            Pageable pageable
    );

    @Query(value = "SELECT nextval('seq_mercados_codigo_comerciante')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    @Query("SELECT DISTINCT c FROM Comerciante c " +
           "LEFT JOIN FETCH c.persona p " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "ORDER BY c.codigoComerciante ASC")
    List<Comerciante> findAllConRelaciones();

    @Query("SELECT c FROM Comerciante c " +
           "LEFT JOIN FETCH c.persona p " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "WHERE c.id = :id")
    Optional<Comerciante> findByIdConRelaciones(@Param("id") UUID id);
}
