package pe.finanzas.finanzas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.AutenticacionFilter;
import pe.finanzas.finanzas.model.Usuario;
import pe.finanzas.finanzas.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;

    public Usuario autenticathe(AutenticacionFilter filter){
        return usuarioRepository.findByCuentaAndClave(filter.getCuenta(), filter.getClave());
    }
}
