import { apiDelete, apiGet, apiPatch, apiPost } from "./axios";

// ─── Types ==>

export interface CartItem {
    cartItemId: string;
    productId: string;
    variantId: string;
    productName: string;
    variantName: string;
    sku: string;
    imageUrl?: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
    currencyCode: string;
}

export interface Cart {
    cartId: string;
    items: CartItem[];
    totalItems: number;
    subtotal: number;
    currencyCode: string;
}

// ─── Endpoints ==>

export const cartApi = {

    getCart: () =>
        apiGet<Cart>("/cart"),

    addItem: (variantId: string, quantity: number) =>
        apiPost<Cart>("/cart/items", { variantId, quantity }),

    updateItem: (cartItemId: string, quantity: number) =>
        apiPatch<Cart>(`/cart/items/${cartItemId}`, { quantity }),

    removeItem: (cartItemId: string) =>
        apiDelete<Cart>(`/cart/items/${cartItemId}`),

    clearCart: () =>
        apiDelete<void>("/cart"),

    // Called immediately after login to fold the guest cart into the user cart
    mergeCart: (sessionId: string) =>
        apiPost<Cart>("/cart/merge", { sessionId }),

} as const;