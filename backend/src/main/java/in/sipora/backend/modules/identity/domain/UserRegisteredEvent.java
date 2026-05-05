package in.sipora.backend.modules.identity.domain;

import java.util.UUID;

/**
 * Published by AuthService immediately after a new user registers.
 * Notification module listens to this and sends the welcome email.
 *
 * Add this publish call to AuthService.register() after userRepository.save():
 *
 *   eventPublisher.publishEvent(new UserRegisteredEvent(
 *       user.getId(), user.getFullName(), user.getEmail()));
 *
 * AuthService must also inject ApplicationEventPublisher as a constructor param.
 */
public record UserRegisteredEvent(
        UUID userId,
        String fullName,
        String email
) {}