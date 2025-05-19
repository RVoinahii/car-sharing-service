package com.carshare.rentalsystem.client.telegram;

import com.carshare.rentalsystem.model.TelegramUserLink;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ActiveTelegramUserStorage {
    private final Map<Long, TelegramUserLink> chatIdToUserLink = new ConcurrentHashMap<>();

    public void add(TelegramUserLink link) {
        chatIdToUserLink.put(link.getChatId(), link);
    }

    public boolean contains(Long chatId) {
        return chatIdToUserLink.containsKey(chatId);
    }

    public Optional<TelegramUserLink> findByChatId(Long chatId) {
        return Optional.ofNullable(chatIdToUserLink.get(chatId));
    }

    public Collection<TelegramUserLink> getAll() {
        return Collections.unmodifiableCollection(chatIdToUserLink.values());
    }

    public void refreshAll(List<TelegramUserLink> links) {
        chatIdToUserLink.clear();
        for (TelegramUserLink link : links) {
            chatIdToUserLink.put(link.getChatId(), link);
        }
    }
}
