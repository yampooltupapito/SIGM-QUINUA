package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.EvidenciaIncidenciaMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenciaIncidenciaMercadoRepository extends JpaRepository<EvidenciaIncidenciaMercado, UUID> {

    List<EvidenciaIncidenciaMercado> findByIncidenciaIdOrderByFechaRegistroAsc(UUID incidenciaId);
}
