package com.positivo.podcast.services;

import com.positivo.podcast.dtos.request.LoginRequestDto;
import com.positivo.podcast.dtos.request.RegisterRequestDto;
import com.positivo.podcast.dtos.response.AuthResponseDto;
import com.positivo.podcast.dtos.response.UsuarioResponseDto;
import com.positivo.podcast.entities.Usuario;
import com.positivo.podcast.exceptions.EmailAlreadyExistsException;
import com.positivo.podcast.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        // Cria o objeto de autenticação com as credenciais
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequestDto.email(),
                loginRequestDto.senha()
        );

        // O AuthenticationManager processa a autenticação
        Authentication authentication = authenticationManager.authenticate(authToken);

        // Extrai o usuário autenticado do objeto Authentication
        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Gera o token JWT para o usuário
        String token = tokenService.generateToken(usuario);

        // Cria o DTO com os dados do usuário para a resposta
        UsuarioResponseDto usuarioDto = new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole());

        // Retorna a resposta completa
        return new AuthResponseDto(token, usuarioDto);
    }

    public void register(RegisterRequestDto registerRequestDto) {
        if (usuarioRepository.findByEmail(registerRequestDto.email()).isPresent()) {
            throw new EmailAlreadyExistsException("O email '" + registerRequestDto.email() + "' já está em uso.");
        }

        Usuario novoUsuario = new Usuario(
                registerRequestDto.nome(),
                registerRequestDto.email(),
                passwordEncoder.encode(registerRequestDto.senha()),
                "USER" // Role padrão
        );

        usuarioRepository.save(novoUsuario);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + username));
    }
}
