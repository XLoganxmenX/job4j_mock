package ru.checkdev.mock.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import ru.checkdev.mock.exception.RespClientErrorException;
import ru.checkdev.mock.exception.RespEntityNotFoundException;
import ru.checkdev.mock.exception.RespServiceErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class.getSimpleName());

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(value = {NullPointerException.class, IllegalArgumentException.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", "Some of fields empty");
            put("details", e.getMessage());
        }}));
        LOGGER.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handle(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                e.getFieldErrors().stream()
                        .map(f -> Map.of(
                                f.getField(),
                                String.format("%s. Actual value: %s", f.getDefaultMessage(), f.getRejectedValue())
                        ))
                        .collect(Collectors.toList())
        );
    }

    @ExceptionHandler(value = SQLException.class)
    public void sqlException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", "Error when saving or retrieving data");
            put("details", e.getMessage());
        }}));
        LOGGER.error(e.getMessage());
    }

    @ExceptionHandler(value = ResponseStatusException.class)
    public void responseStatusException(Exception e,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", "Not found");
            put("details", e.getMessage());
            put("request URI", request.getRequestURI());
        }}));
        LOGGER.error(e.getMessage());
    }

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

    @ExceptionHandler(value = { RespClientErrorException.class })
    public void catchRespClientErrorException(Exception e,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws IOException {
        handleExceptionAndSetResponse(e, request, response, HttpStatus.BAD_REQUEST);
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
        LOGGER.error(e.getLocalizedMessage());
    }
}
