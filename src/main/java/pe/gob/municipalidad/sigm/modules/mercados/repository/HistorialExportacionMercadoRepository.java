package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialExportacionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialExportacionMercadoRepository extends JpaRepository<HistorialExportacionMercado, UUID> {

    @Query("SELECT h FROM HistorialExportacionMercado h LEFT JOIN FETCH h.mercado WHERE h.usuario.id = :usuarioId ORDER BY h.fechaGeneracion DESC")
    Page<HistorialExportacionMercado> findByUsuarioIdOrderByFechaGeneracionDesc(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT h FROM HistorialExportacionMercado h LEFT JOIN FETCH h.mercado ORDER BY h.fechaGeneracion DESC")
    Page<HistorialExportacionMercado> findAllWithMercado(Pageable pageable);

    List<HistorialExportacionMercado> findTop10ByOrderByFechaGeneracionDesc();

    long countByTipoReporte(TipoReporteMercado tipoReporte);
}
