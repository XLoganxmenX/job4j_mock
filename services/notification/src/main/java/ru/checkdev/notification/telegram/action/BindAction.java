package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.checkdev.notification.domain.ProfileReqDTO;
import ru.checkdev.notification.domain.ProfileRespDTO;
import ru.checkdev.notification.domain.TgAccount;
import ru.checkdev.notification.repository.TgAccountRepository;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

@AllArgsConstructor
@Slf4j
public class BindAction implements Action {
    private static final String URL_AUTH_FIND_PROFILE = "/profiles/find";
    private final TgAuthCallWebClint authCallWebClint;
    private final TgAccountRepository accountRepository;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "Введите через пробел логин и пароль для привязки аккаунта telegram к платформе CheckDev:";
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var tgUser = message.getFrom();
        var profileDto = getProfileDtoFromText(message.getText());
        ProfileRespDTO profile;
        if (accountRepository.findByEmail(profileDto.getEmail()).isPresent()) {
            return new SendMessage(chatId,
                    "Telegram аккаунт уже привязан к платформе CheckDev." + System.lineSeparator()
                    + "/start");
        }
        try {
            profile = authCallWebClint.getProfile(URL_AUTH_FIND_PROFILE, profileDto).block();
        } catch (WebClientResponseException e) {
            return handleWebClientException(chatId, e);
        } catch (Exception e) {
            return new SendMessage(chatId,
                    "Возникла непредвиденная ошибка при обращении к сервису авторизации, попробуйте позже."
                    + System.lineSeparator() + "/start");
        }
        TgAccount account = buildTgAccountFrom(message, profile, tgUser);
        accountRepository.save(account);
        return new SendMessage(chatId, "Аккаунт Telegram успешно привязан к платформе CheckDev");
    }

    private ProfileReqDTO getProfileDtoFromText(String text) {
        var loginPassword = text.split(" ", 2);
        var login = loginPassword[0];
        var password = loginPassword[1];
        return new ProfileReqDTO(login, password);
    }

    private BotApiMethod<Message> handleWebClientException(String chatId, WebClientResponseException e) {
        String text;
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            text = "Профиль не найден. Убедитесь, что вы ввели правильные данные." + System.lineSeparator()
                    + "/start";
        } else if (e.getStatusCode().is5xxServerError()) {
            text = "Ошибка при обращении к сервису авторизации, попробуйте позже." + System.lineSeparator()
                    + "/start";
        } else {
            text = "Неизвестная ошибка при обработке запроса." + System.lineSeparator() + "/start";
        }
        return new SendMessage(chatId, text);
    }

    private TgAccount buildTgAccountFrom(Message message, ProfileRespDTO profile, User tgUser) {
        return TgAccount.builder()
                .userId(profile.getId())
                .tgUserId(Math.toIntExact(tgUser.getId()))
                .tgUsername(tgUser.getUserName())
                .tgFirstName(tgUser.getFirstName())
                .tgLastName(tgUser.getLastName())
                .email(profile.getEmail())
                .tgChatId(Math.toIntExact(message.getChatId()))
                .build();
    }
}
