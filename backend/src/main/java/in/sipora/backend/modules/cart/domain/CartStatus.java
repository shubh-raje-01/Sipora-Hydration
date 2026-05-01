package in.sipora.backend.modules.cart.domain;

/**
 * Lifecycle state of a Cart.
 *
 * ACTIVE       — current working cart, can be modified
 * CHECKED_OUT  — converted to an order; read-only history
 * ABANDONED    — not touched for 30+ days; scheduled job marks these
 */
public enum CartStatus {
    ACTIVE,
    CHECKED_OUT,
    ABANDONED
}