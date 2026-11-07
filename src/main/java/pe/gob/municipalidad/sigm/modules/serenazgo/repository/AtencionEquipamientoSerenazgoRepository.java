package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AtencionEquipamientoSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtencionEquipamientoSerenazgoRepository extends JpaRepository<AtencionEquipamientoSerenazgo, UUID> {

    List<AtencionEquipamientoSerenazgo> findByAtencionIdOrderByFechaAsignacionAsc(UUID atencionId);

    List<AtencionEquipamientoSerenazgo> findByAtencionIdAndDevueltoFalse(UUID atencionId);
}
