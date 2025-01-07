package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InfoActionTest {

    private Action infoAction;

    @Test
    public void whenHandleWithActionsListThenGetMessageWithActionsList() {
        var actions = List.of("/start", "/bind", "/unbind");
        infoAction = new InfoAction(actions);

        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                String.join(System.lineSeparator(),
                        "Выберите действие:",
                        "/start",
                        "/bind",
                        "/unbind" + System.lineSeparator()));
        assertThat(infoAction.handle(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenHandleWithoutActionsThenGetMessage() {
        List<String> actions = List.of();
        infoAction = new InfoAction(actions);

        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Выберите действие:" + System.lineSeparator());
        assertThat(infoAction.handle(message)).isEqualTo(expectedMessage);
    }
}