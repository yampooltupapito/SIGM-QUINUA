package pe.gob.municipalidad.sigm.modules.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalConfiguracion;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalConfiguracionRepository extends JpaRepository<PortalConfiguracion, Long> {

    Optional<PortalConfiguracion> findByClave(String clave);

    List<PortalConfiguracion> findByGrupo(String grupo);
}
