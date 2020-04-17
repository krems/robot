package ru.oval.accounting.viewmodel.states;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.oval.accounting.viewmodel.Event;

public interface State {
    State next(Event event);

    SendMessage currentMessage(SendMessage prototype);
}
