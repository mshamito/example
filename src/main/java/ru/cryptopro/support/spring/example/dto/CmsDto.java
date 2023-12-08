package ru.cryptopro.support.spring.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

import java.util.Base64;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsDto {
    final String cms;
    SignatureParams params;

    public CmsDto(byte[] cms) {
        this.cms = Base64.getEncoder().encodeToString(cms);
    }

    public CmsDto(byte[] sign, SignatureParams params)
    {
        this.cms = Base64.getEncoder().encodeToString(sign);
        this.params = params;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return Strings.isBlank(cms);
    }
}