import { apiGet, apiPost, type PageResponse } from "./axios";

// ─── Types ==>

export interface ShippingAddress {
    fullName: string;
    phone: string;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    pinCode: string;
    country: string;
}

export interface OrderItem {
    orderItemId: string;
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

export interface Order {
    orderId: string;
    orderNumber: string;
    status: OrderStatus;
    subtotal: number;
    shippingCharge: number;
    totalAmount: number;
    currencyCode: string;
    shippingAddress: ShippingAddress;
    trackingNumber?: string;
    cancelReason?: string;
    createdAt: string;
    shippedAt?: string;
    deliveredAt?: string;
    cancelledAt?: string;
    items: OrderItem[];
}

export interface OrderCard {
    orderId: string;
    orderNumber: string;
    status: OrderStatus;
    itemCount: number;
    totalAmount: number;
    currencyCode: string;
    createdAt: string;
}

export interface PlaceOrderResponse {
    orderId: string;
    orderNumber: string;
    status: string;
    totalAmount: number;
    currencyCode: string;
}

export interface PlaceOrderRequest {
    shippingAddress: {
        fullName: string;
        phone: string;
        line1: string;
        line2?: string;
        city: string;
        state: string;
        pinCode: string;
    };
}

export type OrderStatus =
    | "PENDING_PAYMENT"
    | "CONFIRMED"
    | "PROCESSING"
    | "SHIPPED"
    | "DELIVERED"
    | "CANCELLED"
    | "REFUNDED";

// ─── Endpoints ==>

export const ordersApi = {

    placeOrder: (data: PlaceOrderRequest) =>
        apiPost<PlaceOrderResponse>("/orders", data),

    getMyOrders: (params: { page?: number; size?: number } = {}) =>
        apiGet<PageResponse<OrderCard>>("/orders", { params }),

    getOrderById: (orderId: string) =>
        apiGet<Order>(`/orders/${orderId}`),

    cancelOrder: (orderId: string, reason?: string) =>
        apiPost<Order>(`/orders/${orderId}/cancel`, { reason }),

} as const;