package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.VinculoUsuarioComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVinculoComerciante;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VinculoUsuarioComercianteRepository extends JpaRepository<VinculoUsuarioComerciante, UUID> {

    @Query("SELECT v FROM VinculoUsuarioComerciante v " +
           "JOIN FETCH v.comerciante c " +
           "LEFT JOIN FETCH c.persona p " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "WHERE v.usuario.id = :usuarioId AND v.estado = :estado")
    List<VinculoUsuarioComerciante> findByUsuarioIdAndEstado(
            @Param("usuarioId") Long usuarioId,
            @Param("estado") EstadoVinculoComerciante estado);

    @Query("SELECT v FROM VinculoUsuarioComerciante v " +
           "JOIN FETCH v.comerciante c " +
           "LEFT JOIN FETCH c.persona p " +
           "LEFT JOIN FETCH c.personaJuridica pj " +
           "WHERE v.usuario.id = :usuarioId")
    List<VinculoUsuarioComerciante> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    Optional<VinculoUsuarioComerciante> findByUsuarioIdAndComercianteId(Long usuarioId, UUID comercianteId);

    boolean existsByUsuarioIdAndComercianteId(Long usuarioId, UUID comercianteId);

    List<VinculoUsuarioComerciante> findByComercianteId(UUID comercianteId);
}
