package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PabellonCambioEstado;

import java.util.List;
import java.util.UUID;

@Repository
public interface PabellonCambioEstadoRepository extends JpaRepository<PabellonCambioEstado, UUID> {

    List<PabellonCambioEstado> findByPabellonIdOrderByFechaCambioDesc(UUID pabellonId);
}
