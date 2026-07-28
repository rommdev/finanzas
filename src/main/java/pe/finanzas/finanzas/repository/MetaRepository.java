package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Integer> {

    List<Meta> findByUsuarioIdUsuarioOrderByIdMetaDesc(Integer idusuario);

    @Query("""
                select m
                from Meta m
                where m.idMeta =: idMeta
                    and m.usuario.idusuario =: idusario
                """)
    Meta findByIdMetaAndIdUsuario(
            @Param("idmeta") Integer idmeta,
            @Param("idusuario") Integer idusuario
    );

    @Query("""
            select m
            from Meta m
            where
                   m.usuario.idusuario =: idusuario
                   and
                    (
                      :estado is null
                       or :estado = ''
                       or (:estado = 'completado' and m.completada = true)
                       or (:estado = 'incompleta' and m.completada = false)
                                )
            order by m.idMeta desc
                        """)
    List<Meta> findAllByFilter(
            @Param("idusuario") Integer idusuario,
            @Param("estado") String estado
    );

}
