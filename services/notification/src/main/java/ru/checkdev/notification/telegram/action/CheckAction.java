package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.repository.TgAccountRepository;

@AllArgsConstructor
public class CheckAction implements Action {
    private final TgAccountRepository accountRepository;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        return callback(message);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var userId = Math.toIntExact(message.getFrom().getId());
        var optionalAccount = accountRepository.findByTgUserId(userId);
        if (optionalAccount.isEmpty()) {
            return new SendMessage(chatId,
                    "Telegram аккаунт не найден на платформе CheckDev." + System.lineSeparator()
                            + "/start");
        }
        var account = optionalAccount.get();
        var userName = StringUtils.defaultIfBlank(account.getTgUsername(), "нет данных");
        var firstName = StringUtils.defaultIfBlank(account.getTgFirstName(), "нет данных");
        var lastName = StringUtils.defaultIfBlank(account.getTgLastName(), "нет данных");
        var email = account.getEmail();
        var text = String.join(System.lineSeparator(),
                "Ник пользователя: " + userName,
                "Имя: " + firstName,
                "Фамилия и Отчество: " + lastName,
                "Email: " + email);
        return new SendMessage(chatId, text);
    }
}
