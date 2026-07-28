package pe.finanzas.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Setter
@Table(name = "tbl_presupuesto_categoria")
@Entity
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class PresupuestoCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idpresupuestocategoria;

    private Double montoasignado;
    private Boolean activo;

    public String getActivoDescripcion(){
        return Boolean.TRUE.equals(activo) ? "Activo" : "Inactivo";
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presupuesto")
    private Presupuesto presupuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
}
