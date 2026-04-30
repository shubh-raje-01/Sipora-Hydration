package in.sipora.backend.modules.catalog.domain;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    /**
     * Acquires a pessimistic write lock (SELECT FOR UPDATE) on the variant row.
     * Used exclusively by deductStock and restoreStock to prevent overselling
     * under concurrent order placement.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id AND v.active = true")
    Optional<ProductVariant> findByIdWithLock(@Param("id") UUID id);

    Optional<ProductVariant> findBySkuAndActiveTrue(String sku);

    boolean existsBySku(String sku);
}