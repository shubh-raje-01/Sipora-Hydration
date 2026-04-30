package in.sipora.backend.modules.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** All root categories (no parent), ordered for display. */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findAllRootCategories();

    /** All active categories with their children eagerly loaded. */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveWithChildren();
}