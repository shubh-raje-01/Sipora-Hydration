package in.sipora.backend.modules.catalog.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * All request and response records for the catalog module web layer.
 */
public final class CatalogDTOs {

    private CatalogDTOs() {}

    // Category requests ==>

    public record CreateCategoryRequest(
            @NotBlank @Size(max = 100)
            String name,

            @Size(max = 120)
            String slug,          // auto-generated from name if blank

            @Size(max = 1000)
            String description,

            String imageUrl,

            UUID parentId,

            int displayOrder
    ) {}

    // Product requests ==>

    public record CreateProductRequest(
            @NotBlank @Size(max = 255)
            String name,

            @Size(max = 280)
            String slug,          // auto-generated from name if blank

            @Size(max = 5000)
            String description,

            @Size(max = 500)
            String shortDescription,

            @Size(max = 255)
            String metaTitle,

            @Size(max = 500)
            String metaDescription,

            @NotNull
            UUID categoryId,

            boolean featured,

            int displayOrder,

            List<String> imageUrls,

            @NotEmpty
            @Valid
            List<CreateVariantRequest> variants
    ) {}

    public record UpdateProductRequest(
            @Size(max = 255)
            String name,

            @Size(max = 5000)
            String description,

            @Size(max = 500)
            String shortDescription,

            @Size(max = 255)
            String metaTitle,

            @Size(max = 500)
            String metaDescription,

            UUID categoryId,

            Boolean featured,

            String status,

            int displayOrder,

            List<String> imageUrls
    ) {}

    public record CreateVariantRequest(
            @NotBlank @Size(max = 100)
            String sku,

            @NotBlank @Size(max = 200)
            String displayName,

            @NotNull
            @DecimalMin(value = "0.01", message = "Price must be greater than 0")
            BigDecimal price,

            @Min(0)
            int stockQty,

            @Min(1)
            int lowStockThreshold,

            String color,
            String size,
            String flavor,
            Integer quantityCount,
            int displayOrder
    ) {}

    public record UpdateVariantRequest(
            String displayName,
            BigDecimal price,
            Integer stockQty,
            Integer lowStockThreshold,
            String color,
            String size,
            String flavor,
            Integer quantityCount,
            Boolean active,
            Integer displayOrder
    ) {}

    public record AdjustStockRequest(
            @NotNull
            @Min(value = 1, message = "Quantity must be at least 1")
            Integer qty,

            @NotBlank
            @Pattern(regexp = "ADD|DEDUCT", message = "Operation must be ADD or DEDUCT")
            String operation,

            String reason
    ) {}

    // Responses ==>

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoryResponse(
            UUID   id,
            String name,
            String slug,
            String description,
            String imageUrl,
            int    displayOrder,
            boolean active,
            UUID   parentId,
            List<CategoryResponse> children
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProductResponse(
            UUID              id,
            String            name,
            String            slug,
            String            description,
            String            shortDescription,
            String            metaTitle,
            String            metaDescription,
            String            status,
            boolean           featured,
            int               displayOrder,
            BigDecimal        startingPrice,
            String            currencyCode,
            boolean           inStock,
            int               totalStock,
            List<String>      images,
            CategoryResponse  category,
            List<VariantResponse> variants,
            Instant           createdAt,
            Instant           updatedAt
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VariantResponse(
            UUID       id,
            String     sku,
            String     displayName,
            BigDecimal price,
            String     currencyCode,
            int        stockQty,
            int        lowStockThreshold,
            boolean    inStock,
            boolean    lowStock,
            String     color,
            String     size,
            String     flavor,
            Integer    quantityCount,
            boolean    active,
            int        displayOrder
    ) {}

    /** Minimal card used in listing pages — no full description or variants. */
    public record ProductCardResponse(
            UUID       id,
            String     name,
            String     slug,
            String     shortDescription,
            BigDecimal startingPrice,
            String     currencyCode,
            boolean    inStock,
            String     primaryImageUrl,
            boolean    featured,
            String     categoryName
    ) {}
}