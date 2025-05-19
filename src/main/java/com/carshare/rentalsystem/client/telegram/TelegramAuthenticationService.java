package com.carshare.rentalsystem.client.telegram;

import com.carshare.rentalsystem.dto.telegram.TelegramTokenResponseDto;
import com.carshare.rentalsystem.util.AesEncryptionUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramAuthenticationService {
    private static final String BOT_USERNAME = "car_sharing_alert_bot";
    private static final Duration EXPIRATION_DURATION = Duration.ofMinutes(10);
    private static final String LINK_START = "start=";

    private final Map<String, Instant> validKeys = new ConcurrentHashMap<>();

    public TelegramTokenResponseDto getTelegramLink(Long userId) {
        String secretKey = generateSecretKey(userId);
        validKeys.put(secretKey, Instant.now().plus(EXPIRATION_DURATION));
        String telegramLink = String.format(
                "https://t.me/%s?%s%s", BOT_USERNAME, LINK_START, secretKey
        );

        return new TelegramTokenResponseDto(telegramLink);
    }

    public boolean isLinkParameterValid(String parameter) {
        Instant expirationTime = validKeys.get(parameter);
        if (expirationTime != null && Instant.now().isBefore(expirationTime)) {
            validKeys.remove(parameter);
            return true;
        }
        return false;
    }

    @Scheduled(fixedRate = 600_000)
    public void cleanUpExpiredKeys() {
        Instant now = Instant.now();
        for (Map.Entry<String, Instant> entry : validKeys.entrySet()) {
            String key = entry.getKey();
            Instant expiry = entry.getValue();
            if (expiry.isBefore(now)) {
                validKeys.remove(key);
            }
        }
    }

    private String generateSecretKey(Long userId) {
        return AesEncryptionUtil.encrypt(userId.toString());
    }
}
