package com.carshare.rentalsystem.service.review.rental;

import com.carshare.rentalsystem.dto.review.rental.request.ReviewSearchParameters;
import com.carshare.rentalsystem.dto.review.rental.response.RentalReviewResponseDto;
import com.carshare.rentalsystem.dto.review.rental.response.ReviewPreviewResponseDto;
import com.carshare.rentalsystem.exception.DuplicateReviewException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.RentalNotFinishedException;
import com.carshare.rentalsystem.exception.ReviewAccessDeniedException;
import com.carshare.rentalsystem.mapper.RentalReviewMapper;
import com.carshare.rentalsystem.mapper.search.ReviewSearchParametersMapper;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.model.RentalReviewMedia;
import com.carshare.rentalsystem.repository.SpecificationBuilderFactory;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import com.carshare.rentalsystem.repository.review.rental.RentalReviewRepository;
import com.carshare.rentalsystem.service.aws.s3.S3StorageService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class RentalReviewServiceImpl implements RentalReviewService {
    private final S3StorageService s3StorageService;
    private final RentalRepository rentalRepository;
    private final RentalReviewRepository rentalReviewRepository;
    private final RentalReviewMapper rentalReviewMapper;
    private final ReviewSearchParametersMapper searchParametersMapper;
    private final SpecificationBuilderFactory specificationBuilderFactory;

    @Transactional
    @Override
    public ReviewPreviewResponseDto uploadReview(Long userId, Long rentalId, String impression,
                                                 List<MultipartFile> files, String comment) {
        if (!isValidImpression(impression)) {
            throw new IllegalStateException("Invalid overall impression state, can be only:"
                    + " EXCELLENT, GOOD, NEUTRAL, BAD, TERRIBLE");
        }

        Rental rental = rentalRepository.findByIdAndUserIdWithCarAndUser(rentalId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        if (rental.getStatus() != Rental.RentalStatus.COMPLETED
                && rental.getStatus() != Rental.RentalStatus.CANCELLED) {
            throw new RentalNotFinishedException(
                    "Only completed or cancelled rentals can be reviewed");
        }

        if (rentalReviewRepository.findByRentalAndUser(rental, rental.getUser()).isPresent()) {
            throw new DuplicateReviewException("A review for this rental already exists");
        }

        RentalReview rentalReview = createNewRentalReview(rental);
        rentalReview.setOverallImpression(RentalReview.OverallImpression.valueOf(impression));
        rentalReview.setComment(comment);

        if (files != null && !files.isEmpty()) {
            List<String> s3Keys = s3StorageService.uploadMedia(rentalId, files);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String s3Key = s3Keys.get(i);

                RentalReviewMedia media = createNewReviewMedia(rentalReview, s3Key, file);

                rentalReview.getMediaFiles().add(media);
            }
        }

        RentalReview savedReview = rentalReviewRepository.save(rentalReview);
        return rentalReviewMapper.toPreviewDto(savedReview);
    }

    @Override
    public Page<ReviewPreviewResponseDto> getSpecificReviews(
            ReviewSearchParameters searchParameters, Pageable pageable) {
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<RentalReview> reviewSpecification = specificationBuilderFactory
                .getBuilder(RentalReview.class).build(filters);

        return rentalReviewRepository.findAll(reviewSpecification, pageable)
                .map(rentalReviewMapper::toPreviewDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RentalReviewResponseDto getReviewById(Long reviewId) {
        RentalReview rentalReview = findReviewById(reviewId);

        RentalReviewResponseDto responseDto = rentalReviewMapper.toDto(rentalReview);

        List<String> s3Keys = rentalReview.getMediaFiles().stream()
                .map(RentalReviewMedia::getS3Key)
                .toList();

        responseDto.setMedia(s3StorageService.getMedia(s3Keys));

        return responseDto;
    }

    @Override
    public void deleteById(Long userId, Long reviewId) {
        RentalReview rentalReview = findReviewById(reviewId);

        boolean isManager = rentalReview.getRental().getUser().isManager();

        if (!isManager && !rentalReview.getRental().getUser().getId().equals(userId)) {
            throw new ReviewAccessDeniedException(
                    "You do not have permission to delete this review");
        }

        List<String> keys = rentalReview.getMediaFiles().stream()
                .map(RentalReviewMedia::getS3Key)
                .toList();

        s3StorageService.deleteMedia(keys);
    }

    private boolean isValidImpression(String input) {
        return Arrays.stream(RentalReview.OverallImpression.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(input));
    }

    private RentalReview createNewRentalReview(Rental rental) {
        RentalReview rentalReview = new RentalReview();
        rentalReview.setRental(rental);
        rentalReview.setUser(rental.getUser());
        rentalReview.setCreatedAt(LocalDateTime.now());

        return rentalReview;
    }

    private RentalReviewMedia createNewReviewMedia(RentalReview rentalReview, String s3Key,
                                                   MultipartFile file) {
        RentalReviewMedia media = new RentalReviewMedia();
        media.setRentalReview(rentalReview);
        media.setS3Key(s3Key);

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            media.setMediaType(RentalReviewMedia.MediaType.IMAGE);
        } else if (contentType != null && contentType.startsWith("video/")) {
            media.setMediaType(RentalReviewMedia.MediaType.VIDEO);
        } else {
            media.setMediaType(RentalReviewMedia.MediaType.IMAGE);
        }
        return media;
    }

    private RentalReview findReviewById(Long reviewId) {
        return rentalReviewRepository.findById(reviewId).orElseThrow(
                () -> new EntityNotFoundException("Can't find review with ID: " + reviewId)
        );
    }

}
