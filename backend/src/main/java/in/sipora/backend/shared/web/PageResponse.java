package in.sipora.backend.shared.web;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Shared Kernel — serializable wrapper for paginated results.
 *
 * Spring's Page<T> is not directly serializable into a clean JSON shape.
 * This record flattens it into what the frontend actually needs.
 *
 * JSON output:
 * {
 *   "content": [ ...items... ],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 143,
 *   "totalPages": 8,
 *   "first": true,
 *   "last": false
 * }
 *
 * Usage in a service or controller:
 *   Page<ProductDTO> page = productRepository.findAll(spec, pageable);
 *   return PageResponse.from(page);
 *
 * Then wrap in ApiResponse:
 *   return ResponseEntity.ok(ApiResponse.success("Products retrieved", PageResponse.from(page)));
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isFirst(),
                springPage.isLast()
        );
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    public boolean hasContent() {
        return !isEmpty();
    }
}