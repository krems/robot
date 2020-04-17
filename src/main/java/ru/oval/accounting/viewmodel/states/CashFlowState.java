package ru.oval.accounting.viewmodel.states;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.oval.accounting.model.model.Account;
import ru.oval.accounting.model.model.Currency;
import ru.oval.accounting.model.model.deals.CashFlow;
import ru.oval.accounting.model.repository.AccountRepository;
import ru.oval.accounting.model.repository.DealRepository;
import ru.oval.accounting.viewmodel.Event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Double.parseDouble;

@Log4j2
public class CashFlowState implements State {
    private final int userId;
    private final CashFlow.CashFlowBuilder cashFlowBuilder;
    private InnerState state = InnerState.NEW;

    public CashFlowState(final CashFlow.CashFlowBuilder cashFlowBuilder, final int userId) {
        this.cashFlowBuilder = cashFlowBuilder;
        this.userId = userId;
    }

    @Override
    public State next(final Event event) {
        if (!event.isMessageEvent()) {
            return this;
        }
        switch (state) {
            case NEW: {
                final String accName = event.sendMessage.getText();
                final Optional<Account> account = AccountRepository.findByName(userId, accName);
                if (account.isPresent()) {
                    cashFlowBuilder.accountTo(account.get());
                    state = InnerState.ACCOUNT_TO;
                } else {
                    log.warn("Account [to] {} for user {} not found", accName, userId);
                }
                return this;
            }
            case ACCOUNT_TO: {
                final String accName = event.sendMessage.getText();
                final Optional<Account> account = AccountRepository.findByName(userId, accName);
                if (account.isPresent()) {
                    cashFlowBuilder.accountFrom(account.get());
                    state = InnerState.ACCOUNT_FROM;
                } else {
                    log.warn("Account [from] {} for user {} not found", accName, userId);
                }
                return this;
            }
            case ACCOUNT_FROM: {
                try {
                    final BigDecimal notional = BigDecimal.valueOf(parseDouble(event.sendMessage.getText()));
                    cashFlowBuilder.notional(notional);
                    state = InnerState.NOTIONAL;
                } catch (NumberFormatException e) {
                    log.warn("Invalid number: {}", event.sendMessage.getText(), e);
                }
                return this;
            }
            case NOTIONAL: {
                final String ccy = event.sendMessage.getText();
                final Optional<Currency> currency = Currency.find(ccy);
                if (currency.isPresent()) {
                    cashFlowBuilder.currency(currency.get());
                    state = InnerState.CURRENCY;
                } else {
                    log.warn("Currency {} for user {} not found", ccy, userId);
                }
                return this;
            }
            case CURRENCY: {
                DealRepository.save(userId, cashFlowBuilder.build());
                return new NewState(userId);
            }
        }
        return this;
    }

    @Override
    public SendMessage currentMessage(final SendMessage prototype) {
        switch (state) {
            case NEW:
                return prototype.setText("To which account you've sent the money?")
                        .setReplyMarkup(accountsKeyboard());
            case ACCOUNT_TO:
                return prototype.setText("From which account you've sent the money?")
                        .setReplyMarkup(accountsKeyboard());
            case ACCOUNT_FROM:
                return prototype.setText("How much?");
            case NOTIONAL:
                return prototype.setText("Which currency?")
                        .setReplyMarkup(currencyKeyBoard());
        }
        return null;
    }

    private ReplyKeyboard currencyKeyBoard() {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setSelective(true)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);
        final KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(new KeyboardButton("RUB"));
        firstRow.add(new KeyboardButton("USD"));
        firstRow.add(new KeyboardButton("EUR"));
        final List<KeyboardRow> keyboard = List.of(firstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup accountsKeyboard() {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setSelective(true)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);

        final List<KeyboardRow> keyboard = new ArrayList<>();

        final Set<Account> accounts = AccountRepository.findAll(userId);
        int column = 0;
        KeyboardRow row = new KeyboardRow();
        keyboard.add(row);
        for (final Account account : accounts) {
            if (column == 3) {
                column = 0;
                row = new KeyboardRow();
                keyboard.add(row);
            }
            row.add(new KeyboardButton(account.name));
            column++;
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private enum InnerState {
        NEW,
        ACCOUNT_TO,
        ACCOUNT_FROM,
        NOTIONAL,
        CURRENCY,

    }
}
