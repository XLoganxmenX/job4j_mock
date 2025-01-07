package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgAccount;
import ru.checkdev.notification.repository.TgAccountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnbindActionTest {
    @Mock
    private TgAccountRepository accountRepository;

    private Action unbindAction;

    @BeforeEach
    void setUp() {
        unbindAction = new UnbindAction(accountRepository);
    }

    @Test
    public void whenHandleThenGetMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Введите через пробел логин и пароль для отвязки аккаунта telegram от платформы CheckDev:");
        assertThat(unbindAction.handle(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndNotExistAccountThenGetNotExistMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var email = "test@test.ru";
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Telegram аккаунта нет на платформе CheckDev или уже был отвязан ранее." + System.lineSeparator()
                        + "/start");
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThat(unbindAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndExistAccountThenGetExistMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var email = "test@test.ru";
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Telegram аккаунт был успешно отвязан." + System.lineSeparator()
                        + "/start");
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(new TgAccount()));
        assertThat(unbindAction.callback(message)).isEqualTo(expectedMessage);
    }
}