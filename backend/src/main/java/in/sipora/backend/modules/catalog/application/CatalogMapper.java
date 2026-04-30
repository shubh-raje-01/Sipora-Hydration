package in.sipora.backend.modules.catalog.application;

import in.sipora.backend.modules.catalog.api.ProductSummary;
import in.sipora.backend.modules.catalog.api.VariantSummary;
import in.sipora.backend.modules.catalog.domain.Category;
import in.sipora.backend.modules.catalog.domain.Product;
import in.sipora.backend.modules.catalog.domain.ProductVariant;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CategoryResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.ProductCardResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.ProductResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.VariantResponse;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts catalog domain entities to DTOs and public API summaries.
 * Manual mapper — product-to-DTO conversion has enough computed fields
 * (startingPrice, inStock, totalStock) that MapStruct expressions would be verbose.
 */
@Component
public class CatalogMapper {

    // ── Product

    public ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getDescription(),
                p.getShortDescription(),
                p.getMetaTitle(),
                p.getMetaDescription(),
                p.getStatus().name(),
                p.isFeatured(),
                p.getDisplayOrder(),
                p.getStartingPrice(),
                "INR",
                p.isInStock(),
                p.getTotalStock(),
                p.getImages(),
                toCategoryResponse(p.getCategory()),
                p.getVariants().stream()
                        .filter(ProductVariant::isActive)
                        .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                        .map(this::toVariantResponse)
                        .toList(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    public ProductCardResponse toCardResponse(Product p) {
        return new ProductCardResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getShortDescription(),
                p.getStartingPrice(),
                "INR",
                p.isInStock(),
                p.getPrimaryImageUrl(),
                p.isFeatured(),
                p.getCategory() != null ? p.getCategory().getName() : null
        );
    }

    public ProductSummary toSummary(Product p) {
        List<String> categoryNames = p.getCategory() != null
                ? List.of(p.getCategory().getName())
                : List.of();
        return new ProductSummary(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getShortDescription(),
                p.getStartingPrice(),
                "INR",
                p.isInStock(),
                p.getTotalStock(),
                p.getPrimaryImageUrl(),
                categoryNames
        );
    }

    // ── Variant

    public VariantResponse toVariantResponse(ProductVariant v) {
        return new VariantResponse(
                v.getId(),
                v.getSku(),
                v.getDisplayName(),
                v.getPrice().getAmount(),
                v.getPrice().getCurrencyCode(),
                v.getStockQty(),
                v.getLowStockThreshold(),
                v.isInStock(),
                v.isLowStock(),
                v.getColor(),
                v.getSize(),
                v.getFlavor(),
                v.getQuantityCount(),
                v.isActive(),
                v.getDisplayOrder()
        );
    }

    public VariantSummary toVariantSummary(ProductVariant v) {
        return new VariantSummary(
                v.getId(),
                v.getProduct().getId(),
                v.getSku(),
                v.getDisplayName(),
                v.getPrice().getAmount(),
                v.getPrice().getCurrencyCode(),
                v.getStockQty(),
                v.isInStock()
        );
    }

    // ── Category

    public CategoryResponse toCategoryResponse(Category c) {
        if (c == null) return null;
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getDescription(),
                c.getImageUrl(),
                c.getDisplayOrder(),
                c.isActive(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getChildren() == null ? List.of() :
                        c.getChildren().stream()
                                .filter(Category::isActive)
                                .map(this::toCategoryResponse)
                                .toList()
        );
    }
}