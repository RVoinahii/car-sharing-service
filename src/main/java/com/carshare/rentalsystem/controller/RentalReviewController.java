package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.review.rental.request.ReviewSearchParameters;
import com.carshare.rentalsystem.dto.review.rental.response.RentalReviewResponseDto;
import com.carshare.rentalsystem.dto.review.rental.response.ReviewPreviewResponseDto;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.review.rental.RentalReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Rental review management", description = "Endpoints for managing rental reviews")
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class RentalReviewController {
    private final RentalReviewService rentalReviewService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping(value = "/{rentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReviewPreviewResponseDto uploadReview(Authentication authentication,
            @PathVariable Long rentId,
            @RequestPart(value = "overallImpression") String impression,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "comment", required = false) String comment) {
        return rentalReviewService.uploadReview(getAuthenticatedUserId(authentication),
                rentId, impression, files, comment);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping
    public Page<ReviewPreviewResponseDto> getAllReviews(Authentication authentication,
            ReviewSearchParameters searchParameters, Pageable pageable) {

        return rentalReviewService.getSpecificReviews(searchParameters, pageable);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/{reviewId}")
    public RentalReviewResponseDto getReviewById(@PathVariable Long reviewId) {
        return rentalReviewService.getReviewById(reviewId);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @DeleteMapping("/{reviewId}")
    public void deleteReviewById(Authentication authentication, @PathVariable Long reviewId) {
        rentalReviewService.deleteById(getAuthenticatedUserId(authentication), reviewId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
