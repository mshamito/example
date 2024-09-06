package ru.cryptopro.support.spring.example.exception;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CryptographicException extends RuntimeException {
    public CryptographicException(String s) {
        super(s);
        log.error(s);
    }
}
