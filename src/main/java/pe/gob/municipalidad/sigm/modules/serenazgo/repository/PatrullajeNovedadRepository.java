package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PatrullajeNovedad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoNovedadPatrullaje;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatrullajeNovedadRepository extends JpaRepository<PatrullajeNovedad, UUID> {

    List<PatrullajeNovedad> findByPatrullajeIdOrderByFechaHoraDesc(UUID patrullajeId);

    List<PatrullajeNovedad> findByPatrullajeIdAndTipoNovedad(UUID patrullajeId, TipoNovedadPatrullaje tipoNovedad);

    long countByPatrullajeId(UUID patrullajeId);
}
