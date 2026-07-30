package pe.finanzas.finanzas.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.Usuario;
import pe.finanzas.finanzas.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<Usuario>getAll(){
        return usuarioRepository.findAll();
    }

    public Usuario getOne(Integer id){
        return usuarioRepository.findById(id).orElseThrow();
    }

    public ResultadoResponse create(Usuario usuario){
        try {
            usuario.setActivo(true);

            if (usuario.getClave() == null || usuario.getClave().isBlank()){
                return new ResultadoResponse(false, "La clave es obligatoria");
            }

            var registro = usuarioRepository.save(usuario);

            return new ResultadoResponse(true, "Usuario con ID " +registro.getIdusuario() + " registrado");
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }

    }

    public ResultadoResponse update(Usuario usuario) {
        try {
            var original = usuarioRepository.findById(usuario.getIdusuario()).orElseThrow();

            original.setNombres(usuario.getNombres());
            original.setApellidos(usuario.getApellidos());
            original.setCuenta(usuario.getCuenta());
            original.setTipo(usuario.getTipo());

            original.setActivo(original.getActivo());
            original.setClave(original.getClave());
            original.setFecha_nac(original.getFecha_nac());

            var registro  = usuarioRepository.save(original);

            var mensaje = String.format("Usuario con ID %s actualizado", registro.getIdusuario());

            return new ResultadoResponse(true,mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }

    @Transactional
    public ResultadoResponse changeActive(Integer id){
        try {
            var usuario = usuarioRepository.findById(id).orElseThrow();

            usuario.setActivo(!usuario.getActivo());

            var estado = usuario.getActivo() ? "Activo" : "desactivado";
            var mensaje = String.format("Usuario con ID %s %s", usuario.getIdusuario(), estado);

            return new ResultadoResponse(true,mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }


}
