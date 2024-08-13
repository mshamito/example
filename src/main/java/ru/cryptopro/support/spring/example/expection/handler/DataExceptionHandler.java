package ru.cryptopro.support.spring.example.expection.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.cryptopro.support.spring.example.expection.ProvidedDataException;
import ru.cryptopro.support.spring.example.expection.handler.helper.ErrorMessageHelper;

import java.util.Map;

@SuppressWarnings("unused")
@ControllerAdvice
public class DataExceptionHandler {
    @ExceptionHandler(ProvidedDataException.class)
    public ResponseEntity<Map<String, Object>> exception(ProvidedDataException exception) {
        return ResponseEntity.badRequest().body(ErrorMessageHelper.getMessageBody(exception));
    }
}
