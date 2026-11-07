package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialEquipamientoSerenazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialEquipamientoSerenazgoRepository extends JpaRepository<HistorialEquipamientoSerenazgo, UUID> {
    List<HistorialEquipamientoSerenazgo> findByEquipamientoIdOrderByFechaEntregaDesc(UUID equipamientoId);
    List<HistorialEquipamientoSerenazgo> findByPersonalIdOrderByFechaEntregaDesc(UUID personalId);
}
