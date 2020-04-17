package ru.oval.accounting.viewmodel;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.oval.accounting.viewmodel.states.NewState;
import ru.oval.accounting.viewmodel.states.State;

import static ru.oval.accounting.viewmodel.Event.messageEvent;

public class Session {
    public final Integer userId;
    public State state;


    public Session(final Integer userId) {
        this.userId = userId;
        state = new NewState(userId);
    }

    public SendMessage onMessage(final Message message) {
        final SendMessage response = new SendMessage()
                .setChatId(message.getChatId())
                .enableMarkdown(true);
        this.state = state.next(messageEvent(message));
        return state.currentMessage(response);
    }
}
