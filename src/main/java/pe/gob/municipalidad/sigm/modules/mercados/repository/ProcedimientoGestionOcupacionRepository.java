package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ProcedimientoGestionOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedimientoGestionOcupacionRepository extends JpaRepository<ProcedimientoGestionOcupacion, Long> {

    Optional<ProcedimientoGestionOcupacion> findByCodigo(String codigo);

    List<ProcedimientoGestionOcupacion> findByTipoSolicitudAndActivoTrue(TipoSolicitudGestion tipoSolicitud);

    List<ProcedimientoGestionOcupacion> findByActivoTrueOrderByNombreAsc();
}
