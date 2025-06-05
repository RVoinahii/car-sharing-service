package com.carshare.rentalsystem.dto.review.rental.response;

import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RentalReviewResponseDto {
    private Long id;
    private RentalPreviewResponseDto rentalPreview;
    private String overallImpression;
    private String comment;
    private List<String> media;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}
