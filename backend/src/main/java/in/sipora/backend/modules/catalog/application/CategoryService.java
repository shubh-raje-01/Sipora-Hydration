package in.sipora.backend.modules.catalog.application;

import in.sipora.backend.modules.catalog.domain.Category;
import in.sipora.backend.modules.catalog.domain.CategoryRepository;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CategoryResponse;
import in.sipora.backend.modules.catalog.web.CatalogDTOs.CreateCategoryRequest;
import in.sipora.backend.shared.exception.ResourceNotFoundException;
import in.sipora.backend.shared.exception.ValidationException;
import in.sipora.backend.shared.util.SlugUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static in.sipora.backend.config.RedisConfig.CacheNames.CATEGORIES;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper      catalogMapper;

    @Cacheable(CATEGORIES)
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllActiveWithChildren()
                .stream()
                .filter(Category::isRoot)
                .map(catalogMapper::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .filter(Category::isActive)
                .map(catalogMapper::toCategoryResponse)
                .orElseThrow(() -> ResourceNotFoundException.ofSlug("Category", slug));
    }

    @CacheEvict(value = CATEGORIES, allEntries = true)
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String slug = resolveSlug(request.slug(), request.name());

        Category.CategoryBuilder builder = Category.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description())
                .imageUrl(request.imageUrl())
                .displayOrder(request.displayOrder());

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Category", request.parentId()));
            builder.parent(parent);
        }

        return catalogMapper.toCategoryResponse(categoryRepository.save(builder.build()));
    }

    // ── Helpers

    private String resolveSlug(String providedSlug, String name) {
        String slug = (providedSlug != null && !providedSlug.isBlank())
                ? providedSlug.toLowerCase().trim()
                : SlugUtils.generate(name);

        if (!SlugUtils.isValid(slug)) {
            throw new ValidationException("Invalid slug format: " + slug);
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw new ValidationException("A category with slug '" + slug + "' already exists");
        }
        return slug;
    }

    public Category findEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
    }
}