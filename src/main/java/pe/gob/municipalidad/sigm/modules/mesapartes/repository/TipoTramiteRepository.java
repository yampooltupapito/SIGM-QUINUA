package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoTramiteRepository extends JpaRepository<TipoTramite, Long> {

    Optional<TipoTramite> findByCodigo(String codigo);

    Optional<TipoTramite> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<TipoTramite> findByEstado(String estado);

    List<TipoTramite> findByEstadoOrderByNombreAsc(String estado);

    List<TipoTramite> findAllByOrderByNombreAsc();

    List<TipoTramite> findByPermitePresentacionVirtualTrueAndEstado(String estado);

    List<TipoTramite> findByEstadoCatalogo(EstadoCatalogoTramite estadoCatalogo);

    List<TipoTramite> findByPermitePresentacionVirtualTrueAndEstadoCatalogo(EstadoCatalogoTramite estadoCatalogo);

    @Query("SELECT t FROM TipoTramite t WHERE " +
           "(:estadoCatalogo IS NULL OR t.estadoCatalogo = :estadoCatalogo) AND " +
           "(:q IS NULL OR LOWER(t.codigo) LIKE :q OR LOWER(t.nombre) LIKE :q OR (t.codigoTupa IS NOT NULL AND LOWER(t.codigoTupa) LIKE :q))")
    Page<TipoTramite> buscarParaAdmin(
            @Param("estadoCatalogo") EstadoCatalogoTramite estadoCatalogo,
            @Param("q") String q,
            Pageable pageable);
}
