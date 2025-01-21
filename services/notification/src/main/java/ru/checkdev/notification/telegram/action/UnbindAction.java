package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.ProfileReqDTO;
import ru.checkdev.notification.repository.TgAccountRepository;

@AllArgsConstructor
@Slf4j
public class UnbindAction implements Action {
    private final TgAccountRepository accountRepository;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "Введите через пробел логин и пароль для отвязки аккаунта telegram от платформы CheckDev:";
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var profileDto = getProfileDtoFromText(message.getText());
        if (accountRepository.findByEmail(profileDto.getEmail()).isEmpty()) {
            return new SendMessage(chatId,
                    "Telegram аккаунта нет на платформе CheckDev или уже был отвязан ранее." + System.lineSeparator()
                            + "/start");
        }
        accountRepository.deleteByEmail(profileDto.getEmail());
        return new SendMessage(chatId,
                "Telegram аккаунт был успешно отвязан." + System.lineSeparator()
                        + "/start");
    }

    private ProfileReqDTO getProfileDtoFromText(String text) {
        var loginPassword = text.split(" ", 2);
        var login = loginPassword[0];
        var password = loginPassword[1];
        return new ProfileReqDTO(login, password);
    }
}
