package ru.cryptopro.support.spring.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientTlsDto {
    private String url;
    private boolean mTLS;
}
