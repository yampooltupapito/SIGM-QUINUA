package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialExportacionReporteTributario;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialExportacionReporteTributarioRepository extends JpaRepository<HistorialExportacionReporteTributario, UUID> {

    Page<HistorialExportacionReporteTributario> findByUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId, Pageable pageable);

    Page<HistorialExportacionReporteTributario> findAllByOrderByFechaSolicitudDesc(Pageable pageable);

    List<HistorialExportacionReporteTributario> findTop10ByOrderByFechaSolicitudDesc();
}

