package com.carshare.rentalsystem.mapper.search;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.car.request.dto.CarSearchParameters;
import java.util.HashMap;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface CarSearchParametersMapper {
    @Named("toMap")
    default Map<String, String> toMap(CarSearchParameters parameters) {
        Map<String, String> map = new HashMap<>();

        if (parameters.model() != null && !parameters.model().isEmpty()) {
            map.put("model", parameters.model());
        }

        if (parameters.brand() != null && !parameters.brand().isEmpty()) {
            map.put("brand", parameters.brand());
        }

        if (parameters.type() != null) {
            map.put("type", parameters.type().name());
        }

        if (parameters.priceRange() != null && !parameters.priceRange().isEmpty()) {
            map.put("priceRange", parameters.priceRange());
        }

        if (parameters.onlyAvailable() != null) {
            map.put("onlyAvailable", parameters.onlyAvailable().toString());
        }

        return map;
    }
}
