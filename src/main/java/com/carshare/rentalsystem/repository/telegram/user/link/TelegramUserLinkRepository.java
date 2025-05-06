package com.carshare.rentalsystem.repository.telegram.user.link;

import com.carshare.rentalsystem.model.TelegramUserLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TelegramUserLinkRepository extends JpaRepository<TelegramUserLink, Long> {
    boolean existsByUserId(Long userId);

    @Query("SELECT tul FROM TelegramUserLink tul JOIN FETCH tul.user u JOIN FETCH u.role")
    List<TelegramUserLink> findAllWithUsersAndRoles();
}
