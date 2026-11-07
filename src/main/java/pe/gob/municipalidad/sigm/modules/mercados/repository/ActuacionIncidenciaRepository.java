package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActuacionIncidenciaMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActuacionIncidenciaRepository extends JpaRepository<ActuacionIncidenciaMercado, UUID> {

    List<ActuacionIncidenciaMercado> findByIncidenciaIdOrderByFechaAsc(UUID incidenciaId);
}
