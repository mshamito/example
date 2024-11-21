package ru.cryptopro.support.spring.example.exception.handler;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.cryptopro.support.spring.example.exception.CryptographicException;
import ru.cryptopro.support.spring.example.exception.handler.helper.ErrorMessageHelper;

import java.util.Map;

@SuppressWarnings("unused")
@ControllerAdvice
public class CryptographicExceptionHandler {
    @ExceptionHandler(CryptographicException.class)
    public ResponseEntity<Map<String, Object>> exception(CryptographicException exception) {
        return ResponseEntity
                .internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorMessageHelper.getMessageBody(exception));
    }
}
