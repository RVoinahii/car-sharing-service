package com.carshare.rentalsystem.dto.car.response.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarPreviewResponseDto {
    private Long id;
    private String model;
    private String brand;
    private String type;
    private BigDecimal dailyFee;
}
