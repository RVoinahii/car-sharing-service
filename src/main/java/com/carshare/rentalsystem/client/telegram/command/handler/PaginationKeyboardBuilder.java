package com.carshare.rentalsystem.client.telegram.command.handler;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.util.ArrayList;
import java.util.List;

public class PaginationKeyboardBuilder {
    private static final int MIN_PAGE_NUMBER = 1;
    private static final int PAGE_STEP = 1;
    private static final String PREVIOUS_BUTTON_TEXT = "⬅️ Previous";
    private static final String NEXT_BUTTON_TEXT = "➡️ Next";

    public static InlineKeyboardMarkup create(int currentPage, int totalPages, String prefix) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (currentPage > MIN_PAGE_NUMBER) {
            row.add(new InlineKeyboardButton(PREVIOUS_BUTTON_TEXT)
                    .callbackData(prefix + (currentPage - PAGE_STEP)));
        }

        if (currentPage < totalPages) {
            row.add(new InlineKeyboardButton(NEXT_BUTTON_TEXT)
                    .callbackData(prefix + (currentPage + PAGE_STEP)));
        }

        return new InlineKeyboardMarkup(row.toArray(new InlineKeyboardButton[0]));
    }
}
