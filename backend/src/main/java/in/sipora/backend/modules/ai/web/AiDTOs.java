package in.sipora.backend.modules.ai.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * All request and response records for the AI module web layer.
 */
public final class AiDTOs {

    private AiDTOs() {}

    // Requests ==>

    public record ChatRequest(
            @NotBlank(message = "Message cannot be blank")
            @Size(max = 1000, message = "Message must be under 1000 characters")
            String message,

            /**
             * Client-generated session UUID.
             * Authenticated users: pass their userId (persistent sessions).
             * Guests: pass a UUID from localStorage (temporary session).
             * If null, a new session is created and returned in the response.
             */
            String sessionId
    ) {}

    public record RecommendRequest(
            @NotBlank(message = "Describe your preference")
            @Size(max = 500, message = "Preference description must be under 500 characters")
            String preference,

            /** Optional: "gym", "office", "gifting", "kids", etc. */
            String useCase,

            /** Optional: budget in INR for filtering. */
            Integer maxBudget,

            String sessionId
    ) {}

    // Responses ==>

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChatResponse(
            String sessionId,
            String message,
            List<UUID> recommendedProductIds,   // product IDs for frontend to render cards
            Instant timestamp
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecommendResponse(
            String sessionId,
            String message,
            List<UUID> recommendedProductIds,
            Instant timestamp
    ) {}

    /**
     * Sent as individual SSE events during streaming.
     * type = "token" — partial response chunk (render live)
     * type = "done" — stream complete, includes final product IDs
     * type = "error" — something went wrong
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StreamEvent(
            String type,
            String content,
            List<UUID> recommendedProductIds,
            String sessionId
    ) {
        public static StreamEvent token(String chunk) {
            return new StreamEvent("token", chunk, null, null);
        }

        public static StreamEvent done(List<UUID> productIds, String sessionId) {
            return new StreamEvent("done", null, productIds, sessionId);
        }

        public static StreamEvent error(String message) {
            return new StreamEvent("error", message, null, null);
        }
    }

    public record ClearSessionResponse(String sessionId, String message) {}
}
