package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.checkdev.notification.domain.TgAccount;
import ru.checkdev.notification.repository.TgAccountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {
    @Mock
    private TgAccountRepository accountRepository;

    private Action checkAction;

    @BeforeEach
    void setUp() {
        checkAction = new CheckAction(accountRepository);
    }

    @Test
    public void whenAccountNotExistThenGetNotExistMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        message.setFrom(new User(2L, "firstName", false));
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Telegram аккаунт не найден на платформе CheckDev." + System.lineSeparator()
                        + "/start");
        when(accountRepository.findByTgUserId(Math.toIntExact(message.getFrom().getId())))
                .thenReturn(Optional.empty());
        assertThat(checkAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenAccountExistThenGetFullInfo() {
        var message = new Message();
        message.setChat(new Chat(1L, "Test"));
        message.setFrom(new User(2L, "firstName", false));
        var account = new TgAccount(1, 3, Math.toIntExact(message.getFrom().getId()),
                "username", "firstName", "lastName", "email@email.ru", 1);
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                String.join(System.lineSeparator(),
                        "Ник пользователя: " + account.getTgUsername(),
                        "Имя: " + account.getTgFirstName(),
                        "Фамилия и Отчество: " + account.getTgLastName(),
                        "Email: " + account.getEmail()));
        when(accountRepository.findByTgUserId(Math.toIntExact(message.getFrom().getId())))
                .thenReturn(Optional.of(account));
        assertThat(checkAction.callback(message)).isEqualTo(expectedMessage);
    }
}