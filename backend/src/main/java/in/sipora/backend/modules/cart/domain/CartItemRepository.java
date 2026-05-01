package in.sipora.backend.modules.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndVariantId(UUID cartId, UUID variantId);

    Optional<CartItem> findByIdAndCartUserId(UUID itemId, UUID userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") UUID cartId);
}