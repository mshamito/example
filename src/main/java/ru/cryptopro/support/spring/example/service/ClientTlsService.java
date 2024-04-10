package ru.cryptopro.support.spring.example.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.cryptopro.support.spring.example.config.SSLContextConfig;
import ru.cryptopro.support.spring.example.dto.ClienTlsDto;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@Log4j2
@Service
public class ClientTlsService {
    private final SSLContextConfig contextConfig;

    public ClientTlsService(SSLContextConfig contextConfig) {
        this.contextConfig = contextConfig;
    }

    @SneakyThrows
    public String connect(ClienTlsDto clienTlsDto) {
        URL url = new URL(clienTlsDto.getUrl());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(contextConfig.getInstance(clienTlsDto.isMTLS()).getSocketFactory());
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        log.info("Url: {}", clienTlsDto.getUrl());
        log.info("mTLS: {}", clienTlsDto.isMTLS());
        connection.connect();
        log.info("Cipher: {}", connection.getCipherSuite());
        log.info("Status Code: {}", connection.getResponseCode());
        Class<?>[] classes = {InputStream.class};
        BufferedReader in = new BufferedReader(
                new InputStreamReader((InputStream) connection.getContent(classes)));
        String content = "";
        String current;
        while((current = in.readLine()) != null) {
            content += current;
        }
        return content;
    }
}
