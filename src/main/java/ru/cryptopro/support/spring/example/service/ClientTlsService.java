package ru.cryptopro.support.spring.example.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.cryptopro.support.spring.example.config.SSLContextConfig;
import ru.cryptopro.support.spring.example.dto.ClienTlsDto;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

@Log4j2
@Service
public class ClientTlsService {
    private final SSLContextConfig contextConfig;
    private final TrustManager[] trustManagers;

    public ClientTlsService(
            SSLContextConfig contextConfig,
            @Qualifier("gostTrustManagers")
            TrustManager[] trustManagers) {
        this.contextConfig = contextConfig;
        this.trustManagers = trustManagers;
    }

    @SneakyThrows
    public String connect(ClienTlsDto clienTlsDto) {
//        return connectHttpsURLConnection(clienTlsDto);
        return connectOhHttp(clienTlsDto);
    }

    @SneakyThrows
    public String connectOhHttp(ClienTlsDto clienTlsDto) {
        URL url = URI.create(clienTlsDto.getUrl()).toURL();
        String[] ciphers = new String[]{
                "TLS_CIPHER_2001",
                "TLS_CIPHER_2012",
                "TLS_GOSTR341112_256_WITH_KUZNYECHIK_CTR_OMAC",
                "TLS_GOSTR341112_256_WITH_MAGMA_CTR_OMAC"
        };
        ConnectionSpec spec = new ConnectionSpec.Builder(
                ConnectionSpec.MODERN_TLS)
                .tlsVersions("TLSv1.2", "TLSv1.1", "TLSv1")
                .cipherSuites(ciphers)
                .build();
        SSLSocketFactory socketFactory = contextConfig.getInstance(clienTlsDto.isMTLS()).getSocketFactory();
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
                .connectionSpecs(Collections.singletonList(spec))
                .build();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            log.info("Url: {}", clienTlsDto.getUrl());
            log.info("mTLS: {}", clienTlsDto.isMTLS());
            log.info("Cipher: {}", response.handshake().cipherSuite());
            log.info("Status Code: {}", response.code());
            return response.body().string();
        }
    }

    @SneakyThrows
    public String connectHttpsURLConnection(ClienTlsDto clienTlsDto) {
        URL url = URI.create(clienTlsDto.getUrl()).toURL();
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
