package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.RepresentanteContribuyente;

import java.util.List;
import java.util.UUID;

@Repository
public interface RepresentanteContribuyenteRepository extends JpaRepository<RepresentanteContribuyente, UUID> {

    List<RepresentanteContribuyente> findByContribuyenteIdOrderByCreatedAtDesc(UUID contribuyenteId);

    List<RepresentanteContribuyente> findByContribuyenteIdAndEstado(UUID contribuyenteId, pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRepresentanteLegal estado);
}
