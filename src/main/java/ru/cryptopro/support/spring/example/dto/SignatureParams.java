package ru.cryptopro.support.spring.example.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignatureParams {
    @Builder.Default
    private boolean detached = false;
    @Builder.Default
    private boolean encodeToB64 = true;
    @Builder.Default
    private String tsp = "";
    @Builder.Default
    private String type = "bes";
}
