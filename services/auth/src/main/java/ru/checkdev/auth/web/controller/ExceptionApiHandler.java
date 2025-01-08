package ru.checkdev.auth.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.checkdev.auth.exception.RespEntityNotFoundException;
import ru.checkdev.auth.exception.RespServiceErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionApiHandler {

    @ExceptionHandler(value = { DataIntegrityViolationException.class })
    public void catchDataIntegrityViolationException(Exception e,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response)
            throws IOException {
        handleExceptionAndSetResponse(e, request, response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { SocketException.class })
    public void catchSocketException(Exception e,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        handleExceptionAndSetResponse(e, request, response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = { TransientDataAccessException.class })
    public void catchTransientDataAccessException(Exception e,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws IOException {
        handleExceptionAndSetResponse(e, request, response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = { RespServiceErrorException.class })
    public void catchRespServiceErrorException(Exception e,
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws IOException {
        handleExceptionAndSetResponse(e, request, response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = { RespEntityNotFoundException.class })
    public void catchRespEntityNotFoundException(Exception e,
                                               HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {
        handleExceptionAndSetResponse(e, request, response, HttpStatus.NOT_FOUND);
    }

    private Map<String, String> convertExceptionToMap(Exception e, HttpServletRequest request) {
        return Map.of(
                "message", e.getMessage(),
                "type", String.valueOf(e.getClass()),
                "timestamp", String.valueOf(LocalDateTime.now()),
                "path", request.getRequestURI());
    }

    private void handleExceptionAndSetResponse(Exception e,
                                               HttpServletRequest request,
                                               HttpServletResponse response,
                                               HttpStatus status) throws IOException {
        Map<String, String> details = convertExceptionToMap(e, request);
        response.setStatus(status.value());
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(details));
        log.error(e.getLocalizedMessage());
    }
}
