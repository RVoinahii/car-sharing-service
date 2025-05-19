package com.carshare.rentalsystem.client.telegram.message.template;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplateDispatcher {
    private final Map<MessageType, MessageTemplateBuilder<?>> buildersMap;

    public MessageTemplateDispatcher(List<MessageTemplateBuilder<?>> builderList) {
        this.buildersMap = builderList.stream()
                .collect(Collectors.toMap(MessageTemplateBuilder::getMessageType, b -> b));
    }

    @SuppressWarnings("unchecked")
    public <T> String createMessage(MessageType messageType, MessageRecipient recipient,
                                    T context) {
        MessageTemplateBuilder<?> rawBuilder = buildersMap.get(messageType);

        if (rawBuilder == null) {
            throw new IllegalArgumentException("No builder found for message type: "
                    + messageType.name());
        }

        Class<?> expectedType = rawBuilder.getSupportedType();
        if (!expectedType.isInstance(context)) {
            throw new IllegalArgumentException(
                    "Expected context of type " + expectedType.getSimpleName()
                            + ", but got " + context.getClass().getSimpleName()
            );
        }

        MessageTemplateBuilder<T> builder = (MessageTemplateBuilder<T>) rawBuilder;

        return builder.createMessage(recipient, context);
    }
}
