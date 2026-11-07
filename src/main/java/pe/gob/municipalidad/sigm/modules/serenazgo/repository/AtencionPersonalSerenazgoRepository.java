package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AtencionPersonalSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtencionPersonalSerenazgoRepository extends JpaRepository<AtencionPersonalSerenazgo, UUID> {

    List<AtencionPersonalSerenazgo> findByAtencionIdOrderByFechaAsignacionAsc(UUID atencionId);

    List<AtencionPersonalSerenazgo> findByAtencionIdAndActivoTrue(UUID atencionId);

    Optional<AtencionPersonalSerenazgo> findByAtencionIdAndPersonalIdAndActivoTrue(UUID atencionId, UUID personalId);

    Optional<AtencionPersonalSerenazgo> findByAtencionIdAndEsResponsableTrueAndActivoTrue(UUID atencionId);
}
