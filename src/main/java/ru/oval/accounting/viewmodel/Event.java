package ru.oval.accounting.viewmodel;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public class Event {
    public final Message sendMessage;
    public final CallbackQuery callbackQuery;

    private Event(final Message sendMessage, final CallbackQuery callbackQuery) {
        this.sendMessage = sendMessage;
        this.callbackQuery = callbackQuery;
    }

    public boolean isMessageEvent() {
        return sendMessage != null;
    }

    public boolean isCallbackEvent() {
        return callbackQuery != null;
    }

    public static Event messageEvent(final Message text) {
        return new Event(text, null);
    }

    public static Event callBackEvent(final CallbackQuery callbackQuery) {
        return new Event(null ,callbackQuery);
    }
}
