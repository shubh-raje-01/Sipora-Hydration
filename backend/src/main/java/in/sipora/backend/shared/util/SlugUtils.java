package in.sipora.backend.shared.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Shared Kernel — URL-safe slug generator.
 *
 * Converts human-readable strings into clean, lowercase, hyphenated slugs
 * suitable for use in URLs and as unique identifiers.
 *
 * Examples:
 *   "Sipora Glass Bottle — 700ml" ->  "sipora-glass-bottle-700ml"
 *   "100% Natural Hydration!" ->  "100-natural-hydration"
 *   "Café & Lemon Twist" ->  "cafe-lemon-twist"
 */
public final class SlugUtils {

    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern NON_ALPHANUM = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-{2,}");
    private static final Pattern LEADING_TRAILING_HYPHEN = Pattern.compile("^-|-$");

    private SlugUtils() {}

    /**
     * Generates a slug from the given input string.
     *
     * @param input raw display name, may contain Unicode, spaces, punctuation
     * @return lowercase hyphenated slug, safe for URLs and slugs columns
     * @throws IllegalArgumentException if input is null or blank
     */
    public static String generate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Cannot generate slug from null or blank string");
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        return NON_ASCII.matcher(normalized).replaceAll("")
                .toLowerCase(Locale.ROOT)
                .transform(s -> NON_ALPHANUM.matcher(s).replaceAll(" "))
                .transform(s -> WHITESPACE.matcher(s).replaceAll("-"))
                .transform(s -> MULTI_HYPHEN.matcher(s).replaceAll("-"))
                .transform(s -> LEADING_TRAILING_HYPHEN.matcher(s).replaceAll(""))
                .strip();
    }

    /**
     * Appends a short suffix to an existing slug to make it unique.
     * Used when a slug already exists in the database.
     *
     * Example: "sipora-bottle" + 2 -> "sipora-bottle-2"
     *
     * @param slug  base slug (already generated)
     * @param count iteration count, must be >= 2
     * @return suffixed slug
     */
    public static String appendSuffix(String slug, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("count must be >= 2 when appending suffix");
        }
        return slug + "-" + count;
    }

    /**
     * Validates that a slug is well-formed (only lowercase letters, digits, hyphens).
     * Useful for validating user-supplied slugs in admin APIs.
     */
    public static boolean isValid(String slug) {
        if (slug == null || slug.isBlank()) return false;
        return slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$");
    }
}