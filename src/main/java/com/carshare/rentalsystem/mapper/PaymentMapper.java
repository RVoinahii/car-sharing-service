package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentCancelResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = RentalMapper.class)
public interface PaymentMapper {
    @Mapping(target = "rental", source = "rental", qualifiedByName = "toDto")
    PaymentResponseDto toDto(Payment payment);

    PaymentCancelResponseDto toCancelDto(Payment payment);

    PaymentPreviewResponseDto toPreviewDto(Payment payment);
}
