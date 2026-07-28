package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.Movimiento;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository <Movimiento, Integer> {

    List<Movimiento> findAllByOrderByIdMovimientoDesc();

    List<Movimiento> findByCategoriaTipoIgnoreCaseOrderByIdMovimientoDesc(String tipo);

    @Query("""
        select m
        from movimiento m
        where m.usuario.idUsuario =: idUsuario
        ORDER BY m.idMovimiento DESC
        """)

    List<Movimiento> findByUsuario(@Param("idusuario") Integer idusuario);
}
