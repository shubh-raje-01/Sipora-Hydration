package in.sipora.backend.modules.auth.service;

import in.sipora.backend.config.security.JwtUtil;
import in.sipora.backend.modules.auth.dto.AuthResponse;
import in.sipora.backend.modules.auth.dto.LoginRequest;
import in.sipora.backend.modules.auth.dto.RegisterRequest;
import in.sipora.backend.modules.auth.entity.Role;
import in.sipora.backend.modules.auth.entity.User;
import in.sipora.backend.modules.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow( () -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password!");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

}
