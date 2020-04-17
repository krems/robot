package ru.oval.bots;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.oval.accounting.model.model.Account;
import ru.oval.accounting.model.model.Currency;
import ru.oval.accounting.model.model.deals.CashFlow;
import ru.oval.accounting.model.model.deals.Deal;
import ru.oval.accounting.model.repository.AccountRepository;
import ru.oval.accounting.model.repository.DealRepository;
import ru.oval.accounting.viewmodel.Session;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ru.oval.util.Utils.isBlank;
import static ru.oval.util.Utils.isEmpty;

@Log4j2
public class AccountingBot extends TelegramLongPollingBot {
    private static final String LIST_ACCOUNTS = "/accounts";
    private static final String LIST_DEALS = "/deals";
    private static final String ADD_ACCOUNT = "/add_account";
    private static final String HELP = "/help";
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
                handleIncomingMessage(message);
            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                handleCallbackQuery(callbackQuery);
            } else {
                log.warn("Unknown update: {}", update);
            }
        } catch (Throwable e) {
            log.error("Fatal error handling update: {}", update, e);
            throw e;
        }
    }

    private void handleIncomingMessage(final Message message) {
        try {
            if (message.hasText()) {
                doHandleIncomingMessage(message);
            } else {
                defaultResponse(message.getChatId());
            }
        } catch (Exception e) {
            log.error("Exception handling message: {}", message, e);
            defaultResponse(message.getChatId());
        }
    }

    private void handleCallbackQuery(final CallbackQuery callbackQuery) {
        try {
            final String dealId = callbackQuery.getData();
            log.info("Cancelling {}", dealId);
            DealRepository.remove(callbackQuery.getFrom().getId(), Long.parseLong(dealId));
            answerCallbackQuery(callbackQuery.getId(), "Cancelled");
        } catch (Exception e) {
            log.error("Error handling callback query: {}", callbackQuery, e);
        }
    }


    private static final Pattern notionalPattern = Pattern.compile("([+-]?)(\\d+?\\.?\\d*?)([a-zA-Z]*)");

    private void doHandleIncomingMessage(final Message message) {
        final Integer userId = message.getFrom().getId();

        final String text = message.getText();
        final List<String> words = Arrays.stream(text.split("\\s"))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(toList());
        if (words.contains("accounts") || words.contains("аккаунты")) {
            final String reply = AccountRepository.findAll(userId)
                    .stream()
                    .map(a -> a.name)
                    .collect(joining("\n"));
            plainTextReply(message.getChatId(), reply.isBlank() ? "No accounts yet" : reply);
            return;
        }
        if (words.contains("deals") || words.contains("сделки")) {
            final String reply = DealRepository.findAll(userId)
                    .stream()
                    .map(Deal::toString)
                    .collect(joining("\n"));
            plainTextReply(message.getChatId(), reply.isBlank() ? "Nothing yet" : reply);
            return;
        }

        if ("to".equals(words.get(0))) {
            words.remove(0);
        }

        final String toAccountName = words.get(0);
        final Optional<Account> toAccount = AccountRepository.findByName(userId, toAccountName);
        if (toAccount.isEmpty()) {
            log.warn("Couldn't find account {} for user {}", toAccountName, userId);
            plainTextReply(message.getChatId(), "First word should be the account name to which you transfer, " +
                    "not " + toAccountName);
            return;
        }
        words.remove(0);
        final Account accountTo = toAccount.get();

        final int fromIndex = words.indexOf("from");
        if (fromIndex != -1) {
            if (fromIndex + 1 >= words.size()) {
                plainTextReply(message.getChatId(), "Didn't get from which account");
                return;
            }
            final String fromAccountName = words.get(fromIndex + 1);
            final Optional<Account> fromAccount = AccountRepository.findByName(userId, fromAccountName);
            if (fromAccount.isEmpty()) {
                log.warn("Couldn't find account {} for user {}", fromAccountName, userId);
                plainTextReply(message.getChatId(), "Didn't get from which account");
                return;
            }
            words.remove(fromIndex + 1);
            words.remove(fromIndex);
            final var cashFlow = CashFlow.builder()
                    .accountTo(accountTo)
                    .accountFrom(fromAccount.get());
            for (final String word : words) {
                final Matcher matcher = notionalPattern.matcher(word);
                if (matcher.matches()) {
                    final String sign = matcher.group(1);
                    final int signum = isEmpty(sign) ? 1 : ("-".equals(sign) ? -1 : 1);
                    final double number = Double.parseDouble(matcher.group(2));
                    final BigDecimal notional = BigDecimal.valueOf(signum * number);
                    cashFlow.notional(notional);
                    final String currencyString = matcher.group(3);
                    final Optional<Currency> currency = Currency.find(currencyString);
                    if (currency.isEmpty()) {
                        log.warn("Couldn't find currency {}", currencyString);
                        plainTextReply(message.getChatId(), "I don't know such currency: " + currencyString);
                        return;
                    }
                    cashFlow.currency(currency.get());
                    final CashFlow deal = cashFlow.build();
                    DealRepository.save(userId, deal);
                    replyWithCallBack(message.getChatId(), deal);
                }
            }
        }
    }

    private void replyWithCallBack(final Long chatId, final CashFlow deal) {
        final SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(deal.toString());
        addCancelCallback(sendMessage, deal);
        doExecute(sendMessage);
    }

    private void plainTextReply(final Long chatId, final String reply) {
        final SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(reply);
        doExecute(sendMessage);
    }

    private void defaultResponse(final Long chatId) {
        plainTextReply(chatId, "Sorry, I didn't understand that");
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

    private void addCancelCallback(final SendMessage sendMessage, final Deal deal) {
        final List<List<InlineKeyboardButton>> buttons = List.of((List.of(new InlineKeyboardButton()
                .setText("Cancel")
                .setCallbackData("" + deal.id))));
        final InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);
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
