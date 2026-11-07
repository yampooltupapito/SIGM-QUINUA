package pe.gob.municipalidad.sigm.common.document.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    Optional<Documento> findByIdentificadorPublico(UUID identificadorPublico);

    Optional<Documento> findByNombreInterno(String nombreInterno);

    @Query("SELECT d FROM Documento d WHERE " +
           "(:estado IS NULL OR d.estado = :estado) AND " +
           "(:categoria IS NULL OR d.categoria = :categoria) AND " +
           "(:clasificacion IS NULL OR d.clasificacionAcceso = :clasificacion) AND " +
           "(:termino IS NULL OR LOWER(d.nombreOriginal) LIKE :termino " +
           "  OR (d.descripcion IS NOT NULL AND LOWER(d.descripcion) LIKE :termino)) AND " +
           "(:usuarioId IS NULL OR d.usuarioPropietario.id = :usuarioId OR d.clasificacionAcceso = pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso.PUBLICO) " +
           "ORDER BY d.fechaCreacion DESC")
    Page<Documento> buscarConFiltros(
            @Param("estado") EstadoDocumento estado,
            @Param("categoria") CategoriaDocumento categoria,
            @Param("clasificacion") ClasificacionAcceso clasificacion,
            @Param("termino") String termino,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.estado = 'ACTIVO'")
    long countActivos();

    @Query("SELECT COALESCE(SUM(d.tamanoBytes), 0) FROM Documento d WHERE d.estado = 'ACTIVO'")
    long sumTamanoBytesActivos();
}
