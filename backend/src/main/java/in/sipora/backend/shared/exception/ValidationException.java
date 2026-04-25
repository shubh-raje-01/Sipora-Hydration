package in.sipora.backend.shared.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared Kernel — thrown when a business-level validation rule is violated.
 *
 * Distinct from @Valid / Bean Validation (which is handled separately in
 * GlobalExceptionHandler via MethodArgumentNotValidException).
 *
 * This is used for rule violations that can only be detected in the service layer,
 * e.g. "cannot cancel a shipped order" or "email already registered".
 *
 * Supports optional field-level errors for multi-field violations.
 *
 * Usage:
 *   throw new ValidationException("Email already registered");
 *
 *   throw ValidationException.builder()
 *       .message("Order validation failed")
 *       .fieldError("quantity", "must be greater than 0")
 *       .fieldError("variantId", "variant does not belong to this product")
 *       .build();
 */
public class ValidationException extends DomainException {

    private static final String ERROR_CODE = "validation_error";

    private final List<FieldError> fieldErrors;

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
        this.fieldErrors = Collections.emptyList();
    }

    private ValidationException(String message, List<FieldError> fieldErrors) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
        this.fieldErrors = Collections.unmodifiableList(fieldErrors);
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    // Nested value type — mirrors Spring's FieldError but dependency-free

    public record FieldError(String field, String message) {}

    // Builder for multi-field violations ==>

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String message;
        private final List<FieldError> errors = new ArrayList<>();

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder fieldError(String field, String errorMessage) {
            this.errors.add(new FieldError(field, errorMessage));
            return this;
        }

        public ValidationException build() {
            if (message == null || message.isBlank()) {
                throw new IllegalStateException("ValidationException requires a message");
            }
            return new ValidationException(message, errors);
        }
    }
}