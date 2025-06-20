package com.carshare.rentalsystem.test.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class TestPageImplDeserializerUtil<T> extends StdDeserializer<PageImpl<T>> {
    private static final String CONTENT_FIELD = "content";
    private static final String NUMBER_FIELD = "number";
    private static final String SIZE_FIELD = "size";
    private static final String TOTAL_ELEMENTS_FIELD = "totalElements";

    private final Class<T> clazz;
    private final int defaultSize;

    public TestPageImplDeserializerUtil(Class<T> clazz, int defaultSize) {
        super(PageImpl.class);
        this.clazz = clazz;
        this.defaultSize = defaultSize;
    }

    @Override
    public PageImpl<T> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode root = mapper.readTree(parser);

        List<T> content = new ArrayList<>();
        if (root.has(CONTENT_FIELD) && root.get(CONTENT_FIELD).isArray()) {
            for (JsonNode item : root.get(CONTENT_FIELD)) {
                content.add(mapper.treeToValue(item, clazz));
            }
        }

        int number = root.has(NUMBER_FIELD) ? root.get(NUMBER_FIELD).asInt() : 0;
        int size = root.has(SIZE_FIELD) ? root.get(SIZE_FIELD).asInt() : defaultSize;
        long totalElements = root.has(TOTAL_ELEMENTS_FIELD)
                ? root.get(TOTAL_ELEMENTS_FIELD).asLong()
                : content.size();

        return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
    }
}
