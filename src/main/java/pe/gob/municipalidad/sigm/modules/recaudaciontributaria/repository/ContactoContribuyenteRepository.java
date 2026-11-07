package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContactoContribuyente;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactoContribuyenteRepository extends JpaRepository<ContactoContribuyente, UUID> {

    List<ContactoContribuyente> findByContribuyenteIdOrderByCreatedAtDesc(UUID contribuyenteId);

    List<ContactoContribuyente> findByContribuyenteIdAndEstado(UUID contribuyenteId, String estado);
}
