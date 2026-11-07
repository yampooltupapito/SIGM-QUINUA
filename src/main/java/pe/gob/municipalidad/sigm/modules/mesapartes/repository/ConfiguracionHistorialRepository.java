package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ConfiguracionHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;

import java.util.List;

@Repository
public interface ConfiguracionHistorialRepository extends JpaRepository<ConfiguracionHistorial, Long> {

    List<ConfiguracionHistorial> findByEntidadTipoAndEntidadIdOrderByFechaCambioDesc(
            TipoEntidadConfiguracion entidadTipo, Long entidadId);

    Page<ConfiguracionHistorial> findByEntidadTipoOrderByFechaCambioDesc(
            TipoEntidadConfiguracion entidadTipo, Pageable pageable);

    Page<ConfiguracionHistorial> findAllByOrderByFechaCambioDesc(Pageable pageable);
}
