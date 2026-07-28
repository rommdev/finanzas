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
@Table(name = "tbl_usuario")
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idusuario;

    private String nombres;
    private String apellidos;
    private String cuenta;
    private String clave;
    private LocalDate fecha_nac;
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo")
    private Tipo tipo;

    public String getFullName() {
        return String.format("%s %s", nombres, apellidos);
    }

    public String getActivoDescripcion() {
        return activo ? "Activo" : "Inactivo";
    }

}
