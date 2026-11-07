package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ReporteMercadoConfiguracion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReporteMercadoConfiguracionRepository extends JpaRepository<ReporteMercadoConfiguracion, UUID> {

    Optional<ReporteMercadoConfiguracion> findByCodigo(String codigo);

    List<ReporteMercadoConfiguracion> findByEstadoOrderByCategoriaAscNombreAsc(String estado);

    List<ReporteMercadoConfiguracion> findByCategoriaAndEstado(String categoria, String estado);
}
