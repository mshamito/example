package ru.cryptopro.support.spring.example.expection;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataException extends RuntimeException{
    public DataException(String s) {
        super(s);
        log.error(s);
    }
}