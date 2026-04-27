package in.sipora.backend.modules.identity.application;

import in.sipora.backend.modules.identity.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Spring Security's UserDetailsService implementation.
 *
 * The "username" here is the user's UUID string — set as the JWT subject
 * in JwtTokenProvider. This way each authenticated request triggers a
 * single PK lookup (UUID) instead of a non-indexed email scan.
 *
 * Registered as a bean; SecurityConfig autowires it into the
 * DaoAuthenticationProvider.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid user identifier: " + userId);
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));
    }
}