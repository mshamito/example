package ru.cryptopro.support.spring.example.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.exception.ProvidedDataException;

import java.io.IOException;
import java.io.InputStream;

@Getter
public class VerifyRequest {
    final private InputStream sign;
    final private InputStream data;

    public VerifyRequest(MultipartFile sign, MultipartFile data) {
        try {
            this.sign = sign.getInputStream();
            this.data = data == null ? null : data.getInputStream();
        } catch (IOException e) {
            throw new ProvidedDataException(e.getMessage());
        }
    }

    public VerifyRequest(InputStream sign, InputStream data) {
        this.sign = sign;
        this.data = data;
    }
}
