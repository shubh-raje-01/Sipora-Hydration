package in.sipora.backend.shared.exception;

import in.sipora.backend.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Shared Kernel — thrown when any entity lookup returns empty.
 *
 * Maps to HTTP 404 in GlobalExceptionHandler.
 *
 * Usage:
 *   throw ResourceNotFoundException.of("Product", id);
 *   throw ResourceNotFoundException.ofSlug("Product", "sipora-glass-bottle");
 */
public class ResourceNotFoundException extends DomainException {

    private static final String ERROR_CODE = "resource_not_found";

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    // Static factory helpers — preferred over direct constructor calls ==>

    public static ResourceNotFoundException of(String resourceType, UUID id) {
        return new ResourceNotFoundException(
                "%s not found with id: %s".formatted(resourceType, id));
    }

    public static ResourceNotFoundException of(String resourceType, String identifier) {
        return new ResourceNotFoundException(
                "%s not found: %s".formatted(resourceType, identifier));
    }

    public static ResourceNotFoundException ofSlug(String resourceType, String slug) {
        return new ResourceNotFoundException(
                "%s not found with slug: %s".formatted(resourceType, slug));
    }

    public static ResourceNotFoundException ofField(String resourceType, String field, Object value) {
        return new ResourceNotFoundException(
                "%s not found where %s = %s".formatted(resourceType, field, value));
    }
}