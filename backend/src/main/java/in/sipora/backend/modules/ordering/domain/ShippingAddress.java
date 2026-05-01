package in.sipora.backend.modules.ordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Shipping address snapshot embedded in the Order entity.
 *
 * Copied from the user-provided address at checkout time.
 * Immutable after order creation — if the user changes their address
 * later, this record is unaffected. This is the address the parcel was
 * (or will be) sent to.
 *
 * Not linked to identity.Address — cross-module boundary is preserved
 * by copying scalar values into this embeddable.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ShippingAddress {

    @Column(name = "ship_full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "ship_phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "ship_line1", nullable = false, length = 255)
    private String line1;

    @Column(name = "ship_line2", length = 255)
    private String line2;

    @Column(name = "ship_city", nullable = false, length = 100)
    private String city;

    @Column(name = "ship_state", nullable = false, length = 100)
    private String state;

    @Column(name = "ship_pin_code", nullable = false, length = 6)
    private String pinCode;

    @Column(name = "ship_country", nullable = false, length = 2)
    @Builder.Default
    private String country = "IN";
}