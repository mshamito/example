package ru.cryptopro.support.spring.example.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.cryptopro.support.spring.example.exception.handler.helper.ErrorMessageHelper;

import java.util.Map;

@SuppressWarnings("unused")
@ControllerAdvice
public class MissingServletRequestParameterExceptionHandler {
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> exception(MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest().body(ErrorMessageHelper.getMessageBody(exception));
    }
}
