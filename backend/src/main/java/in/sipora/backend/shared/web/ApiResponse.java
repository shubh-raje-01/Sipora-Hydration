package in.sipora.backend.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Shared Kernel — uniform envelope for every REST response.
 *
 * All controllers return ApiResponse<T>. This guarantees the frontend always
 * receives the same shape regardless of which module or endpoint was called.
 *
 * Success shape:
 * {
 *   "success": true,
 *   "message": "Product retrieved successfully",
 *   "data": { ...product fields... },
 *   "timestamp": "2025-04-23T10:00:00Z"
 * }
 *
 * Error shape (data is null, omitted by @JsonInclude):
 * {
 *   "success": false,
 *   "message": "Product not found with id: ...",
 *   "errorCode": "resource_not_found",
 *   "timestamp": "2025-04-23T10:00:00Z"
 * }
 *
 * Usage in a controller:
 *   return ResponseEntity.ok(ApiResponse.success("Product retrieved", product));
 *   return ResponseEntity.status(201).body(ApiResponse.success("Created", created));
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String errorCode,
        Instant timestamp
) {

    // Success factories ==>

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, Instant.now());
    }

    // Error factories — used by GlobalExceptionHandler  ==>

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, "internal_error", Instant.now());
    }
}
