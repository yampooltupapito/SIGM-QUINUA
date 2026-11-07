package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PuntoControlRuta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PuntoControlRutaRepository extends JpaRepository<PuntoControlRuta, UUID> {

    List<PuntoControlRuta> findByRutaId(UUID rutaId);

    List<PuntoControlRuta> findByRutaSectorId(UUID sectorId);

    List<PuntoControlRuta> findByRutaIdOrderByOrdenAsc(UUID rutaId);

    List<PuntoControlRuta> findByRutaIdAndActivoTrueOrderByOrdenAsc(UUID rutaId);

    Optional<PuntoControlRuta> findByRutaIdAndOrden(UUID rutaId, Integer orden);

    Optional<PuntoControlRuta> findByRutaIdAndCodigo(UUID rutaId, String codigo);
}
