package pe.gob.municipalidad.sigm.modules.portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalNoticia;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalNoticiaRepository extends JpaRepository<PortalNoticia, Long> {

    Optional<PortalNoticia> findBySlug(String slug);

    Optional<PortalNoticia> findBySlugAndEstado(String slug, String estado);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<PortalNoticia> findByEstadoOrderByFechaPublicacionDesc(String estado, Pageable pageable);

    Page<PortalNoticia> findByEstadoAndTipoOrderByFechaPublicacionDesc(String estado, String tipo, Pageable pageable);

    List<PortalNoticia> findTop3ByEstadoAndDestacadaTrueOrderByFechaPublicacionDesc(String estado);

    List<PortalNoticia> findTop5ByEstadoOrderByFechaPublicacionDesc(String estado);

    @Query("SELECT n FROM PortalNoticia n WHERE " +
           "(:estado IS NULL OR n.estado = :estado) AND " +
           "(:tipo IS NULL OR n.tipo = :tipo) AND " +
           "(LOWER(n.titulo) LIKE :qPattern OR LOWER(n.resumen) LIKE :qPattern) " +
           "ORDER BY n.fechaPublicacion DESC NULLS LAST, n.createdAt DESC")
    Page<PortalNoticia> buscarNoticiasConTermino(@Param("estado") String estado,
                                                @Param("tipo") String tipo,
                                                @Param("qPattern") String qPattern,
                                                Pageable pageable);

    @Query("SELECT n FROM PortalNoticia n WHERE " +
           "(:estado IS NULL OR n.estado = :estado) AND " +
           "(:tipo IS NULL OR n.tipo = :tipo) " +
           "ORDER BY n.fechaPublicacion DESC NULLS LAST, n.createdAt DESC")
    Page<PortalNoticia> buscarNoticiasSinTermino(@Param("estado") String estado,
                                                @Param("tipo") String tipo,
                                                Pageable pageable);
}
