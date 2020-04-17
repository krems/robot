package ru.oval.bots;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.util.WebhookUtils;
import ru.oval.accounting.viewmodel.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.oval.util.Utils.isBlank;

@Log4j2
public class AccountingBot extends TelegramLongPollingBot {
    private static final String CANCEL = "/cancel";
    private static final DefaultBotOptions options = new DefaultBotOptions();
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    public AccountingBot() {
        super(options);
    }

    @Override
    public void onUpdateReceived(final Update update) {
        try {
            if (update.hasMessage()) {
                final Message message = update.getMessage();
                if (message.hasText()) {
                    handleIncomingMessage(message);
                } else {
                    defaultResponse(message);
                }
            } else if (update.hasCallbackQuery()) {
                log.info("{}", update.getCallbackQuery().getData());
                answerCallbackQuery(update.getCallbackQuery().getId(), "ok");
            } else {
                log.warn("Unknown update: {}", update);
            }
        } catch (Exception e) {
            log.error("Error handling update: {}", update, e);
        }
    }

    private void handleIncomingMessage(final Message message) {
        final Integer userId = message.getFrom().getId();
        final Session session = sessions.compute(userId, (id, curSession) -> {
            if (curSession == null || CANCEL.equals(message.getText())) {
                return new Session(id);
            }
            return curSession;
        });
        final SendMessage response = session.onMessage(message);
        doExecute(response);
    }

    private void defaultResponse(final Message message) {
        final SendMessage sendMessage = new SendMessage()
                .setChatId(message.getChatId())
                .setText("Sorry, I didn't understand that");
        doExecute(sendMessage);
    }

    public synchronized void answerCallbackQuery(String callbackId, String message) {
        if (message.length() > 200) {
            throw new IllegalArgumentException("Too long response: " + message);
        }
        final AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);
        answer.setShowAlert(false);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setInline(final SendMessage sendMessage) {
        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        final List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        buttons1.add(new InlineKeyboardButton().setText("Buy").setCallbackData("/buy"));
        buttons1.add(new InlineKeyboardButton().setText("Sell").setCallbackData("/sell"));
        buttons.add(buttons1);

        final InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        sendMessage.setReplyMarkup(markupKeyboard);
    }

    private void doExecute(final BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.error("Error executing method: {}", method.getMethod(), e);
        }
    }

    @Override
    public String getBotUsername() {
        final String name = System.getenv("ACCOUNTING_BOT_USERNAME");
        if (isBlank(name)) {
            log.error("Bot name is empty: {}", name);
            throw new IllegalStateException("Bot username is not provided");
        }
        return name;
    }

    @Override
    public String getBotToken() {
        final String token = System.getenv("ACCOUNTING_BOT_TOKEN");
        if (isBlank(token)) {
            log.error("token: {}", token);
            throw new IllegalStateException("Bot token is not provided");
        }
        return token;
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {
        // do nothing, no fucking webhook
    }
}
