import { apiDelete, apiPost } from "./axios";

// ─── Types ==>

export interface ChatRequest {
    message: string;
    sessionId?: string;
}

export interface ChatResponse {
    sessionId: string;
    message: string;
    recommendedProductIds?: string[];
    timestamp: string;
}

export interface RecommendRequest {
    preference: string;
    useCase?: string;
    maxBudget?: number;
    sessionId?: string;
}

export interface RecommendResponse {
    sessionId: string;
    message: string;
    recommendedProductIds?: string[];
    timestamp: string;
}

// SSE stream event types — mirrors StreamEvent record in Spring Boot
export interface StreamToken {
    type: "token";
    content: string;
}

export interface StreamDone {
    type: "done";
    recommendedProductIds?: string[];
    sessionId: string;
}

export interface StreamError {
    type: "error";
    content: string;
}

export type StreamEvent = StreamToken | StreamDone | StreamError;

// ─── Callbacks for streamChat

export interface StreamCallbacks {
    onToken: (chunk: string) => void;
    onDone:  (event: StreamDone) => void;
    onError?: (message: string) => void;
    onClose?: () => void;
}

// ─── Endpoints ==>

export const aiApi = {

    // ── Blocking chat (simple, no streaming)
    chat: (data: ChatRequest) =>
        apiPost<ChatResponse>("/ai/chat", data),

    // ── Streaming chat via SSE
    // Uses the native fetch API (not axios) because axios doesn't support
    // streaming responses. Returns an AbortController so the caller can
    // cancel the stream (e.g. when the component unmounts or user sends
    // a new message before the current one finishes).
    //
    // Usage in useSSE hook:
    //   const abort = aiApi.streamChat({ message, sessionId }, {
    //     onToken: (chunk) => appendToCurrentMessage(chunk),
    //     onDone:  (ev)    => setProductIds(ev.recommendedProductIds),
    //     onError: (msg)   => showError(msg),
    //   });
    //   return () => abort.abort(); // cleanup on unmount

    streamChat: (data: ChatRequest, callbacks: StreamCallbacks): AbortController => {
        const controller = new AbortController();

        const token = (() => {
            // Import tokenStore lazily to avoid circular deps
            const { tokenStore } = require("./axios") as typeof import("./axios");
            return tokenStore.get();
        })();

        const sessionId = (() => {
            const { getSessionId } = require("./axios") as typeof import("./axios");
            return getSessionId();
        })();

        const headers: Record<string, string> = {
            "Content-Type": "application/json",
        };
        if (token)     headers["Authorization"]  = `Bearer ${token}`;
        if (sessionId) headers["X-Session-Id"]   = sessionId;

        fetch("/api/v1/ai/chat/stream", {
            method: "POST",
            headers,
            body: JSON.stringify(data),
            signal: controller.signal,
        })
            .then(async (response) => {
                if (!response.ok || !response.body) {
                    callbacks.onError?.("Stream connection failed");
                    return;
                }

                const reader  = response.body.getReader();
                const decoder = new TextDecoder();
                let   buffer  = "";

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    // SSE format: "data: {json}\n\n"
                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split("\n\n");
                    buffer = lines.pop() ?? "";    // keep any incomplete chunk

                    for (const line of lines) {
                        const dataLine = line.replace(/^data:\s*/, "").trim();
                        if (!dataLine) continue;

                        try {
                            const event = JSON.parse(dataLine) as StreamEvent;

                            if (event.type === "token") {
                                callbacks.onToken(event.content);
                            } else if (event.type === "done") {
                                callbacks.onDone(event);
                                callbacks.onClose?.();
                            } else if (event.type === "error") {
                                callbacks.onError?.(event.content);
                            }
                        } catch {
                            // Malformed SSE chunk — skip silently
                        }
                    }
                }

                callbacks.onClose?.();
            })
            .catch((err: Error) => {
                if (err.name !== "AbortError") {
                    callbacks.onError?.(err.message ?? "Stream error");
                }
            });

        return controller;
    },

    // ── Product recommendations
    recommend: (data: RecommendRequest) =>
        apiPost<RecommendResponse>("/ai/recommend", data),

    // ── Clear session history
    clearSession: (sessionId: string) =>
        apiDelete<{ sessionId: string; message: string }>(`/ai/sessions/${sessionId}`),

} as const;