package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CargoRecepcionTributaria;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CargoRecepcionTributariaRepository extends JpaRepository<CargoRecepcionTributaria, UUID> {

    Optional<CargoRecepcionTributaria> findByNotificacionId(UUID notificacionId);

    boolean existsByNotificacionId(UUID notificacionId);
}

