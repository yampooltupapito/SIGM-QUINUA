package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.EvidenciaInspeccionMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenciaInspeccionMercadoRepository extends JpaRepository<EvidenciaInspeccionMercado, UUID> {

    List<EvidenciaInspeccionMercado> findByInspeccionIdOrderByFechaSubidaDesc(UUID inspeccionId);

    List<EvidenciaInspeccionMercado> findByHallazgoId(UUID hallazgoId);
}
