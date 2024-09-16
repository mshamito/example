package ru.cryptopro.support.spring.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cp")
@Data
public class CrlConfig {
    private String crlFolder;
}