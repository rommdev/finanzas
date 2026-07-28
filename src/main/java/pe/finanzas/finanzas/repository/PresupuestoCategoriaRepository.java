package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.PresupuestoCategoria;

import java.util.List;

@Repository
public interface PresupuestoCategoriaRepository extends JpaRepository<PresupuestoCategoria, Integer> {

    List<PresupuestoCategoria> findByPresupuestoIdPresupuestoOrderByIdPresupuestoCategoriaDesc(Integer idpresupuesto);

    @Query("""
          select pc
          from PresupuestoCategoria pc
          where pc.idPresupuestoCategoria =: idPresupuestoCategoria
            and pc.presupuesto.usuario.idUsuario =: idUsuario  
            """)

    PresupuestoCategoria findByIdPresupuestoCategoriaAndIdUsuario(
            @Param("idpresupuestocategoria") Integer idpresupuestocategoria,
            @Param("idusuario") Integer idusuario
    );

    boolean existsByPresupuestoIdPresupuestoAndCategoriaIdCategoria(Integer idpresupuesto, Integer idcategoria);

    boolean existsByPresupuestoIdPresupuestoAndCategoriaIdCategoriaAndIdPresupuestoCategoriaNot(
            Integer idpresupuesto,
            Integer idcategoria,
            Integer idpresupuestocategoria
    );
}
