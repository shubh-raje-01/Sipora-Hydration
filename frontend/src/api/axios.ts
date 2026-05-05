import axios, {
    type AxiosInstance,
    type AxiosRequestConfig,
    type AxiosResponse,
    type InternalAxiosRequestConfig,
} from "axios";

// ─── Shared response envelope types ->
// These mirror the Java records in - in.sipora.shared.web

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    errorCode?: string;
    timestamp: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

// ─── In-memory token store ==>
// Access token NEVER goes in localStorage (XSS risk).
// Lives in module-level memory — cleared on page reload (intentional).
// On reload, the refresh interceptor silently re-issues a new pair.

let _accessToken: string | null = null;

export const tokenStore = {
    get: ()=> _accessToken,
    set: (t: string)=> { _accessToken = t; },
    clear: ()=> { _accessToken = null; },
};

// ─── Axios instance ==>

const api: AxiosInstance = axios.create({
    baseURL: "/api/v1",
    timeout: 15_000,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

// ─── Request interceptor ==>
// Attaches Bearer token + guest session ID to every outgoing request.

api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = tokenStore.get();
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // Guest cart identity — backend reads this as X-Session-Id
        const sid = getSessionId();
        if (sid && config.headers) {
            config.headers["X-Session-Id"] = sid;
        }

        return config;
    },
    (err) => Promise.reject(err)
);

// ─── Response interceptor — silent token refresh on 401 ─────────────
// Flow:
//   1. Request fails with 401
//   2. Interceptor calls POST /auth/refresh with the stored refresh token
//   3. Stores the new access token in memory
//   4. Retries the original failed request transparently
//   5. If refresh also fails → fires sipora:logout event → Zustand clears state

let _isRefreshing = false;
type QueueItem = { resolve: (t: string) => void; reject: (e: unknown) => void };
let _refreshQueue: QueueItem[] = [];

const flushQueue = (err: unknown, token: string | null) => {
    _refreshQueue.forEach(({ resolve, reject }) =>
        err ? reject(err) : resolve(token!)
    );
    _refreshQueue = [];
};

api.interceptors.response.use(
    (res: AxiosResponse) => res,
    async (error) => {
        const orig = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        const is401 = error.response?.status === 401;
        const isAuthEndpt = (orig.url ?? "").includes("/auth/");
        const alreadyRetry = orig._retry === true;

        if (!is401 || isAuthEndpt || alreadyRetry) {
            return Promise.reject(error);
        }

        orig._retry = true;

        const rt = getRefreshToken();
        if (!rt) {
            triggerLogout();
            return Promise.reject(error);
        }

        // Queue concurrent requests while a refresh is already in flight
        if (_isRefreshing) {
            return new Promise<string>((resolve, reject) => {
                _refreshQueue.push({ resolve, reject });
            }).then((newToken) => {
                if (orig.headers) orig.headers.Authorization = `Bearer ${newToken}`;
                return api(orig);
            });
        }

        _isRefreshing = true;

        try {
            const { data } = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string }>>(
                "/api/v1/auth/refresh",
                { refreshToken: rt }
            );

            const { accessToken: newAt, refreshToken: newRt } = data.data;
            tokenStore.set(newAt);
            setRefreshToken(newRt);
            flushQueue(null, newAt);

            if (orig.headers) orig.headers.Authorization = `Bearer ${newAt}`;
            return api(orig);

        } catch (refreshErr) {
            flushQueue(refreshErr, null);
            triggerLogout();
            return Promise.reject(refreshErr);

        } finally {
            _isRefreshing = false;
        }
    }
);

// ─── Refresh token persistence ==>
// Refresh token = opaque UUID (not a JWT), stored in localStorage.
// 7-day TTL. Acceptable tradeoff for persistent sessions.

const RT_KEY = "sipora_rt";
export const getRefreshToken = () => localStorage.getItem(RT_KEY);
export const setRefreshToken = (t: string) => localStorage.setItem(RT_KEY, t);
export const clearRefreshToken = () => localStorage.removeItem(RT_KEY);

// ─── Guest session ID ==>
// Generated once per browser, persisted in localStorage.
// Sent as X-Session-Id so the backend can track anonymous carts.

const SID_KEY = "sipora_sid";

export const getSessionId = (): string => {
    let sid = localStorage.getItem(SID_KEY);
    if (!sid) {
        sid = crypto.randomUUID();
        localStorage.setItem(SID_KEY, sid);
    }
    return sid;
};

export const clearSessionId = () => localStorage.removeItem(SID_KEY);

// ─── Logout trigger ==>
// Fires a CustomEvent instead of directly calling the Zustand store
// to avoid a circular import (axios ↔ store).
// useAuth hook listens for this event and clears Zustand state.

export const triggerLogout = () => {
    tokenStore.clear();
    clearRefreshToken();
    window.dispatchEvent(new CustomEvent("sipora:logout"));
};

// ─── Typed convenience wrappers ==>
// Unwrap ApiResponse<T> so callsites get T directly, not the envelope.
// All API modules use these instead of raw api.get / api.post.

export const apiGet = async <T>(
    url: string,
    config?: AxiosRequestConfig
): Promise<T> => {
    const res = await api.get<ApiResponse<T>>(url, config);
    return res.data.data;
};

export const apiPost = async <T>(
    url: string,
    body?: unknown,
    config?: AxiosRequestConfig
): Promise<T> => {
    const res = await api.post<ApiResponse<T>>(url, body, config);
    return res.data.data;
};

export const apiPatch = async <T>(
    url: string,
    body?: unknown,
    config?: AxiosRequestConfig
): Promise<T> => {
    const res = await api.patch<ApiResponse<T>>(url, body, config);
    return res.data.data;
};

export const apiDelete = async <T = void>(
    url: string,
    config?: AxiosRequestConfig
): Promise<T> => {
    const res = await api.delete<ApiResponse<T>>(url, config);
    return res.data.data;
};

export default api;