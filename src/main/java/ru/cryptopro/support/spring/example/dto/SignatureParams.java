package ru.cryptopro.support.spring.example.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignatureParams {
    @Builder.Default
    private final boolean detached = false;
    @Builder.Default
    private final boolean encodeToB64 = true;
    @Builder.Default
    private final String tsp = "";
    @Builder.Default
    private final String type = "bes";
}
