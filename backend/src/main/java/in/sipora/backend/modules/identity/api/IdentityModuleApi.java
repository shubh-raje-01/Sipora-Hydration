package in.sipora.backend.modules.identity.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the identity module.
 *
 * This is the ONLY interface other modules are allowed to import.
 * No other class from in.sipora.modules.identity.* may be imported outside
 * this module. Enforced by ArchUnit in ModuleBoundaryTest.
 *
 * Methods are deliberately minimal — return only what callers actually need.
 */
public interface IdentityModuleApi {

    /**
     * Returns a lightweight user summary by ID.
     * Returns empty if the user does not exist or is disabled.
     */
    Optional<UserSummary> getUserById(UUID userId);

    /**
     * Returns true if the given userId belongs to a real, enabled user.
     * Useful for foreign-key-style checks without fetching the full summary.
     */
    boolean userExists(UUID userId);

    /**
     * Returns true if the given user has the ADMIN role.
     */
    boolean isAdmin(UUID userId);
}