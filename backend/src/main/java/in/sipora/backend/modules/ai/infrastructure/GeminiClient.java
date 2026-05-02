package in.sipora.backend.modules.ai.infrastructure;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseStream;

import in.sipora.backend.modules.ai.domain.ChatMessage;
import in.sipora.backend.modules.ai.domain.ChatRole;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Infrastructure wrapper for the Google Vertex AI (Gemini) SDK.
 *
 * Responsibilities:
 *  - Manages the VertexAI client lifecycle (init / shutdown)
 *  - Converts ChatMessage history to Gemini's Content API format
 *  - Provides both blocking (chat) and streaming (chatStream) methods
 *  - Never contains business logic — that lives in HydrationAdvisor
 *
 * Model: gemini-2.0-flash — fast, low-latency, ideal for chat
 *
 * The system instruction is passed per-call so HydrationAdvisor can inject
 * the live product catalog context without rebuilding the model instance.
 */
@Slf4j
@Component
public class GeminiClient {

    @Value("${sipora.ai.gemini.project-id}")
    private String projectId;

    @Value("${sipora.ai.gemini.location}")
    private String location;

    @Value("${sipora.ai.gemini.model}")
    private String modelName;

    private VertexAI vertexAI;

    @PostConstruct
    public void init() {
        this.vertexAI = new VertexAI(projectId, location);
        log.info("GeminiClient initialised: project={} location={} model={}",
                projectId, location, modelName);
    }

    @PreDestroy
    public void destroy() {
        if (vertexAI != null) {
            try {
                vertexAI.close();
            } catch (Exception e) {
                log.warn("Error closing VertexAI client", e);
            }
        }
    }

    // ── Blocking chat ==>

    /**
     * Sends the full conversation history + new user message to Gemini
     * and returns the complete model reply as a String.
     *
     * @param history previous turns (maybe empty for the first message)
     * @param userMessage the new user input
     * @param systemPrompt injected product context + persona instructions
     * @return complete model response text
     */
    public String chat(List<ChatMessage> history, String userMessage, String systemPrompt) {
        try {
            GenerativeModel model = buildModel(systemPrompt);
            List<Content> contents = buildContents(history, userMessage);
            GenerateContentResponse response = model.generateContent(contents);
            return extractText(response);
        } catch (IOException e) {
            log.error("Gemini API error during chat", e);
            throw new in.sipora.backend.shared.exception.DomainException(
                    "AI service is temporarily unavailable. Please try again.",
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }

    // ── Streaming chat

    /**
     * Streams the Gemini response token-by-token.
     * Each chunk is delivered to the tokenConsumer as it arrives.
     *
     * The caller (HydrationAdvisor) passes each chunk to an SseEmitter
     * so the React frontend receives a live typewriter effect.
     *
     * @param history previous turns
     * @param userMessage new user input
     * @param systemPrompt injected product context + persona
     * @param tokenConsumer callback invoked for each streamed text chunk
     * @return the full assembled response (for persisting to history)
     */
    public String chatStream(
            List<ChatMessage> history,
            String userMessage,
            String systemPrompt,
            Consumer<String> tokenConsumer) {

        StringBuilder fullResponse = new StringBuilder();
        try {
            GenerativeModel model = buildModel(systemPrompt);
            List<Content> contents = buildContents(history, userMessage);

            ResponseStream<GenerateContentResponse> stream =
                    model.generateContentStream(contents);

            for (GenerateContentResponse chunk : stream) {
                String token = extractText(chunk);
                if (!token.isEmpty()) {
                    fullResponse.append(token);
                    tokenConsumer.accept(token);
                }
            }
        } catch (IOException e) {
            log.error("Gemini API streaming error", e);
            throw new in.sipora.backend.shared.exception.DomainException(
                    "AI stream interrupted. Please try again.",
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, e);
        }
        return fullResponse.toString();
    }

    // ── Helpers ==>

    private GenerativeModel buildModel(String systemPrompt) {
        return new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAI)
                .setSystemInstruction(ContentMaker.fromString(systemPrompt))
                .build();
    }

    /**
     * Converts the internal ChatMessage list + new user message
     * into the Content list format required by the Gemini SDK.
     *
     * History must alternate USER / MODEL — Gemini rejects consecutive
     * same-role messages. The ChatHistoryStore enforces this invariant.
     */
    private List<Content> buildContents(List<ChatMessage> history, String userMessage) {
        List<Content> contents = new ArrayList<>();

        for (ChatMessage msg : history) {
            contents.add(Content.newBuilder()
                    .setRole(msg.role().geminiRole())
                    .addParts(Part.newBuilder().setText(msg.content()).build())
                    .build());
        }

        // Append the new user message
        contents.add(Content.newBuilder()
                .setRole(ChatRole.USER.geminiRole())
                .addParts(Part.newBuilder().setText(userMessage).build())
                .build());

        return contents;
    }

    private String extractText(GenerateContentResponse response) {
        try {
            return response.getCandidates(0)
                    .getContent()
                    .getParts(0)
                    .getText();
        } catch (Exception e) {
            return "";
        }
    }
}