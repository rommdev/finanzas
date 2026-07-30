package pe.finanzas.finanzas.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.type.descriptor.jdbc.UuidAsBinaryJdbcType;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.PresupuestoFilter;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.Presupuesto;
import pe.finanzas.finanzas.model.Usuario;
import pe.finanzas.finanzas.repository.PresupuestoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;

    public List<Presupuesto> getAll(){
        return  presupuestoRepository.findAll();
    }

    public List<Presupuesto> getByUsuarioActual(Integer idusuario){
        return presupuestoRepository.findByUsuarioIdUsuarioOrderByIdPresupuestoDesc(idusuario);
    }

    public List<Presupuesto> search(PresupuestoFilter filter, Integer idusuario){
        return presupuestoRepository.findAllByFilters(idusuario, filter.getMes(), filter.getAnio());
    }

    public Presupuesto getOne(Integer idpresupuesto, Integer idusuario){
        return presupuestoRepository.findByIdPresupuestoAndIdUsuario(idpresupuesto,idusuario);
    }

    public ResultadoResponse create (Presupuesto presupuesto, Integer idusuario){
        try {
            var validacion = validar(presupuesto);
            if (!validacion.success()) {
                return validacion;
            }
            if (presupuestoRepository.existsByUsuarioIdUsuarioAndMesAndAnio(idusuario, presupuesto.getMes(), presupuesto.getAnio())){
                return new ResultadoResponse(false, "Ya existe un presupuesto registradopara ese mes y año");
            }

            Usuario usuario = new Usuario();
            usuario.setIdusuario(idusuario);

            presupuesto.setUsuario(usuario);

            var registro = presupuestoRepository.save(presupuesto);
            var mensaje = String.format("Presupuesto con ID %s registrado", registro.getIdpresupuesto());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }

    public ResultadoResponse update(Presupuesto presupuesto, Integer idusuario){
        try {
            var validacion = validar(presupuesto);
            if (!validacion.success()){
                return validacion;
            }

            var original = presupuestoRepository.findByIdPresupuestoAndIdUsuario(presupuesto.getIdpresupuesto(), idusuario);

            if (original == null){
                return new ResultadoResponse(false, "El presupuesto no existe o no pertenece al usuario actual");
            }

            if (presupuestoRepository.existsByUsuarioIdUsuarioAndMesAndAnioAndIdPresupuestoNot(idusuario, presupuesto.getMes(), presupuesto.getAnio(), presupuesto.getIdpresupuesto())){
                return new ResultadoResponse(false, "Ya existe otro presupuesto registrado para ese mes y año");
            }

            original.setMes(presupuesto.getMes());
            original.setAnio(presupuesto.getAnio());
            original.setMontototal(presupuesto.getMontototal());

            var registro = presupuestoRepository.save(original);
            var mensaje = String.format("Presupuesto con ID %s actualizado", registro.getIdpresupuesto());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }

    public Integer getAnioActual() {
        return LocalDate.now().getYear();
    }

    private ResultadoResponse validar(Presupuesto presupuesto) {
        if (presupuesto.getMes() == null || presupuesto.getMes().isBlank()) {
            return new ResultadoResponse(false, "Debe seleccionar un mes");
        }

        if (presupuesto.getAnio() == null) {
            return new ResultadoResponse(false, "Debe ingresar un año");
        }

        if (presupuesto.getAnio() < 1900 || presupuesto.getAnio() > 9999) {
            return new ResultadoResponse(false, "El año debe tener 4 dígitos");
        }

        if (presupuesto.getMontototal() == null || presupuesto.getMontototal() <= 0) {
            return new ResultadoResponse(false, "El monto total debe ser mayor a 0");
        }

        return new ResultadoResponse(true, "Validación correcta");
    }
}
