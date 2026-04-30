package in.sipora.backend.modules.catalog.application;

import in.sipora.backend.modules.catalog.api.CatalogModuleApi;
import in.sipora.backend.modules.catalog.api.ProductSummary;
import in.sipora.backend.modules.catalog.api.VariantSummary;
import in.sipora.backend.modules.catalog.domain.ProductRepository;
import in.sipora.backend.modules.catalog.domain.ProductStatus;
import in.sipora.backend.modules.catalog.domain.ProductVariantRepository;
import in.sipora.backend.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the public module contract consumed by cart, ordering, ai and review.
 *
 * deductStock and restoreStock use findByIdWithLock (SELECT FOR UPDATE)
 * so concurrent orders don't oversell the same variant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogModuleApiImpl implements CatalogModuleApi {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSummary> getProductById(UUID productId) {
        return productRepository.findActiveById(productId)
                .map(catalogMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSummary> getProductBySlug(String slug) {
        return productRepository.findActiveBySlug(slug)
                .map(catalogMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VariantSummary> getVariantById(UUID variantId) {
        return variantRepository.findById(variantId)
                .filter(v -> v.isActive())
                .map(catalogMapper::toVariantSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVariantAvailable(UUID variantId, int requestedQty) {
        return variantRepository.findById(variantId)
                .filter(v -> v.isActive() && v.getStockQty() >= requestedQty)
                .isPresent();
    }

    @Override
    @Transactional
    public void deductStock(UUID variantId, int qty) {
        var variant = variantRepository.findByIdWithLock(variantId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductVariant", variantId));
        variant.deductStock(qty);   // throws DomainException (409) if insufficient
        variantRepository.save(variant);
        log.info("Stock deducted: variantId={} qty={} remaining={}", variantId, qty, variant.getStockQty());
    }

    @Override
    @Transactional
    public void restoreStock(UUID variantId, int qty) {
        var variant = variantRepository.findByIdWithLock(variantId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductVariant", variantId));
        variant.restoreStock(qty);
        variantRepository.save(variant);
        log.info("Stock restored: variantId={} qty={} total={}", variantId, qty, variant.getStockQty());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummary> getProductsByIds(List<UUID> productIds) {
        return productRepository.findByIdIn(productIds)
                .stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(catalogMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummary> getAllActiveProducts() {
        return productRepository.findAllActiveWithVariants()
                .stream()
                .map(catalogMapper::toSummary)
                .toList();
    }
}