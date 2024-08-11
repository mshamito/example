package ru.cryptopro.support.spring.example.utils;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public class HeadersHelper {
    public static HttpHeaders prepareHeaders(String originalFileName, String fileExtension) {
        return prepareHeaders(originalFileName, fileExtension, true);
    }
    public static HttpHeaders prepareHeaders(String originalFileName, String fileExtension, boolean base64) {
        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename(originalFileName + fileExtension, StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        if (base64)
            headers.setContentType(MediaType.TEXT_PLAIN);
        return headers;
    }
}
