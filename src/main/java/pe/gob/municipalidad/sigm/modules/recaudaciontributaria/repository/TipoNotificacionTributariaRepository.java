package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.TipoNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaTipoNotificacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoNotificacionTributariaRepository extends JpaRepository<TipoNotificacionTributaria, UUID>, JpaSpecificationExecutor<TipoNotificacionTributaria> {

    Optional<TipoNotificacionTributaria> findByCodigo(String codigo);

    List<TipoNotificacionTributaria> findByEstadoOrderByNombreAsc(String estado);

    List<TipoNotificacionTributaria> findByCategoriaAndEstado(CategoriaTipoNotificacion categoria, String estado);

    boolean existsByCodigo(String codigo);
}

