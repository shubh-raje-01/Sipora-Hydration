package in.sipora.backend.modules.identity.api;

import java.util.UUID;

/**
 * Lightweight user projection shared across module boundaries.
 *
 * This is a plain Java record — no JPA, no Jackson, no framework annotations.
 * Other modules receive this type from IdentityModuleApi and must not
 * attempt to cast it to in.sipora.modules.identity.domain.User.
 */
public record UserSummary(
        UUID   id,
        String fullName,
        String email,
        String phone,
        String role
) {}