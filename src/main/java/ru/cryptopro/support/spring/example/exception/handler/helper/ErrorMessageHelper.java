package ru.cryptopro.support.spring.example.exception.handler.helper;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessageHelper {
    public static Map<String, Object> getMessageBody(Exception message) {
        return new HashMap<String, Object>() {{
            put("error", true);
            put("errorMsg", message.getMessage());
        }};
    }
}
