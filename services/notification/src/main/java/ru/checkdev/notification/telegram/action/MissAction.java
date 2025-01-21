package ru.checkdev.notification.telegram.action;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MissAction implements Action {
    @Override
    public BotApiMethod<Message> handle(Message message) {
        return new SendMessage(message.getChatId().toString(),
                "Команда не поддерживается! Список доступных команд: /start");
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return new SendMessage(message.getChatId().toString(),
                "Команда не поддерживается! Список доступных команд: /start");
    }
}
