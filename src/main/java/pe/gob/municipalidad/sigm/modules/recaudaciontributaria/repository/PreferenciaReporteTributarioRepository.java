package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PreferenciaReporteTributario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreferenciaReporteTributarioRepository extends JpaRepository<PreferenciaReporteTributario, UUID> {

    List<PreferenciaReporteTributario> findByUsuarioIdAndReporteCodigoOrderByNombreConfiguracionAsc(Long usuarioId, String reporteCodigo);

    Optional<PreferenciaReporteTributario> findFirstByUsuarioIdAndReporteCodigoAndEsPredeterminadoTrue(Long usuarioId, String reporteCodigo);
}

