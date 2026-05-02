package in.sipora.backend.modules.ai.application;

import in.sipora.backend.modules.ai.domain.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages per-session conversation history in Redis.
 *
 * Design:
 *  - Key: "ai:chat:{sessionId}"
 *  - Value: JSON array of ChatMessage records
 *  - TTL: SESSION_TTL (2 hours, refreshed on every interaction)
 *  - Cap: MAX_HISTORY_SIZE messages (sliding window — oldest dropped first)
 *
 * The sliding window is critical — Gemini has a context window limit,
 * and sending 50+ messages would hit token limits and increase latency.
 * Keeping 20 messages (10 exchanges) is the sweet spot for Sipora's
 * hydration advisor use case.
 *
 * sessionId can be:
 *  - A user UUID (for authenticated users — persistent across devices)
 *  - A UUID from the frontend localStorage (for guests)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHistoryStore {

    private static final String KEY_PREFIX = "ai:chat:";
    private static final int MAX_HISTORY_SIZE = 20;   // 10 exchanges
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, Object> redisTemplate;
    private final JsonMapper objectMapper;

    // ── Read ==>

    public List<ChatMessage> getHistory(String sessionId) {
        String key = key(sessionId);
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) return new ArrayList<>();

            String json = raw instanceof String s ? s : objectMapper.writeValueAsString(raw);
            List<ChatMessage> history = objectMapper.readValue(json,
                    new TypeReference<>() {});

            // Refresh TTL on read (keep active sessions alive)
            redisTemplate.expire(key, SESSION_TTL);
            return history;
        } catch (Exception e) {
            log.warn("Failed to read chat history for session {}: {}", sessionId, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Write ==>

    /**
     * Appends a USER message and the MODEL reply to the session history.
     * Applies the sliding window cap before persisting.
     *
     * Both messages are always appended together to maintain the
     * strict USER/MODEL alternation that Gemini requires.
     */
    public void appendExchange(String sessionId, ChatMessage userMsg, ChatMessage modelMsg) {
        List<ChatMessage> history = getHistory(sessionId);
        history.add(userMsg);
        history.add(modelMsg);

        // Sliding window — drop oldest pairs first (always drop in pairs of 2)
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0); // oldest USER
            if (!history.isEmpty()) history.remove(0); // its MODEL reply
        }

        persist(sessionId, history);
    }

    // ── Clear ==>

    public void clearHistory(String sessionId) {
        redisTemplate.delete(key(sessionId));
        log.debug("Chat history cleared for session: {}", sessionId);
    }

    // ── Session management ==>

    /**
     * Generates a new session ID for a guest or returns the user's ID string
     * for authenticated sessions. Callers decide which to use.
     */
    public static String newSessionId() {
        return UUID.randomUUID().toString();
    }

    public boolean hasHistory(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(sessionId)));
    }

    // ── Internal ==>

    private void persist(String sessionId, List<ChatMessage> history) {
        try {
            String json = objectMapper.writeValueAsString(history);
            redisTemplate.opsForValue().set(key(sessionId), json, SESSION_TTL);
        } catch (Exception e) {
            log.error("Failed to persist chat history for session {}", sessionId, e);
        }
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
