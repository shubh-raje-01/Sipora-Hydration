package in.sipora.backend.modules.ai.domain;

/**
 * Role of a message in the Gemini conversation history.
 *
 * Maps to Gemini's "user" and "model" role strings.
 * "system" instructions are passed separately via the system_instruction
 * parameter — not as a role in the conversation history.
 */
public enum ChatRole {
    USER,
    MODEL;

    /** Returns the lowercase role string expected by the Gemini API. */
    public String geminiRole() {
        return name().toLowerCase();
    }
}