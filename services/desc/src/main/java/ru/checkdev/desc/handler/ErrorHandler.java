package ru.checkdev.desc.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import ru.checkdev.desc.exception.RespClientErrorException;
import ru.checkdev.desc.exception.RespEntityNotFoundException;
import ru.checkdev.desc.exception.RespServiceErrorException;

import java.io.IOException;

@Component
public class ErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            throw new RespServiceErrorException("При запросе возникла непредвиденная ошибка в соседнем сервисе");
        }

        if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                && httpResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new RespClientErrorException("Некорректный запрос в соседний сервис");
        }

        if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                && httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new RespEntityNotFoundException("Запрашиваемый ресурс не найден в соседнем сервисе");
        }
    }
}
