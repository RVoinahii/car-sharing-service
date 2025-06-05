package com.carshare.rentalsystem.dto.review.rental.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReviewPreviewResponseDto {
    private Long id;
    private String overallImpression;
    private Long carId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}
