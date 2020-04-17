package ru.oval.accounting.viewmodel.states;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.oval.accounting.model.model.deals.Bond;
import ru.oval.accounting.model.model.deals.CashFlow;
import ru.oval.accounting.model.model.deals.Equity;
import ru.oval.accounting.viewmodel.Event;

import java.util.List;

public class NewState implements State {
    private static final String BOND = "Bond";
    private static final String EQUITY = "Equity";
    private static final String DEBIT_CREDIT = "Debit/Credit";
    private static final String MESSAGE = "Select deal type";

    private final int userId;

    public NewState(final int userId) {
        this.userId = userId;
    }

    @Override
    public State next(final Event event) {
        if (!event.isMessageEvent()) {
            return this;
        }
        switch (event.sendMessage.getText()) {
            case BOND:
                return new BondState(Bond.builder());
            case EQUITY:
                return new EquityState(Equity.builder());
            case DEBIT_CREDIT:
                return new CashFlowState(CashFlow.builder(), userId);
            default:
                return this;
        }
    }

    @Override
    public SendMessage currentMessage(final SendMessage prototype) {
        return prototype
                .setText(MESSAGE)
                .setReplyMarkup(createDealsKeyboard());
    }

    private ReplyKeyboardMarkup createDealsKeyboard() {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setSelective(true)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);

        final KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(new KeyboardButton(BOND));
        firstRow.add(new KeyboardButton(EQUITY));
        final KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(new KeyboardButton(DEBIT_CREDIT));

        final List<KeyboardRow> keyboard = List.of(firstRow, secondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
}
