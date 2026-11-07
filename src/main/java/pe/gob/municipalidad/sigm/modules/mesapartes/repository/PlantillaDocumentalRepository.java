package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PlantillaDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDocumentalPlantilla;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaDocumentalRepository extends JpaRepository<PlantillaDocumental, Long> {

    Optional<PlantillaDocumental> findByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<PlantillaDocumental> findByTipoDocumentalAndEstado(TipoDocumentalPlantilla tipoDocumental, String estado);

    List<PlantillaDocumental> findAllByOrderByCodigoAsc();
}
