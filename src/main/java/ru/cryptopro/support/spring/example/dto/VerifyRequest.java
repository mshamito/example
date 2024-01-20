package ru.cryptopro.support.spring.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyRequest {
    final private byte[] sign;
    final private byte[] data;
}
