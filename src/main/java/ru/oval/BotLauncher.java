package ru.oval;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import ru.oval.handlers.AccountingBot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotLauncher {
    private static final List<BotSession> sessions = new CopyOnWriteArrayList<>();

    public static void main(final String[] args) {
        try {
            setUp();
            final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            addSession(telegramBotsApi.registerBot(new AccountingBot()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void setUp() {
        ApiContextInitializer.init();
        registerShutdownHook();
    }

    private static void addSession(final BotSession session) {
        sessions.add(session);
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (final BotSession session : sessions) {
                session.stop();
            }
        }));
    }
}
