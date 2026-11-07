package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPredio;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoPredioRepository extends JpaRepository<DocumentoPredio, UUID> {

    List<DocumentoPredio> findByPredioIdOrderByCreatedAtDesc(UUID predioId);
}
