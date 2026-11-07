package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MedioNotificacionTributaria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedioNotificacionTributariaRepository extends JpaRepository<MedioNotificacionTributaria, UUID>, JpaSpecificationExecutor<MedioNotificacionTributaria> {

    Optional<MedioNotificacionTributaria> findByCodigo(String codigo);

    List<MedioNotificacionTributaria> findByEstadoOrderByNombreAsc(String estado);

    boolean existsByCodigo(String codigo);
}

