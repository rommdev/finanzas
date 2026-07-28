package pe.finanzas.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Setter
@Table(name = "tbl_categoria")
@Entity
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcategoria;

    private String descripcion;
    private String tipo;
    private Boolean activo;

    public String getActivoDescripcion(){
        return activo ? "Activo" : "Inactivo";
    }

}
