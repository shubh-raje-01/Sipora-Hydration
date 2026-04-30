package in.sipora.backend.modules.catalog.web;

import in.sipora.backend.modules.catalog.application.ProductService;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.AdjustStockRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CreateProductRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CreateVariantRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.ProductCardResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.ProductResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.UpdateProductRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.UpdateVariantRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.VariantResponse;
import in.sipora.backend.shared.web.ApiResponse;
import in.sipora.backend.shared.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Storefront product catalog and admin management")
public class ProductController {

    private final ProductService productService;

    // ── Storefront (public)

    @GetMapping
    @Operation(summary = "List products with filters and pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductCardResponse>>> listProducts(
            @Parameter(description = "Filter by category UUID")
            @RequestParam(required = false) UUID categoryId,

            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,

            @Parameter(description = "Keyword search on name and short description")
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(50, Math.max(1, size)),
                sort
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Products retrieved",
                productService.listProducts(categoryId, minPrice, maxPrice, inStock, search, pageable)));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products for the homepage")
    public ResponseEntity<ApiResponse<List<ProductCardResponse>>> getFeaturedProducts() {
        return ResponseEntity.ok(
                ApiResponse.success("Featured products retrieved", productService.getFeaturedProducts()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full product detail by URL slug")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved", productService.getBySlug(slug)));
    }

    // ── Admin

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Create a product with variants")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.createProduct(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Update product fields")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Archive a product (soft delete)")
    public ResponseEntity<ApiResponse<Void>> archiveProduct(@PathVariable UUID id) {
        productService.archiveProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product archived"));
    }

    // ── Variant management (admin)

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Add a variant to an existing product")
    public ResponseEntity<ApiResponse<VariantResponse>> addVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateVariantRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Variant added", productService.addVariant(productId, request)));
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Update a product variant")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Variant updated", productService.updateVariant(variantId, request)));
    }

    @PostMapping("/variants/{variantId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Manually add or deduct stock for a variant")
    public ResponseEntity<ApiResponse<VariantResponse>> adjustStock(
            @PathVariable UUID variantId,
            @Valid @RequestBody AdjustStockRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Stock adjusted", productService.adjustStock(variantId, request)));
    }
}