package com.carshare.rentalsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarPreviewDto {
    private Long id;
    private String model;
    private String brand;
    private String type;
}
