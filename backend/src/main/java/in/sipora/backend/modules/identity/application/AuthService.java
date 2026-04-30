package in.sipora.backend.modules.identity.application;

import in.sipora.backend.modules.identity.domain.User;
import in.sipora.backend.modules.identity.domain.UserRepository;
import in.sipora.backend.modules.identity.infrastructure.JwtTokenProvider;
import in.sipora.backend.modules.identity.web.IdentityDTOs.AuthResponse;
import in.sipora.backend.modules.identity.web.IdentityDTOs.LoginRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.RefreshTokenRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.RegisterRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.UserResponse;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/**
 * Handles all authentication flows:
 *  - register    → persist user, issue token pair
 *  - login       → authenticate, issue token pair
 *  - refresh     → validate opaque refresh token from Redis, issue new pair
 *  - logout      → evict refresh token from Redis (access token self-expires)
 *
 * Refresh tokens are opaque UUIDs stored in Redis as:
 *   Key:   "refresh:{refreshToken}"
 *   Value: userId string
 *   TTL:   refreshTokenExpiryMs
 *
 * This allows instant revocation — delete the key, token is dead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper            userMapper;

    @Value("${sipora.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${sipora.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    private static final String REFRESH_KEY_PREFIX = "refresh:";

    // Register ==>

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ValidationException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getId());

        return issueTokenPair(user);
    }

    // Login ==>

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager delegates to DaoAuthenticationProvider
        // which calls UserDetailsService.loadUserByUsername with the email.
        // We override loadUserByUsername to accept UUID, so we look up by
        // email first here to get the UUID, then authenticate.
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new DomainException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        // Spring Security validates password and enabled status
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getId().toString(),   // username = UUID
                        request.password()
                )
        );

        return issueTokenPair(user);
    }

    // Refresh ==>

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String redisKey = REFRESH_KEY_PREFIX + request.refreshToken();
        Object storedUserId = redisTemplate.opsForValue().get(redisKey);

        if (storedUserId == null) {
            throw new DomainException(
                    "Refresh token is invalid or has expired. Please log in again.",
                    HttpStatus.UNAUTHORIZED);
        }

        UUID userId = UUID.fromString(storedUserId.toString());
        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new DomainException("User account not found or disabled", HttpStatus.UNAUTHORIZED));

        // Rotate: revoke old refresh token, issue new pair
        redisTemplate.delete(redisKey);
        return issueTokenPair(user);
    }

    // Logout ==>

    public void logout(String refreshToken) {
        redisTemplate.delete(REFRESH_KEY_PREFIX + refreshToken);
        // Access token expires on its own (15 min).
        // For immediate revocation, maintain an access token blocklist in Redis.
    }

    // Helpers ==>

    private AuthResponse issueTokenPair(User user) {
        String accessToken  = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Store opaque refresh token in Redis
        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + refreshToken,
                user.getId().toString(),
                Duration.ofMillis(refreshTokenExpiryMs)
        );

        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.of(accessToken, refreshToken, accessTokenExpiryMs, userResponse);
    }
}