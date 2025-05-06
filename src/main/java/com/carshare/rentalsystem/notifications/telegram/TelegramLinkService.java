package com.carshare.rentalsystem.notifications.telegram;

import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.telegram.user.link.TelegramUserLinkRepository;
import com.carshare.rentalsystem.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TelegramLinkService {
    private final UserRepository userRepository;
    private final TelegramUserLinkRepository telegramUserLinkRepository;

    @Transactional
    public TelegramUserLink linkUser(Long userId, Long chatId) {
        User user = userRepository.findByIdWithRole(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user.")
        );
        TelegramUserLink telegramUserLink = new TelegramUserLink(user, chatId);
        telegramUserLinkRepository.save(telegramUserLink);
        return telegramUserLink;
    }
}
