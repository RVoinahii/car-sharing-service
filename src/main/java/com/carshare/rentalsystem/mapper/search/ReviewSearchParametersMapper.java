package com.carshare.rentalsystem.mapper.search;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.review.rental.request.ReviewSearchParameters;
import java.util.HashMap;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface ReviewSearchParametersMapper {
    default Map<String, String> toMap(ReviewSearchParameters parameters) {
        Map<String, String> map = new HashMap<>();

        if (parameters.carId() != null && !parameters.carId().isEmpty()) {
            map.put("userId", parameters.carId());
        }

        if (parameters.model() != null && !parameters.model().isEmpty()) {
            map.put("model", parameters.model());
        }

        if (parameters.brand() != null && !parameters.brand().isEmpty()) {
            map.put("brand", parameters.brand());
        }

        if (parameters.type() != null) {
            map.put("type", parameters.type().name());
        }

        return map;
    }
}
