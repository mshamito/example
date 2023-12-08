package ru.cryptopro.support.spring.example.expection.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.expection.handler.helper.ErrorMessageHelper;

import java.util.Map;

@ControllerAdvice
public class CryptographicExceptionHandler {
    @ExceptionHandler(CryptographicException.class)
    public ResponseEntity<Map<String,Object>> exception(CryptographicException exception) {
        return ResponseEntity.internalServerError().body(ErrorMessageHelper.getMessageBody(exception));
    }
}
