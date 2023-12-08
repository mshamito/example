package ru.cryptopro.support.spring.example.expection.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.cryptopro.support.spring.example.expection.DataException;
import ru.cryptopro.support.spring.example.expection.handler.helper.ErrorMessageHelper;

import java.util.Map;

@ControllerAdvice
public class DataExceptionHandler {
    @ExceptionHandler(DataException.class)
    public ResponseEntity<Map<String, Object>> exception(DataException exception) {
        return ResponseEntity.badRequest().body(ErrorMessageHelper.getMessageBody(exception));
    }
}
