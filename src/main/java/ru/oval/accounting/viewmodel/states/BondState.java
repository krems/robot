package ru.oval.accounting.viewmodel.states;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.oval.accounting.model.model.deals.Bond;
import ru.oval.accounting.viewmodel.Event;

public class BondState implements State {
    public BondState(final Bond.BondBuilder builder) {
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
