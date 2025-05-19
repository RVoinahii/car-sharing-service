package com.carshare.rentalsystem.client.telegram.message.template;

public interface MessageTemplateBuilder<T> {
    MessageType getMessageType();

    Class<T> getSupportedType();

    String createMessage(MessageRecipient recipient, T context);
}
