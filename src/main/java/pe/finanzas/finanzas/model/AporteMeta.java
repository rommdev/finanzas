package pe.finanzas.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

@Getter
@Setter
@Table(name = "tbl_aporte_meta")
@Entity
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class AporteMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idaporte;

    private LocalDate fecha;
    private Double montoaporte;
    private String observacion;
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_meta")
    private Meta meta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movieminto")
    private Movimiento movimiento;

    public String getActivoDescripcion(){
        return Boolean.TRUE.equals(activo) ? "Activo" : "Inactivo";
    }

}
