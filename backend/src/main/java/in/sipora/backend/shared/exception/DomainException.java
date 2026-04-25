package in.sipora.backend.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Shared Kernel — root exception for all domain / business rule violations.
 *
 * Every module-specific business exception extend this class.
 * The GlobalExceptionHandler maps it to the appropriate HTTP status.
 *
 * Example usage in a module:
 *   public class InsufficientStockException extends DomainException {
 *       public InsufficientStockException(String sku) {
 *           super("Insufficient stock for SKU: " + sku, HttpStatus.CONFLICT);
 *       }
 *   }
 */
public class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public DomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = deriveCode(status);
    }

    public DomainException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public DomainException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = deriveCode(status);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Converts HTTP status to a snake_case error code string.
     * e.g. HttpStatus.NOT_FOUND -> "not_found"
     */
    private static String deriveCode(HttpStatus status) {
        return status.name().toLowerCase().replace(' ', '_');
    }
}