package in.sipora.backend.modules.identity.application;

import in.sipora.backend.modules.identity.api.IdentityModuleApi;
import in.sipora.backend.modules.identity.api.UserSummary;
import in.sipora.backend.modules.identity.domain.Role;
import in.sipora.backend.modules.identity.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements the public module contract.
 *
 * Other modules inject IdentityModuleApi (the interface) — never this class.
 * Spring wires this implementation automatically.
 *
 * All methods are read-only transactions since this API is purely for querying
 * user data on behalf of other modules.
 */
@Service
@RequiredArgsConstructor
public class IdentityModuleApiImpl implements IdentityModuleApi {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSummary> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .filter(u -> u.isEnabled())
                .map(userMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.isEnabled())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }
}