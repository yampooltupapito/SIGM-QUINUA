package pe.gob.municipalidad.sigm.modules.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalFaq;

import java.util.List;

@Repository
public interface PortalFaqRepository extends JpaRepository<PortalFaq, Long> {

    List<PortalFaq> findByEstadoOrderByCategoriaAscOrdenAsc(String estado);

    List<PortalFaq> findByEstadoAndCategoriaOrderByOrdenAsc(String estado, String categoria);

    List<PortalFaq> findAllByOrderByOrdenAsc();

    @Query("SELECT DISTINCT f.categoria FROM PortalFaq f WHERE f.estado = 'ACTIVO' ORDER BY f.categoria ASC")
    List<String> findDistinctCategoriasActivas();

    @Query("SELECT f FROM PortalFaq f WHERE f.estado = 'ACTIVO' AND " +
           "(LOWER(f.pregunta) LIKE :qPattern OR LOWER(f.respuesta) LIKE :qPattern) " +
           "ORDER BY f.orden ASC")
    List<PortalFaq> buscarFaqActivas(@Param("qPattern") String qPattern);
}
