package ru.oval;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import ru.oval.bots.AccountingBot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
public class BotLauncher {
    private static final List<BotSession> sessions = new CopyOnWriteArrayList<>();

    public static void main(final String[] args) {
        try {
            log.info("Started with params: {}", Arrays.toString(args));
            setUp();
            log.debug("Setup done");
            final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            log.debug("API created");
            addSession(telegramBotsApi.registerBot(new AccountingBot()));
            log.info("Started");
        } catch (TelegramApiException e) {
            log.error("Error starting bots", e);
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
                log.debug("Stopping session {}", session);
//                session.stop();
                log.debug("Session stopped");
            }
        }));
    }
}
