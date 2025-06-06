package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.review.rental.request.ReviewSearchParameters;
import com.carshare.rentalsystem.dto.review.rental.response.RentalReviewResponseDto;
import com.carshare.rentalsystem.dto.review.rental.response.ReviewPreviewResponseDto;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.review.rental.RentalReviewService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(
        name = "Rental review management",
        description = """
        Endpoints for creating, retrieving, and deleting rental reviews.

        - Customers can leave reviews for **completed** or **cancelled** rentals only.
        - One review per rental is allowed.
        - Media files (images or videos) are uploaded to S3 and served as pre-signed URLs.
        - Managers have full access to all reviews.
            """
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class RentalReviewController {
    private final RentalReviewService rentalReviewService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping(value = "/{rentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a rental review",
            description = """
            Allows a customer to upload a review for a specific rental.

            Requirements:
            - Rental must have status 'COMPLETED' or 'CANCELLED'
            - Only one review per rental is allowed
            - Allowed file types: image/jpeg, image/png, video/mp4
            - Max file size per file: 50 MB

            Uploaded media is stored in S3. Pre-signed URLs are generated for access.

            **Required roles**: CUSTOMER, MANAGER
            """
    )
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
    @Operation(
            summary = "Get all rental reviews (paginated)",
            description = """
            Returns a paginated list of rental reviews.

            - Managers can view all reviews
            - Customers typically see public reviews for cars (implementation-specific)
            - Supports filtering by:
              - carId
              - model
              - brand
              - car type (e.g. 'SEDAN', 'SUV')

            **Required roles**: CUSTOMER, MANAGER
            """
    )
    public Page<ReviewPreviewResponseDto> getAllReviews(Authentication authentication,
            ReviewSearchParameters searchParameters, Pageable pageable) {

        return rentalReviewService.getSpecificReviews(searchParameters, pageable);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/{reviewId}")
    @Operation(
            summary = "Get full review details by ID",
            description = """
            Returns full details of a review including pre-signed media URLs.

            - Media URLs are time-limited (10 minutes)
            - Useful for displaying review details with associated images or videos

            **Required roles**: CUSTOMER, MANAGER
            """
    )
    public RentalReviewResponseDto getReviewById(@PathVariable Long reviewId) {
        return rentalReviewService.getReviewById(reviewId);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "Delete a review by ID",
            description = """
            Deletes a review and its media from the system.

            - Customers can delete **only their own** reviews
            - Managers can delete **any** review
            - Media files are permanently removed from S3

            **Required roles**: CUSTOMER, MANAGER
            """
    )
    public void deleteReviewById(Authentication authentication, @PathVariable Long reviewId) {
        rentalReviewService.deleteById(getAuthenticatedUserId(authentication), reviewId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
