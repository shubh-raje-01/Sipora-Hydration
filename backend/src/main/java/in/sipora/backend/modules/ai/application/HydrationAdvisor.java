package in.sipora.backend.modules.ai.application;

import in.sipora.backend.modules.ai.domain.ChatMessage;
import in.sipora.backend.modules.ai.infrastructure.GeminiClient;
import in.sipora.backend.modules.ai.web.AiDTOs.ChatRequest;
import in.sipora.backend.modules.ai.web.AiDTOs.ChatResponse;
import in.sipora.backend.modules.ai.web.AiDTOs.RecommendRequest;
import in.sipora.backend.modules.ai.web.AiDTOs.RecommendResponse;
import in.sipora.backend.modules.ai.web.AiDTOs.StreamEvent;
import in.sipora.backend.modules.catalog.api.CatalogModuleApi;
import in.sipora.backend.modules.catalog.api.ProductSummary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Core AI service — Sipora's "Aire" hydration advisor.
 *
 * Orchestrates:
 *  - ChatHistoryStore (retrieve + persist conversation context)
 *  - ProductContextBuilder (build system prompt with live catalog)
 *  - GeminiClient (send to Gemini API)
 *  - RecommendationParser (extract product IDs from response)
 *
 * Two interaction modes:
 *  1. chat() — blocking request/response (simple integration)
 *  2. chatStream() — async SSE streaming (typewriter effect in frontend)
 *
 * For recommendations, a specialized prompt guides Gemini to always
 * include [PRODUCT_ID:uuid] tags so the frontend can render product cards
 * alongside the text response.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HydrationAdvisor {

    private final GeminiClient geminiClient;
    private final ChatHistoryStore historyStore;
    private final ProductContextBuilder contextBuilder;
    private final CatalogModuleApi catalogModuleApi;

    // ── Blocking chat ==>

    /**
     * Processes a user message and returns the complete model reply.
     * History is loaded, sent to Gemini, then updated with the exchange.
     */
    public ChatResponse chat(ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        String systemPrompt = contextBuilder.buildSystemPrompt();

        List<ChatMessage> history = historyStore.getHistory(sessionId);
        ChatMessage userMsg = ChatMessage.userMessage(request.message());

        String rawResponse = geminiClient.chat(history, request.message(), systemPrompt);
        ChatMessage modelMsg = ChatMessage.modelMessage(rawResponse);

        historyStore.appendExchange(sessionId, userMsg, modelMsg);

        List<UUID> productIds = RecommendationParser.extractProductIds(rawResponse);
        String cleanText = RecommendationParser.cleanText(rawResponse);

        log.debug("Chat response: sessionId={} productRefs={}", sessionId, productIds.size());

        return new ChatResponse(sessionId, cleanText, productIds.isEmpty() ? null : productIds, Instant.now());
    }

    // ── Streaming chat ==>

    /**
     * Streams the model response to the SseEmitter token-by-token.
     * Runs in the async "taskExecutor" thread pool — does NOT block the request thread.
     *
     * SSE event format:
     *   data: {"type":"token","content":"Hello"}\n\n
     *   data: {"type":"token","content":", how"}\n\n
     *   ...
     *   data: {"type":"done","recommendedProductIds":[...],"sessionId":"..."}\n\n
     */
    @Async("taskExecutor")
    public void chatStream(ChatRequest request, SseEmitter emitter) {
        String sessionId = resolveSessionId(request.sessionId());
        String systemPrompt = contextBuilder.buildSystemPrompt();

        List<ChatMessage> history = historyStore.getHistory(sessionId);
        StringBuilder fullResponse = new StringBuilder();

        try {
            geminiClient.chatStream(
                    history,
                    request.message(),
                    systemPrompt,
                    token -> {
                        fullResponse.append(token);
                        sendSseEvent(emitter, StreamEvent.token(token));
                    }
            );

            // Persist the complete exchange to history
            ChatMessage userMsg = ChatMessage.userMessage(request.message());
            ChatMessage modelMsg = ChatMessage.modelMessage(fullResponse.toString());
            historyStore.appendExchange(sessionId, userMsg, modelMsg);

            // Send the final "done" event with product references
            List<UUID> productIds = RecommendationParser.extractProductIds(fullResponse.toString());
            sendSseEvent(emitter, StreamEvent.done(
                    productIds.isEmpty() ? null : productIds, sessionId));

            emitter.complete();
        } catch (Exception e) {
            log.error("Error during AI stream for session {}", sessionId, e);
            sendSseEvent(emitter, StreamEvent.error("AI service error. Please try again."));
            emitter.completeWithError(e);
        }
    }

    // ── Recommendations ==>

    /**
     * Generates tailored product recommendations based on user preference.
     * Uses a dedicated prompt structure that forces Gemini to reference
     * specific products from the catalog with [PRODUCT_ID: ...] tags.
     */
    public RecommendResponse recommend(RecommendRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        String systemPrompt = contextBuilder.buildSystemPrompt();

        String recommendPrompt = buildRecommendPrompt(request);
        List<ChatMessage> history = historyStore.getHistory(sessionId);
        ChatMessage userMsg = ChatMessage.userMessage(recommendPrompt);

        String rawResponse = geminiClient.chat(history, recommendPrompt, systemPrompt);
        ChatMessage modelMsg = ChatMessage.modelMessage(rawResponse);
        historyStore.appendExchange(sessionId, userMsg, modelMsg);

        List<UUID> productIds = RecommendationParser.extractProductIds(rawResponse);

        // Enrich with live stock check — filter out out-of-stock products
        List<UUID> availableIds = productIds.stream()
                .filter(id -> catalogModuleApi.getProductById(id)
                        .map(ProductSummary::inStock)
                        .orElse(false))
                .toList();

        String cleanText = RecommendationParser.cleanText(rawResponse);

        log.info("Recommendations generated: sessionId={} products={}",
                sessionId, availableIds.size());

        return new RecommendResponse(
                sessionId, cleanText,
                availableIds.isEmpty() ? null : availableIds,
                Instant.now());
    }

    // ── Clear session ==>

    public void clearSession(String sessionId) {
        historyStore.clearHistory(sessionId);
    }

    // ── Helpers ==>

    private String resolveSessionId(String provided) {
        return (provided != null && !provided.isBlank())
                ? provided
                : ChatHistoryStore.newSessionId();
    }

    private String buildRecommendPrompt(RecommendRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("I need product recommendations. Here's what I'm looking for:\n");
        sb.append("Preference: ").append(request.preference()).append("\n");

        if (request.useCase() != null && !request.useCase().isBlank()) {
            sb.append("Use case: ").append(request.useCase()).append("\n");
        }
        if (request.maxBudget() != null) {
            sb.append("Budget: up to INR ").append(request.maxBudget()).append("\n");
        }

        sb.append("\nPlease recommend the 2-3 most suitable Sipora products. ");
        sb.append("For each recommendation, include [PRODUCT_ID:uuid] tag and explain why it suits me.");
        return sb.toString();
    }

    private void sendSseEvent(SseEmitter emitter, StreamEvent event) {
        try {
            emitter.send(SseEmitter.event().data(event));
        } catch (IOException e) {
            // Client disconnected — not an error, stop streaming silently
            log.debug("SSE client disconnected: {}", e.getMessage());
        }
    }
}