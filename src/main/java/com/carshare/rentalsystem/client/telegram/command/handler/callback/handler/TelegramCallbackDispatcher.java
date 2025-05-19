package com.carshare.rentalsystem.client.telegram.command.handler.callback.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TelegramCallbackDispatcher {
    private final Map<String, TelegramCallbackHandler> handlers;

    public TelegramCallbackDispatcher(List<TelegramCallbackHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(TelegramCallbackHandler::getCallbackData,
                        handler -> handler));
    }

    public TelegramCallbackHandler getCallbackHandler(String key) {
        return handlers.get(key);
    }
}
