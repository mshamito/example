package ru.cryptopro.support.spring.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.security.Principal;
import java.util.Date;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyResult {
    int id;
    private String CAdESType;
    private Principal SubjectDN;
    private Principal IssuerDN;
    private Date notBefore;
    private Date notAfter;
}
