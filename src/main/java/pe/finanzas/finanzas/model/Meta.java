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
@Table(name = "tbl_meta")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idmeta;

    private String nombre;
    private String descripcion;
    private Double montoobjetivo;
    private Double montoactual;
    private LocalDate fechaobjetivo;
    private Boolean completada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario")
    private Usuario usuario;

    private String getCompletadaDescripcion(){
        return Boolean.TRUE.equals(completada) ? "Completada" : "Incompleta";
    }

    public Boolean getFechaLimitePasada(){
        if (fechaobjetivo == null){
            return false;
        }
        return LocalDate.now().isAfter(fechaobjetivo);
    }

    public String getEstadoDescripcion(){
        return Boolean.TRUE.equals(completada) ? "Completada" : "Incompleta";
    }

    public String getBadgeEstado(){
        return Boolean.TRUE.equals(completada) ? "bg-success" : "bg-secondary";
    }

    public Double getMontoRestante(){
        Double objetivo = montoobjetivo == null ? 0.0 : montoobjetivo;
        Double actual = montoactual == null ? 0.0 : montoactual;
        Double restante = objetivo-actual;

        return restante < 0 ? 0.0: restante;
    }

    public Double getPorcentajeAvance(){
        if (montoobjetivo == null || montoobjetivo <= 0){
            return 0.0;
        }

        Double actual = montoactual == null ? 0.0 : montoactual;
        Double porcentaje = (actual * 100 ) / montoobjetivo;

        return porcentaje > 100 ? 100.0 : porcentaje;
    }

}
