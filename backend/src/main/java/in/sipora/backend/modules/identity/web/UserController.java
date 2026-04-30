package in.sipora.backend.modules.identity.web;

import in.sipora.backend.modules.identity.application.UserService;
import in.sipora.backend.modules.identity.web.IdentityDTOs.AddressRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.AddressResponse;
import in.sipora.backend.modules.identity.web.IdentityDTOs.ChangePasswordRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.UpdateProfileRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.UserResponse;
import in.sipora.backend.shared.util.SecurityUtils;
import in.sipora.backend.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User profile and address management.
 * All endpoints require authentication — enforced by SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Profile management and saved addresses")
public class UserController {

    private final UserService userService;

    // ── Profile

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved", userService.getProfile(userId)));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update name or phone number")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated", userService.updateProfile(userId, request)));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change the authenticated user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(SecurityUtils.requireCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // ── Addresses

    @GetMapping("/me/addresses")
    @Operation(summary = "List all saved addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses() {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Addresses retrieved", userService.getAddresses(userId)));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new saved address (max 5)")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> addAddress(
            @Valid @RequestBody AddressRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Address added", userService.addAddress(userId, request)));
    }

    @PutMapping("/me/addresses/{index}")
    @Operation(summary = "Replace an existing address by its list index")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> updateAddress(
            @PathVariable int index,
            @Valid @RequestBody AddressRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Address updated", userService.updateAddress(userId, index, request)));
    }

    @DeleteMapping("/me/addresses/{index}")
    @Operation(summary = "Delete a saved address by its list index")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable int index) {
        userService.deleteAddress(SecurityUtils.requireCurrentUserId(), index);
        return ResponseEntity.ok(ApiResponse.success("Address deleted"));
    }
}