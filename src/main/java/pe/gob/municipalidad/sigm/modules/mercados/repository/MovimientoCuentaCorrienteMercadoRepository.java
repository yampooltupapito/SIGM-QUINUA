package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MovimientoCuentaCorrienteMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovimientoCuentaCorrienteMercadoRepository extends JpaRepository<MovimientoCuentaCorrienteMercado, Long> {

    List<MovimientoCuentaCorrienteMercado> findByComercianteIdOrderByFechaMovimientoAsc(UUID comercianteId);

    @Query(value = "SELECT m.* FROM mercados_cuenta_corriente_movimientos m " +
                   "WHERE m.comerciante_id = CAST(:comercianteId AS uuid) " +
                   "ORDER BY m.fecha_movimiento DESC, m.id DESC",
           countQuery = "SELECT COUNT(*) FROM mercados_cuenta_corriente_movimientos m " +
                        "WHERE m.comerciante_id = CAST(:comercianteId AS uuid)",
           nativeQuery = true)
    Page<MovimientoCuentaCorrienteMercado> buscarPaginadoPorComerciante(@Param("comercianteId") UUID comercianteId,
                                                                       Pageable pageable);

    @Query(value = "SELECT m.* FROM mercados_cuenta_corriente_movimientos m " +
                   "WHERE m.puesto_id = CAST(:puestoId AS uuid) " +
                   "ORDER BY m.fecha_movimiento DESC, m.id DESC",
           countQuery = "SELECT COUNT(*) FROM mercados_cuenta_corriente_movimientos m " +
                        "WHERE m.puesto_id = CAST(:puestoId AS uuid)",
           nativeQuery = true)
    Page<MovimientoCuentaCorrienteMercado> buscarPaginadoPorPuesto(@Param("puestoId") UUID puestoId,
                                                                 Pageable pageable);
}
