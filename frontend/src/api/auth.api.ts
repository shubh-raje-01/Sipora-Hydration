import { apiPost } from "./axios";

// ─── Types ==>

export interface RegisterRequest {
    fullName: string;
    email: string;
    password: string;
    phone?: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: AuthUser;
}

export interface AuthUser {
    id: string;
    fullName: string;
    email: string;
    phone?: string;
    role: "CUSTOMER" | "ADMIN";
    enabled: boolean;
    createdAt: string;
    addresses: Address[];
}

export interface Address {
    index: number;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    pinCode: string;
    country: string;
    label?: string;
}

export interface AddressRequest {
    line1: string;
    line2?: string;
    city: string;
    state: string;
    pinCode: string;
    label?: string;
}

export interface UpdateProfileRequest {
    fullName?: string;
    phone?: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

// ─── Auth endpoints ==>

export const authApi = {

    register: (data: RegisterRequest) =>
        apiPost<AuthResponse>("/auth/register", data),

    login: (data: LoginRequest) =>
        apiPost<AuthResponse>("/auth/login", data),

    refresh: (refreshToken: string) =>
        apiPost<AuthResponse>("/auth/refresh", { refreshToken }),

    logout: (refreshToken: string) =>
        apiPost<void>("/auth/logout", { refreshToken }),

} as const;

// ─── User / profile endpoints ==>

export const userApi = {

    getProfile: () =>
        apiPost<AuthUser>("/users/me"),

    updateProfile: (data: UpdateProfileRequest) =>
        apiPost<AuthUser>("/users/me", data),

    changePassword: (data: ChangePasswordRequest) =>
        apiPost<void>("/users/me/change-password", data),

    getAddresses: () =>
        apiPost<Address[]>("/users/me/addresses"),

    addAddress: (data: AddressRequest) =>
        apiPost<Address[]>("/users/me/addresses", data),

    updateAddress: (index: number, data: AddressRequest) =>
        apiPost<Address[]>(`/users/me/addresses/${index}`, data),

    deleteAddress: (index: number) =>
        apiPost<void>(`/users/me/addresses/${index}`),

} as const;