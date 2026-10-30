package com.example.taller.service;

import com.example.taller.dto.AuthRequestDto;
import com.example.taller.dto.AuthResponseDto;
import com.example.taller.entity.Estado;
import com.example.taller.entity.Usuario;
import com.example.taller.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDto login(AuthRequestDto peticion) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(peticion.getEmail())
                .orElseThrow(this::noAutorizado);

        if (usuario.getEstado() != Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }
        if (!passwordEncoder.matches(peticion.getPassword(), usuario.getPasswordHash())) {
            throw noAutorizado();
        }

        return new AuthResponseDto(
                usuario.getId(),
                jwtService.generar(usuario),
                usuario.getEmail(),
                usuario.getRol().name());
    }

    // Mismo mensaje si el email no existe o la contrasena esta mal:
    // asi no le contamos a nadie que usuarios existen.
    private ResponseStatusException noAutorizado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos");
    }
}