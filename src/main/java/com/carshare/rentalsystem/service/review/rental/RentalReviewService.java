package com.carshare.rentalsystem.service.review.rental;

import com.carshare.rentalsystem.dto.review.rental.request.ReviewSearchParameters;
import com.carshare.rentalsystem.dto.review.rental.response.RentalReviewResponseDto;
import com.carshare.rentalsystem.dto.review.rental.response.ReviewPreviewResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface RentalReviewService {
    ReviewPreviewResponseDto uploadReview(Long userId, Long rentalId, String impression,
                                          List<MultipartFile> files, String comment);

    Page<ReviewPreviewResponseDto> getSpecificReviews(ReviewSearchParameters params,
                                                      Pageable pageable);

    RentalReviewResponseDto getReviewById(Long reviewId);

    void deleteById(Long userId, Long reviewId);
}
