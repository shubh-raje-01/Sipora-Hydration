package in.sipora.backend.modules.ai.application;

import in.sipora.backend.modules.catalog.api.CatalogModuleApi;
import in.sipora.backend.modules.catalog.api.ProductSummary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Builds the Gemini system prompt that defines the AI advisor's persona
 * and injects the live product catalog as context.
 *
 * The product context is cached for 30 minutes (see RedisConfig.CacheNames.PRODUCTS),
 * so the catalog doesn't get queried on every chat message. When an admin
 * adds a new product, @CacheEvict in ProductService invalidates this automatically.
 *
 * Why inject products into the system prompt rather than using RAG?
 * Sipora's catalog is small (< 50 products). Injecting the full catalog
 * as a system prompt is cheaper, simpler, and gives Gemini full context
 * for comparative recommendations ("which is better for gym use?").
 * Switch to RAG (vector search) when the catalog exceeds ~200 products.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductContextBuilder {

    private final CatalogModuleApi catalogModuleApi;

    private static final String PERSONA = """
            You are "Aire", the friendly hydration expert for Sipora — a premium Indian \
            hydration brand that uses retronasal olfaction (scent) to add flavour to plain \
            water without any sugar, calories or additives.

            Your personality:
            - Warm, knowledgeable, and enthusiastic about healthy hydration
            - You speak in a conversational tone — not robotic or overly formal
            - You answer in the same language the user writes in (English or Hindi)
            - You are honest: if a product doesn't match someone's need, say so

            Your capabilities:
            - Recommend Sipora products based on user preferences, lifestyle, or health goals
            - Explain the science of retronasal olfaction in simple terms
            - Help users understand which flavor pods work best for different occasions
            - Answer questions about order tracking, returns and general FAQs

            Boundaries:
            - Only recommend products from the Sipora catalog listed below
            - Do not invent product names, prices or features
            - If asked something outside hydration / Sipora, politely redirect
            - Never discuss competitor products by name

            """;

    /**
     * Builds the full system prompt: persona + live product catalog.
     * Cached to avoid calling CatalogModuleApi on every chat request.
     */
    @Cacheable(value = "ai-product-context", unless = "#result == null")
    public String buildSystemPrompt() {
        List<ProductSummary> products = catalogModuleApi.getAllActiveProducts();
        String catalogSection = buildCatalogSection(products);
        String prompt = PERSONA + catalogSection;
        log.debug("System prompt built with {} products ({} chars)",
                products.size(), prompt.length());
        return prompt;
    }

    // ── Helpers ==>

    private String buildCatalogSection(List<ProductSummary> products) {
        if (products.isEmpty()) {
            return "\nCURRENT CATALOG: No products are currently active.\n";
        }

        StringBuilder sb = new StringBuilder("\nCURRENT SIPORA PRODUCT CATALOG:\n");
        sb.append("=".repeat(50)).append("\n");

        IntStream.range(0, products.size()).forEach(i -> {
            ProductSummary p = products.get(i);
            sb.append(i + 1).append(". ").append(p.name()).append("\n");
            sb.append("   Price: INR ").append(p.startingPrice()).append(" onwards\n");

            if (p.description() != null && !p.description().isBlank()) {
                // Trim long descriptions to keep prompt concise
                String desc = p.description().length() > 150
                        ? p.description().substring(0, 147) + "..."
                        : p.description();
                sb.append("   About: ").append(desc).append("\n");
            }

            if (p.categoryNames() != null && !p.categoryNames().isEmpty()) {
                sb.append("   Category: ").append(String.join(", ", p.categoryNames())).append("\n");
            }

            sb.append("   In stock: ").append(p.inStock() ? "Yes" : "No").append("\n");
            sb.append("   Product ID: ").append(p.id()).append("\n");
            sb.append("\n");
        });

        sb.append("=".repeat(50)).append("\n");
        sb.append("When recommending a product, always include its Product ID so the frontend\n");
        sb.append("can display the correct product card. Format: [PRODUCT_ID:uuid-here]\n");

        return sb.toString();
    }
}