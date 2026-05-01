package in.sipora.backend.modules.identity.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * All request and response records for the identity module web layer.
 * Grouping them in one file keeps the web/ package tidy for a module
 * this size. Split into separate files if they grow significantly.
 */
public final class IdentityDTOs {

    private IdentityDTOs() {}

    // Auth requests =>

    public record RegisterRequest(
            @NotBlank(message = "Full name is required")
            @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
            String fullName,

            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email address")
            @Size(max = 255)
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                    message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
            )
            String password,

            @Pattern(regexp = "^[6-9]\\d{9}$", message = "Must be a valid 10-digit Indian mobile number")
            String phone
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required")
            String currentPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 8, max = 100)
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                    message = "New password must contain uppercase, lowercase and a number"
            )
            String newPassword
    ) {}

    // Auth responses ==>

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,     // seconds
            UserResponse user
    ) {
        public static AuthResponse of(String access, String refresh, long expiryMs, UserResponse user) {
            return new AuthResponse(access, refresh, "Bearer", expiryMs / 1000, user);
        }
    }

    // User requests ==>

    public record UpdateProfileRequest(
            @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
            String fullName,

            @Pattern(regexp = "^[6-9]\\d{9}$", message = "Must be a valid 10-digit Indian mobile number")
            String phone
    ) {}

    public record AddressRequest(
            @NotBlank(message = "Address line 1 is required")
            @Size(max = 255)
            String line1,

            @Size(max = 255)
            String line2,

            @NotBlank(message = "City is required")
            @Size(max = 100)
            String city,

            @NotBlank(message = "State is required")
            @Size(max = 100)
            String state,

            @NotBlank(message = "PIN code is required")
            @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Must be a valid 6-digit Indian PIN code")
            String pinCode,

            @Size(max = 50)
            String label
    ) {}

    // User responses ==>

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserResponse(
            UUID id,
            String fullName,
            String email,
            String phone,
            String role,
            boolean enabled,
            Instant createdAt,
            List<AddressResponse> addresses
    ) {}

    public record AddressResponse(
            int index,
            String line1,
            String line2,
            String city,
            String state,
            String pinCode,
            String country,
            String label
    ) {}
}