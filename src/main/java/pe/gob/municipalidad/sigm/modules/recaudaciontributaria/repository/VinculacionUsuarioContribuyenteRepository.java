package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.VinculacionUsuarioContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VinculacionUsuarioContribuyenteRepository
        extends JpaRepository<VinculacionUsuarioContribuyente, UUID>,
                JpaSpecificationExecutor<VinculacionUsuarioContribuyente> {

    List<VinculacionUsuarioContribuyente> findByUsuarioIdAndActivoTrue(Long usuarioId);

    List<VinculacionUsuarioContribuyente> findByUsuarioIdAndEstadoAndActivoTrue(
            Long usuarioId, EstadoVinculacionContribuyente estado);

    Optional<VinculacionUsuarioContribuyente> findByUsuarioIdAndContribuyenteId(
            Long usuarioId, UUID contribuyenteId);

    Optional<VinculacionUsuarioContribuyente> findByUsuarioIdAndContribuyenteIdAndEstadoAndActivoTrue(
            Long usuarioId, UUID contribuyenteId, EstadoVinculacionContribuyente estado);

    Page<VinculacionUsuarioContribuyente> findByEstado(
            EstadoVinculacionContribuyente estado, Pageable pageable);

    @Query("SELECT v FROM VinculacionUsuarioContribuyente v WHERE " +
           "(:estado IS NULL OR v.estado = :estado) AND " +
           "(LOWER(v.contribuyente.codigoContribuyente) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(v.contribuyente.nombreRazonSocial) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(v.usuario.username) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<VinculacionUsuarioContribuyente> buscarVinculacionesAdmin(
            @Param("estado") EstadoVinculacionContribuyente estado,
            @Param("q") String q,
            Pageable pageable);
}

