package in.sipora.backend.modules.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable shipping / billing address.
 *
 * Stored in the users table as flat columns (address_line1, address_city…).
 * Users can have multiple addresses stored as a @ElementCollection.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

    @NotBlank
    @Size(max = 255)
    @Column(name = "address_line1", nullable = false)
    private String line1;

    @Size(max = 255)
    @Column(name = "address_line2")
    private String line2;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address_city", nullable = false)
    private String city;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address_state", nullable = false)
    private String state;

    @NotBlank
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "must be a valid 6-digit Indian PIN code")
    @Column(name = "address_pin_code", nullable = false, length = 6)
    private String pinCode;

    @Builder.Default
    @Column(name = "address_country", nullable = false, length = 2)
    private String country = "IN";

    @Column(name = "address_label", length = 50)
    private String label;       // "Home", "Office", etc.
}