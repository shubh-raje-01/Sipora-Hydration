import { apiGet, apiPost } from "./axios";

// ─── Types ==>

export interface CreatePaymentOrderResponse {
    paymentRecordId: string;
    orderId: string;
    orderNumber: string;
    razorpayOrderId: string;
    razorpayKeyId: string;
    amount: number;
    amountInPaise: number;
    currency: string;
    description: string;
}

export interface VerifyPaymentRequest {
    razorpay_order_id: string;
    razorpay_payment_id: string;
    razorpay_signature: string;
}

export interface VerifyPaymentResponse {
    success: boolean;
    orderId: string;
    orderNumber: string;
    status: string;
    amount: number;
    currencyCode: string;
    capturedAt?: string;
    message: string;
}

export interface PaymentRecord {
    paymentId: string;
    orderId: string;
    status: "PENDING" | "CAPTURED" | "FAILED" | "REFUNDED";
    amount: number;
    currencyCode: string;
    gatewayOrderId: string;
    gatewayPaymentId?: string;
    failureReason?: string;
    capturedAt?: string;
    failedAt?: string;
    createdAt: string;
}

// ─── Razorpay global type declaration ───────────────────────────────
// The Razorpay checkout.js SDK is loaded via script tag in index.html.
// Declaring it here allows TypeScript to recognise window.Razorpay.

declare global {
    interface Window {
        Razorpay: new (options: RazorpayOptions) => RazorpayInstance;
    }
}

export interface RazorpayOptions {
    key: string;
    amount: number;
    currency: string;
    order_id: string;
    name?: string;
    description?: string;
    image?: string;
    prefill?: {
        name?: string;
        email?: string;
        contact?: string;
    };
    theme?: {
        color?: string;
    };
    handler: (response: RazorpayCallbackResponse) => void;
    modal?: {
        ondismiss?: () => void;
    };
}

export interface RazorpayCallbackResponse {
    razorpay_order_id: string;
    razorpay_payment_id: string;
    razorpay_signature: string;
}

export interface RazorpayInstance {
    open:  () => void;
    close: () => void;
}

// ─── Endpoints ==>

export const paymentsApi = {

    // Step 1 — create a Razorpay order after placing our internal order
    createPaymentOrder: (orderId: string) =>
        apiPost<CreatePaymentOrderResponse>("/payments/create-order", { orderId }),

    // Step 2 — verify the signature from the Razorpay checkout callback
    verifyPayment: (data: VerifyPaymentRequest) =>
        apiPost<VerifyPaymentResponse>("/payments/verify", data),

    // Get payment history for an order (own orders only)
    getPaymentsForOrder: (orderId: string) =>
        apiGet<PaymentRecord[]>(`/payments/orders/${orderId}`),

} as const;