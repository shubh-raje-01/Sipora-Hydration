import { apiDelete, apiGet, apiPost, type PageResponse } from "./axios";

// ─── Types ==>

export interface Category {
    id: string;
    name: string;
    slug: string;
    description?: string;
    imageUrl?: string;
    displayOrder: number;
    active: boolean;
    parentId?: string;
    children: Category[];
}

export interface ProductCard {
    id: string;
    name: string;
    slug: string;
    shortDescription?: string;
    startingPrice: number;
    currencyCode: string;
    inStock: boolean;
    primaryImageUrl?: string;
    featured: boolean;
    categoryName?: string;
}

export interface ProductVariant {
    id: string;
    sku: string;
    displayName: string;
    price: number;
    currencyCode: string;
    stockQty: number;
    lowStockThreshold: number;
    inStock: boolean;
    lowStock: boolean;
    color?: string;
    size?: string;
    flavor?: string;
    quantityCount?: number;
    active: boolean;
    displayOrder: number;
}

export interface Product {
    id: string;
    name: string;
    slug: string;
    description?: string;
    shortDescription?: string;
    metaTitle?: string;
    metaDescription?: string;
    status: string;
    featured: boolean;
    displayOrder: number;
    startingPrice: number;
    currencyCode: string;
    inStock: boolean;
    totalStock: number;
    images: string[];
    category: Category;
    variants: ProductVariant[];
    createdAt: string;
    updatedAt: string;
}

export interface Review {
    reviewId: string;
    productId: string;
    userId: string;
    userName: string;
    rating: number;
    title?: string;
    body: string;
    verifiedPurchase: boolean;
    helpfulVotes: number;
    hidden: boolean;
    createdAt: string;
}

export interface ReviewStats {
    averageRating: number;
    totalReviews: number;
    distribution: Record<string, number>;
}

export interface PostReviewRequest {
    rating: number;
    title?: string;
    body: string;
}

// ─── Query param types ==>

export interface ProductListParams {
    page?: number;
    size?: number;
    categoryId?: string;
    minPrice?: number;
    maxPrice?: number;
    inStock?: boolean;
    search?: string;
    sortBy?: string;
    sortDir?: "asc" | "desc";
}

// ─── Endpoints ==>

export const catalogApi = {

    // ── Categories

    getCategories: () =>
        apiGet<Category[]>("/categories"),

    getCategoryBySlug: (slug: string) =>
        apiGet<Category>(`/categories/${slug}`),

    // ── Products

    listProducts: (params: ProductListParams = {}) =>
        apiGet<PageResponse<ProductCard>>("/products", { params }),

    getFeaturedProducts: () =>
        apiGet<ProductCard[]>("/products/featured"),

    getProductBySlug: (slug: string) =>
        apiGet<Product>(`/products/${slug}`),

    // ── Reviews

    getProductReviews: (
        productId: string,
        params: { page?: number; size?: number; sortBy?: string; sortDir?: string } = {}
    ) =>
        apiGet<PageResponse<Review>>(`/products/${productId}/reviews`, { params }),

    getReviewStats: (productId: string) =>
        apiGet<ReviewStats>(`/products/${productId}/reviews/stats`),

    postReview: (productId: string, data: PostReviewRequest) =>
        apiPost<Review>(`/products/${productId}/reviews`, data),

    deleteReview: (productId: string, reviewId: string) =>
        apiDelete<void>(`/products/${productId}/reviews/${reviewId}`),

    markReviewHelpful: (productId: string, reviewId: string) =>
        apiPost<Review>(`/products/${productId}/reviews/${reviewId}/helpful`),

} as const;