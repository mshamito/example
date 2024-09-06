package ru.cryptopro.support.spring.example.exception;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProvidedDataException extends RuntimeException{
    public ProvidedDataException(String s) {
        super(s);
        log.error(s);
    }
}