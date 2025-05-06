package com.carshare.rentalsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "telegram_user_links")
public class TelegramUserLink {
    @Id
    private Long userId;

    @Column(nullable = false)
    private Long chatId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public TelegramUserLink() {
    }

    public TelegramUserLink(User user, Long chatId) {
        this.user = user;
        this.chatId = chatId;
    }
}
