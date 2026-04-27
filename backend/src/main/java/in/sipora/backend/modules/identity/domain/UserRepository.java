package in.sipora.backend.modules.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for the identity module.
 * Only accessed by classes within this module — never directly by others.
 * Other modules call IdentityModuleApi instead.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.id = :id")
    void disableUser(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :hash WHERE u.id = :id")
    void updatePassword(@Param("id") UUID id, @Param("hash") String hash);
}