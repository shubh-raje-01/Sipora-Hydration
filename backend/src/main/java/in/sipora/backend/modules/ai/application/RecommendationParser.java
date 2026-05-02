package in.sipora.backend.modules.ai.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses [PRODUCT_ID:uuid] tags from Gemini response text.
 *
 * The system prompt instructs Gemini to embed product IDs in its response
 * using the format [PRODUCT_ID:550e8400-e29b-41d4-a716-446655440000].
 *
 * The frontend uses these IDs to render product cards alongside the
 * chat text — creating a richer shopping-assistant experience.
 *
 * This parser:
 *  1. Extracts all valid UUIDs from the tags
 *  2. Strips the tags from the display text (clean text shown to user)
 */
public final class RecommendationParser {

    private static final Pattern PRODUCT_TAG =
            Pattern.compile("\\[PRODUCT_ID:([0-9a-fA-F\\-]{36})\\]");

    private RecommendationParser() {}

    /**
     * Extracts product IDs embedded in the model response.
     *
     * @param modelResponse raw text from Gemini
     * @return list of product UUIDs found in the response (may be empty)
     */
    public static List<UUID> extractProductIds(String modelResponse) {
        List<UUID> ids = new ArrayList<>();
        if (modelResponse == null) return ids;

        Matcher matcher = PRODUCT_TAG.matcher(modelResponse);
        while (matcher.find()) {
            try {
                ids.add(UUID.fromString(matcher.group(1)));
            } catch (IllegalArgumentException ignored) {
                // Malformed UUID from model — skip silently
            }
        }
        return ids;
    }

    /**
     * Removes [PRODUCT_ID:...] tags from the text so the user sees
     * clean prose without the internal markup.
     *
     * @param modelResponse raw text from Gemini
     * @return display-ready text with tags stripped
     */
    public static String cleanText(String modelResponse) {
        if (modelResponse == null) return "";
        return PRODUCT_TAG.matcher(modelResponse).replaceAll("").trim();
    }
}