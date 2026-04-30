package in.sipora.backend.modules.catalog.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Product> findByIdIn(List<UUID> ids);

    List<Product> findByStatusOrderByDisplayOrderAsc(ProductStatus status);

    @Query("SELECT p FROM Product p JOIN FETCH p.variants v JOIN FETCH p.category " +
            "WHERE p.id = :id AND p.status = 'ACTIVE'")
    Optional<Product> findActiveById(@Param("id") UUID id);

    @Query("SELECT p FROM Product p JOIN FETCH p.variants v JOIN FETCH p.category " +
            "WHERE p.slug = :slug AND p.status = 'ACTIVE'")
    Optional<Product> findActiveBySlug(@Param("slug") String slug);

    @Query("SELECT p FROM Product p JOIN FETCH p.variants WHERE p.status = 'ACTIVE' " +
            "ORDER BY p.featured DESC, p.displayOrder ASC")
    List<Product> findAllActiveWithVariants();

    @Query("SELECT p FROM Product p WHERE p.featured = true AND p.status = 'ACTIVE' " +
            "ORDER BY p.displayOrder ASC")
    List<Product> findFeaturedProducts();
}