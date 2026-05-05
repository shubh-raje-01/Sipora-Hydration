import { apiGet } from "./axios";
import type { PageResponse } from "./axios";
import type { Review } from "./catalog.api";

// Re-export Review type for convenience
export type { Review };

// ─── Endpoints ==>
// Note: posting and deleting reviews is done via catalogApi
// (POST /products/:id/reviews, DELETE /products/:id/reviews/:id)
// because they are scoped to a product.
//
// This file handles the user-centric view: "all my reviews" at /users/me/reviews

export const reviewsApi = {

    getMyReviews: (params: { page?: number; size?: number } = {}) =>
        apiGet<PageResponse<Review>>("/users/me/reviews", { params }),

} as const;