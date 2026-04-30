package in.sipora.backend.modules.catalog.application;

import in.sipora.backend.modules.catalog.domain.Category;
import in.sipora.backend.modules.catalog.domain.Product;
import in.sipora.backend.modules.catalog.domain.ProductRepository;
import in.sipora.backend.modules.catalog.domain.ProductSpecification;
import in.sipora.backend.modules.catalog.domain.ProductStatus;
import in.sipora.backend.modules.catalog.domain.ProductVariant;
import in.sipora.backend.modules.catalog.domain.ProductVariantRepository;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.AdjustStockRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CreateProductRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CreateVariantRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.ProductCardResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.ProductResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.UpdateProductRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.UpdateVariantRequest;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.VariantResponse;
import in.sipora.backend.shared.domain.Money;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;
import in.sipora.backend.shared.exception.ValidationException;
import in.sipora.backend.shared.util.SlugUtils;
import in.sipora.backend.shared.web.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static in.sipora.backend.config.RedisConfig.CacheNames.PRODUCTS;
import static in.sipora.backend.config.RedisConfig.CacheNames.PRODUCT_SLUGS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryService categoryService;
    private final CatalogMapper catalogMapper;

    // ── Public listing ==>

    @Transactional(readOnly = true)
    public PageResponse<ProductCardResponse> listProducts(
            UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, String search, Pageable pageable) {

        Specification<Product> spec = ProductSpecification.active()
                .and(ProductSpecification.inCategory(categoryId))
                .and(ProductSpecification.minPrice(minPrice))
                .and(ProductSpecification.maxPrice(maxPrice))
                .and(ProductSpecification.nameContains(search));

        if (Boolean.TRUE.equals(inStock)) {
            spec = spec.and(ProductSpecification.inStock());
        }

        Page<ProductCardResponse> page = productRepository
                .findAll(spec, pageable)
                .map(catalogMapper::toCardResponse);

        return PageResponse.from(page);
    }

    @Cacheable(value = PRODUCT_SLUGS, key = "#slug")
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        return productRepository.findActiveBySlug(slug)
                .map(catalogMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.ofSlug("Product", slug));
    }

    @Cacheable(value = PRODUCTS, key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return productRepository.findActiveById(id)
                .map(catalogMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    @Transactional(readOnly = true)
    public List<ProductCardResponse> getFeaturedProducts() {
        return productRepository.findFeaturedProducts()
                .stream()
                .map(catalogMapper::toCardResponse)
                .toList();
    }

    // ── Admin CRUD ==>

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String slug = resolveProductSlug(request.slug(), request.name());
        Category category = categoryService.findEntityById(request.categoryId());

        Product product = Product.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description())
                .shortDescription(request.shortDescription())
                .metaTitle(request.metaTitle())
                .metaDescription(request.metaDescription())
                .category(category)
                .featured(request.featured())
                .displayOrder(request.displayOrder())
                .status(ProductStatus.DRAFT)
                .images(request.imageUrls() != null ? request.imageUrls() : List.of())
                .build();

        request.variants().forEach(vr -> product.addVariant(buildVariant(vr)));

        Product saved = productRepository.save(product);
        log.info("Product created: {} ({})", saved.getSlug(), saved.getId());
        return catalogMapper.toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = PRODUCTS,      key = "#id"),
            @CacheEvict(value = PRODUCT_SLUGS, allEntries = true)
    })
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = findEntityOrThrow(id);

        if (request.name() != null) product.setName(request.name().trim());
        if (request.description() != null) product.setDescription(request.description());
        if (request.shortDescription() != null) product.setShortDescription(request.shortDescription());
        if (request.metaTitle() != null) product.setMetaTitle(request.metaTitle());
        if (request.metaDescription() != null) product.setMetaDescription(request.metaDescription());
        if (request.featured() != null) product.setFeatured(request.featured());
        if (request.imageUrls() != null) { product.getImages().clear(); product.getImages().addAll(request.imageUrls()); }
        if (request.status() != null) product.setStatus(ProductStatus.valueOf(request.status()));
        if (request.categoryId() != null) product.setCategory(categoryService.findEntityById(request.categoryId()));
        if (request.displayOrder() != 0)  product.setDisplayOrder(request.displayOrder());

        return catalogMapper.toResponse(productRepository.save(product));
    }

    @Caching(evict = {
            @CacheEvict(value = PRODUCTS, key = "#id"),
            @CacheEvict(value = PRODUCT_SLUGS, allEntries = true)
    })
    @Transactional
    public void archiveProduct(UUID id) {
        Product product = findEntityOrThrow(id);
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
        log.info("Product archived: {}", id);
    }

    // ── Variant management ==>

    @Transactional
    public VariantResponse addVariant(UUID productId, CreateVariantRequest request) {
        Product product = findEntityOrThrow(productId);
        if (variantRepository.existsBySku(request.sku())) {
            throw new ValidationException("SKU already exists: " + request.sku());
        }
        ProductVariant variant = buildVariant(request);
        product.addVariant(variant);
        productRepository.save(product);
        return catalogMapper.toVariantResponse(variant);
    }

    @Transactional
    public VariantResponse updateVariant(UUID variantId, UpdateVariantRequest request) {
        ProductVariant v = variantRepository.findById(variantId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductVariant", variantId));

        if (request.displayName() != null) v.setDisplayName(request.displayName());
        if (request.price() != null) v.setPrice(Money.inr(request.price().toPlainString()));
        if (request.stockQty() != null) v.setStockQty(request.stockQty());
        if (request.lowStockThreshold() != null) v.setLowStockThreshold(request.lowStockThreshold());
        if (request.color() != null) v.setColor(request.color());
        if (request.size() != null) v.setSize(request.size());
        if (request.flavor() != null) v.setFlavor(request.flavor());
        if (request.quantityCount() != null) v.setQuantityCount(request.quantityCount());
        if (request.active() != null) v.setActive(request.active());
        if (request.displayOrder() != null) v.setDisplayOrder(request.displayOrder());

        return catalogMapper.toVariantResponse(variantRepository.save(v));
    }

    @Transactional
    public VariantResponse adjustStock(UUID variantId, AdjustStockRequest request) {
        ProductVariant v = variantRepository.findByIdWithLock(variantId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductVariant", variantId));

        switch (request.operation()) {
            case "ADD" -> v.restoreStock(request.qty());
            case "DEDUCT" -> v.deductStock(request.qty());
            default -> throw new DomainException("Unknown stock operation: " + request.operation(), HttpStatus.BAD_REQUEST);
        }

        log.info("Stock adjusted: variantId={} op={} qty={} reason={}",
                variantId, request.operation(), request.qty(), request.reason());

        return catalogMapper.toVariantResponse(variantRepository.save(v));
    }

    // ── Internal helpers ==>

    Product findEntityOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    private ProductVariant buildVariant(CreateVariantRequest r) {
        return ProductVariant.builder()
                .sku(r.sku().toUpperCase().trim())
                .displayName(r.displayName().trim())
                .price(Money.inr(r.price().toPlainString()))
                .stockQty(r.stockQty())
                .lowStockThreshold(r.lowStockThreshold() > 0 ? r.lowStockThreshold() : 5)
                .color(r.color())
                .size(r.size())
                .flavor(r.flavor())
                .quantityCount(r.quantityCount())
                .displayOrder(r.displayOrder())
                .build();
    }

    private String resolveProductSlug(String provided, String name) {
        String slug = (provided != null && !provided.isBlank())
                ? provided.toLowerCase().trim()
                : SlugUtils.generate(name);

        if (!SlugUtils.isValid(slug)) {
            throw new ValidationException("Invalid slug: " + slug);
        }

        // Ensure uniqueness — append counter if taken
        int attempt = 2;
        String candidate = slug;
        while (productRepository.existsBySlug(candidate)) {
            candidate = SlugUtils.appendSuffix(slug, attempt++);
        }
        return candidate;
    }
}