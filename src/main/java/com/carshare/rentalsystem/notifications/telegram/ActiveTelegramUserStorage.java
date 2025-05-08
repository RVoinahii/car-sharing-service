package com.carshare.rentalsystem.notifications.telegram;

import com.carshare.rentalsystem.model.TelegramUserLink;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ActiveTelegramUserStorage {
    private final Set<TelegramUserLink> activeUserLinks = ConcurrentHashMap.newKeySet();

    public void add(TelegramUserLink link) {
        activeUserLinks.add(link);
    }

    public boolean contains(TelegramUserLink link) {
        return activeUserLinks.contains(link);
    }

    public Set<TelegramUserLink> getAll() {
        return Collections.unmodifiableSet(activeUserLinks);
    }

    public void refreshAll(List<TelegramUserLink> links) {
        activeUserLinks.clear();
        activeUserLinks.addAll(links);
    }
}
