package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.GestionSeguimientoDeuda;

import java.util.List;
import java.util.UUID;

@Repository
public interface GestionSeguimientoDeudaRepository extends JpaRepository<GestionSeguimientoDeuda, UUID> {

    List<GestionSeguimientoDeuda> findBySeguimientoIdOrderByFechaGestionDesc(UUID seguimientoId);
}
