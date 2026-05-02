package in.sipora.backend.modules.review.application;

import in.sipora.backend.modules.review.domain.Review;
import in.sipora.backend.modules.review.web.ReviewDTOs.ReviewResponse;

import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getProductId(),
                r.getUserId(),
                r.getUserName(),
                r.getRating(),
                r.getTitle(),
                r.getBody(),
                r.isVerifiedPurchase(),
                r.getHelpfulVotes(),
                r.isHidden(),
                r.getCreatedAt()
        );
    }
}