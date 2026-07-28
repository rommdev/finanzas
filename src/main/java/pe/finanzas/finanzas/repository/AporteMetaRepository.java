package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.AporteMeta;

import java.util.List;

@Repository
public interface AporteMetaRepository extends JpaRepository<AporteMeta, Integer> {

    List<AporteMeta> findByMetaIdOrderByIdAporteDesc(Integer idmeta);

    boolean existsByMovimientoIdMovimiento(Integer idmovimiento);

    @Query("""
            select a 
            from AporteMeta a
            where a.idAporte =: idAporte
                   and a.meta.usuario.idUsuario =: idUsuario
                        """)
    AporteMeta findByIdAporteAndIdUsuario(
            @Param("idaporte") Integer idaporte,
            @Param("idusuario") Integer idusuario
    );
}
