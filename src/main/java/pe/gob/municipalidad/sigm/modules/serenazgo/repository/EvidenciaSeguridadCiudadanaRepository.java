package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.EvidenciaSeguridadCiudadana;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenciaSeguridadCiudadanaRepository extends JpaRepository<EvidenciaSeguridadCiudadana, UUID> {
    List<EvidenciaSeguridadCiudadana> findByIncidenciaIdOrderByFechaRegistroDesc(UUID incidenciaId);
    List<EvidenciaSeguridadCiudadana> findByIntervencionIdOrderByFechaRegistroDesc(UUID intervencionId);
    List<EvidenciaSeguridadCiudadana> findByPatrullajeIdOrderByFechaRegistroDesc(UUID patrullajeId);
    long countByIncidenciaId(UUID incidenciaId);
}
