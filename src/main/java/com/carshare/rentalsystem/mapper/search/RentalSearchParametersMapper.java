package com.carshare.rentalsystem.mapper.search;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import java.util.HashMap;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface RentalSearchParametersMapper {

    @Named("toMap")
    default Map<String, String> toMap(RentalSearchParameters parameters) {
        Map<String, String> map = new HashMap<>();

        if (parameters.userId() != null && !parameters.userId().isEmpty()) {
            map.put("userId", parameters.userId());
        }

        if (parameters.status() != null) {
            map.put("status", parameters.status().name());
        }

        return map;
    }
}
