package ru.cryptopro.support.spring.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TlsConnectionResult {
    private String status;
    private Map<String, List<String>> headers;
    private String cipher;
    private String http;
    private String protocol;
    private List<CertDto> certs;
}
