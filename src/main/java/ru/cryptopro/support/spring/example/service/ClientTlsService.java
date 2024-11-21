package ru.cryptopro.support.spring.example.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.cryptopro.support.spring.example.config.SSLContextConfig;
import ru.cryptopro.support.spring.example.dto.CertDto;
import ru.cryptopro.support.spring.example.dto.ClientTlsDto;
import ru.cryptopro.support.spring.example.dto.TlsConnectionResult;
import ru.cryptopro.support.spring.example.exception.CryptographicException;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public TlsConnectionResult connect(ClientTlsDto clienTlsDto) {
//        return connectHttpsURLConnection(clientTlsDto);
        return connectOkHttp(clienTlsDto);
    }

    @SneakyThrows
    public TlsConnectionResult connectOkHttp(ClientTlsDto clientTlsDto) {
        URL url = URI.create(clientTlsDto.getUrl()).toURL();
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

        SSLSocketFactory socketFactory = contextConfig
                .getInstance(
                        clientTlsDto.isMTLS()
                )
                .getSocketFactory();

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
                .connectionSpecs(Collections.singletonList(spec))
                .build();

        Request request = new Request.Builder().url(url).build();

        TlsConnectionResult result = new TlsConnectionResult();
        try (Response response = client.newCall(request).execute()) {
            result.setHttp(response.protocol().name());
            result.setCode(response.code());
            result.setStatus(response.code() + " " + response.message());
            result.setHeaders(response.headers().toMultimap());
            Handshake handshake = response.handshake();
            if (Objects.nonNull(handshake)) {
                result.setCipher(handshake.cipherSuite().javaName());
                result.setProtocol(handshake.tlsVersion().javaName());
                result.setCerts(
                        handshake.peerCertificates().stream()
                                .map(X509Certificate.class::cast)
                                .map(CertDto::new)
                                .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            throw new CryptographicException(e.getMessage());
        }
        return result;
    }
}
