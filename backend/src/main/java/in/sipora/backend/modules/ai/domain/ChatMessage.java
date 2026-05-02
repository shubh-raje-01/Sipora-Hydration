package in.sipora.backend.modules.ai.domain;

import java.time.Instant;

/**
 * Represents one turn in a conversation.
 *
 * Stored as JSON in Redis keyed by sessionId.
 * Not a JPA entity — chat history is ephemeral (2-hour TTL).
 *
 * role = USER or MODEL
 * content = the raw text of the message
 * sentAt = timestamp for display in the chat UI
 */
public record ChatMessage(
        ChatRole role,
        String content,
        Instant sentAt
) {
    public static ChatMessage userMessage(String content) {
        return new ChatMessage(ChatRole.USER, content, Instant.now());
    }

    public static ChatMessage modelMessage(String content) {
        return new ChatMessage(ChatRole.MODEL, content, Instant.now());
    }
}