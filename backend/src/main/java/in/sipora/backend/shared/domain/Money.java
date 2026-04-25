package in.sipora.backend.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Shared Kernel — immutable Money value object.
 *
 * Used this everywhere a monetary amount appears.
 *
 * Examples:
 *   Money price  = Money.of("499.00", "INR");
 *   Money total  = price.multiply(3);
 *   Money tax    = total.multiply(new BigDecimal("0.18"));  // 18% GST
 *   Money grand  = total.add(tax);
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    // Factory methods ==>

    public static Money of(BigDecimal amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount must not be null");
        validateCurrency(currencyCode);
        return new Money(amount.setScale(SCALE, ROUNDING), currencyCode);
    }

    public static Money of(String amount, String currencyCode) {
        return of(new BigDecimal(amount), currencyCode);
    }

    public static Money inr(String amount) {
        return of(amount, "INR");
    }

    public static Money zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }

    // Arithmetic (returns new instance — immutable) ==>

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount).setScale(SCALE, ROUNDING), this.currencyCode);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount).setScale(SCALE, ROUNDING), this.currencyCode);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING), this.currencyCode);
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier).setScale(SCALE, ROUNDING), this.currencyCode);
    }

    // Comparison helpers ==>

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // Value object equality — two Money are equal if amount + currency match ==>

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return this.amount.compareTo(other.amount) == 0
                && Objects.equals(this.currencyCode, other.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currencyCode);
    }

    @Override
    public String toString() {
        return currencyCode + " " + amount.toPlainString();
    }

    // Guards ==>

    private void assertSameCurrency(Money other) {
        Objects.requireNonNull(other, "other Money must not be null");
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(
                    "Cannot operate on different currencies: %s and %s"
                            .formatted(this.currencyCode, other.currencyCode));
        }
    }

    private static void validateCurrency(String code) {
        Objects.requireNonNull(code, "currencyCode must not be null");
        try {
            Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ISO 4217 currency code: " + code);
        }
    }
}