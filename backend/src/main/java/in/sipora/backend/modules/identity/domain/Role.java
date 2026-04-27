package in.sipora.backend.modules.identity.domain;

/**
 * Roles assigned to a User.
 * Spring Security expects authorities prefixed with ROLE_ —
 * that prefix is added in User.getAuthorities().
 */
public enum Role {
    CUSTOMER,
    ADMIN
}