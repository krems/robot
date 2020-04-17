package ru.oval.accounting.viewmodel.states;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.oval.accounting.model.model.deals.Equity;
import ru.oval.accounting.viewmodel.Event;

public class EquityState implements State {
    public EquityState(final Equity.EquityBuilder builder) {
    }

    @Override
    public State next(final Event event) {
        return null;
    }

    @Override
    public SendMessage currentMessage(final SendMessage prototype) {
        return null;
    }
}
