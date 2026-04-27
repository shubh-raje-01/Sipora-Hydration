package in.sipora.backend.modules.identity.domain;

import in.sipora.backend.shared.domain.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Core identity entity. Implements UserDetails so Spring Security can
 * use it directly — no separate UserDetailsImpl wrapper needed.
 *
 * Design notes:
 *  - Username for Spring Security = UUID (set in JwtAuthFilter).
 *    This avoids email-lookup overhead on every request.
 *  - Addresses stored as @ElementCollection — simple for MVP,
 *    can be extracted to its own table/entity later.
 *  - passwordHash is never returned in serialization (no @JsonProperty).
 *  - enabled flag allows soft-disable without deletion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
public class User extends BaseEntity implements UserDetails {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone", length = 15)
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.CUSTOMER;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_addresses",
            joinColumns = @JoinColumn(name = "user_id")
    )
    private List<Address> addresses = new ArrayList<>();

    // ──────────────────────────────────────────────────────────────────
    // UserDetails — Spring Security contract
    // ──────────────────────────────────────────────────────────────────

    /**
     * Returns the UUID string as the username.
     * JwtAuthFilter stores getId().toString() in the token subject,
     * and sets this as the principal name so SecurityUtils.requireCurrentUserId()
     * can parse it back to UUID without an extra DB lookup.
     */
    @Override
    public String getUsername() {
        return getId() != null ? getId().toString() : null;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked()   { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled()            { return enabled; }
}