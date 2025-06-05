package com.carshare.rentalsystem.client.telegram.command.handler.rental.command;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetRentalCommandHandler.COMMAND_ARGUMENT_DELIMITER_REGEX;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetAllRentalsCommandHandler implements TelegramCommandHandler {
    public static final int MAX_ARGUMENTS_FOR_REGULAR_USER = 1;
    public static final int START_PAGE_INDEX = 0;
    public static final int PAGE_SIZE = 1;
    public static final int PAGE_INDEX_OFFSET = 1;
    public static final String RENTALS_PAGE_CALLBACK_PREFIX = "rentals_page:";

    private static final String GET_ALL_RENTALS_COMMAND = "/get_all_rentals";

    private final RentalService rentalService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final MessageTemplateDispatcher templateDispatcher;

    @Override
    public String getCommand() {
        return GET_ALL_RENTALS_COMMAND;
    }

    @Override
    public void handle(TelegramBot bot, Message message) {
        Long chatId = message.chat().id();
        TelegramUserLink telegramUserLink = telegramUserStorage.findByChatId(chatId).orElse(null);

        if (telegramUserLink == null) {
            bot.execute(new SendMessage(chatId,
                    "❗ Your Telegram account is not linked to any user in the system. "
                            + "Please register or contact support."));
            return;
        }

        User user = telegramUserLink.getUser();

        String[] searchFilters = message.text().split(COMMAND_ARGUMENT_DELIMITER_REGEX);

        if (!user.isManager() && searchFilters.length > MAX_ARGUMENTS_FOR_REGULAR_USER) {
            bot.execute(new SendMessage(chatId,
                    "❗ Sorry, filtering rentals is currently available to managers only."));
            return;
        }

        Page<RentalPreviewResponseDto> page;
        RentalSearchParameters searchParameters = null;

        if (user.isManager()) {
            searchParameters = parseFilters(searchFilters);
            page = rentalService.getSpecificRentals(
                    searchParameters,
                    PageRequest.of(START_PAGE_INDEX, PAGE_SIZE)
            );
        } else {
            page = rentalService.getRentalsById(
                    telegramUserLink.getUserId(),
                    PageRequest.of(START_PAGE_INDEX, PAGE_SIZE)
            );
        }

        if (page.getContent().isEmpty()) {
            bot.execute(new SendMessage(chatId, "❗ No rentals found for your request."));
            return;
        }

        String responseMessage = templateDispatcher.createMessage(
                MessageType.RENTAL_LIST_MSG,
                user.isManager()
                        ? MessageRecipient.RECIPIENT_MANAGER
                        : MessageRecipient.RECIPIENT_CUSTOMER,
                page);

        SendMessage sendMessage = new SendMessage(chatId, responseMessage)
                .replyMarkup(PaginationKeyboardBuilder.create(
                page.getNumber() + PAGE_INDEX_OFFSET,
                page.getTotalPages(),
                RENTALS_PAGE_CALLBACK_PREFIX,
                filterNulls(
                        searchParameters.userId(),
                        searchParameters.status() != null
                                ? searchParameters.status().name()
                                : null)
                ));
        bot.execute(sendMessage);
    }

    private RentalSearchParameters parseFilters(String[] searchFilters) {
        String userIdFilter = null;
        Rental.RentalStatus statusFilter = null;

        for (int i = 1; i < searchFilters.length; i++) {
            String token = searchFilters[i].trim();
            if (isStatusValue(token)) {
                statusFilter = Rental.RentalStatus.valueOf(token.toUpperCase());
            } else {
                userIdFilter = token;
            }
        }
        return new RentalSearchParameters(userIdFilter, statusFilter);
    }

    private boolean isStatusValue(String token) {
        return Arrays.stream(Rental.RentalStatus.values())
                .anyMatch(status -> status.name().equalsIgnoreCase(token));
    }

    private String[] filterNulls(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }
}
