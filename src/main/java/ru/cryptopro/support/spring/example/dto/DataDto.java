package ru.cryptopro.support.spring.example.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@NoArgsConstructor
public class DataDto {
    @Getter
    private byte[] data;

    public DataDto(byte[] data) {
        this.data = data;
    }
}
