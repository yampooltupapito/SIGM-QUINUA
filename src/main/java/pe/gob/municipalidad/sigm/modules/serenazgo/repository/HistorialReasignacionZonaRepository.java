package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialReasignacionZona;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialReasignacionZonaRepository extends JpaRepository<HistorialReasignacionZona, UUID> {

    List<HistorialReasignacionZona> findByZonaIdOrderByCreatedAtDesc(UUID zonaId);
}
