package pe.gob.municipalidad.sigm.modules.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalServicio;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalServicioRepository extends JpaRepository<PortalServicio, Long> {

    List<PortalServicio> findByEstadoOrderByOrdenAsc(String estado);

    List<PortalServicio> findAllByOrderByOrdenAsc();

    Optional<PortalServicio> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);
}
