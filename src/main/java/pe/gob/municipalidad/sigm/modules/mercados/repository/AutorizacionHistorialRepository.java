package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AutorizacionHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface AutorizacionHistorialRepository extends JpaRepository<AutorizacionHistorial, Long> {

    List<AutorizacionHistorial> findByAutorizacionIdOrderByFechaAsc(UUID autorizacionId);
}
