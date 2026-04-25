package in.sipora.backend.shared.util;

import in.sipora.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

/**
 * Shared Kernel — static helpers for reading the authenticated user from
 * the Spring Security context.
 *
 * All modules use this instead of directly touching SecurityContextHolder,
 * keeping the security plumbing isolated to one place.
 *
 * The authenticated principal is expected to be an instance of
 * UserDetails where getUsername() returns the user's UUID as a String.
 * This is set up in the identity module's JwtAuthFilter.
 *
 * Usage in a service:
 *   UUID userId = SecurityUtils.requireCurrentUserId();
 *
 *   Optional<UUID> maybeId = SecurityUtils.getCurrentUserId(); // for endpoints accessible anonymously
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the UUID of the currently authenticated user.
     * Throws if the user is not authenticated (should never happen on secured endpoints).
     *
     * @throws DomainException HTTP 401 if the security context has no authenticated principal
     */
    public static UUID requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new DomainException(
                        "Authentication required", HttpStatus.UNAUTHORIZED));
    }

    /**
     * Returns the UUID of the authenticated user, or empty if anonymous.
     * This si used on endpoints that are accessible to both authenticated and anonymous users.
     */
    public static Optional<UUID> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            try {
                return Optional.of(UUID.fromString(userDetails.getUsername()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        if (principal instanceof String str && !"anonymousUser".equals(str)) {
            try {
                return Optional.of(UUID.fromString(str));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the raw Authentication object. Useful when needed to check roles.
     */
    public static Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? Optional.of(auth) : Optional.empty();
    }

    /**
     * Returns true if the current user has the given role (without the ROLE_ prefix).
     * Example: SecurityUtils.hasRole("ADMIN")
     */
    public static boolean hasRole(String role) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }

    /**
     * Returns true if there is an authenticated (non-anonymous) user in the context.
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId().isPresent();
    }
}