package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.ProfileRespDTO;
import ru.checkdev.notification.domain.TgAccount;
import ru.checkdev.notification.repository.TgAccountRepository;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BindActionTest {
    @Mock
    private TgAuthCallWebClint authCallWebClint;
    @Mock
    private TgAccountRepository accountRepository;

    private Action bindAction;

    @BeforeEach
    void setUp() {
        bindAction = new BindAction(authCallWebClint, accountRepository);
    }

    @Test
    public void whenHandleThenGetMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Введите через пробел логин и пароль для привязки аккаунта telegram к платформе CheckDev:");
        assertThat(bindAction.handle(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndExistAccountThenGetExistMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        var email = "test@test.ru";
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Telegram аккаунт уже привязан к платформе CheckDev." + System.lineSeparator()
                        + "/start");
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(new TgAccount()));
        assertThat(bindAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndFindProfileThenGetOkMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        message.setText("test@test.ru tg/test123");
        var user = new User(2L, "firstName", false);
        user.setId(3L);
        user.setLastName("lastName");
        user.setUserName("userName");
        message.setFrom(user);
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Аккаунт Telegram успешно привязан к платформе CheckDev");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(authCallWebClint.getProfile(any(), any()))
                .thenReturn(Mono.just(new ProfileRespDTO(1, "username", "email", true)));
        assertThat(bindAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndThrowsResponseException404ThenGetErrorMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Профиль не найден. Убедитесь, что вы ввели правильные данные." + System.lineSeparator()
                        + "/start");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(authCallWebClint.getProfile(any(), any()))
                .thenThrow(new WebClientResponseException(404, "Not Found", null, null, null));
        assertThat(bindAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndThrowsResponseException500ThenGetErrorMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Ошибка при обращении к сервису авторизации, попробуйте позже." + System.lineSeparator()
                        + "/start");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(authCallWebClint.getProfile(any(), any()))
                .thenThrow(new WebClientResponseException(500, "Internal Server Error", null, null, null));
        assertThat(bindAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndThrowsSomeResponseExceptionThenGetErrorMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Неизвестная ошибка при обработке запроса." + System.lineSeparator() + "/start");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(authCallWebClint.getProfile(any(), any()))
                .thenThrow(new WebClientResponseException(401, "Unauthorized", null, null, null));
        assertThat(bindAction.callback(message)).isEqualTo(expectedMessage);
    }

    @Test
    public void whenCallbackAndThrowsSomeExceptionThenGetErrorMessage() {
        var message = new Message();
        message.setChat(new Chat(1L, "test"));
        message.setText("test@test.ru tg/test123");
        var expectedMessage = new SendMessage(message.getChatId().toString(),
                "Возникла непредвиденная ошибка при обращении к сервису авторизации, попробуйте позже."
                        + System.lineSeparator() + "/start");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(authCallWebClint.getProfile(any(), any()))
                .thenThrow(new NullPointerException("Error"));
        assertThat(bindAction.callback(message)).isEqualTo(expectedMessage);
    }
}