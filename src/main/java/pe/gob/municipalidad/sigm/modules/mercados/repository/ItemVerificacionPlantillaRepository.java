package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ItemVerificacionPlantilla;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemVerificacionPlantillaRepository extends JpaRepository<ItemVerificacionPlantilla, UUID> {

    List<ItemVerificacionPlantilla> findByPlantillaIdOrderByOrdenAsc(UUID plantillaId);

    boolean existsByPlantillaIdAndCodigo(UUID plantillaId, String codigo);
}
