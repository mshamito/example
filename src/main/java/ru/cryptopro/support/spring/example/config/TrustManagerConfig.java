package ru.cryptopro.support.spring.example.config;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.JCP;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Configuration
public class TrustManagerConfig {
    private final Set<X509Certificate> certs;

    public TrustManagerConfig(
            @Qualifier("certsFromCACerts")
            Set<X509Certificate> certs
    ) {
        this.certs = certs;
    }

    @Bean("gostTrustManagers")
    @SneakyThrows
    public TrustManager[] getGostTrustManager(){
        KeyStore keyStore = KeyStore.getInstance(JCP.CERT_STORE_NAME);
        keyStore.load( null, null);
        for (X509Certificate cert : certs) {
            keyStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
        }

        log.info("trusted certificate size is {} for remote web tls", certs.size());

        TrustManagerFactory factory = TrustManagerFactory.getInstance("GostX509");
        factory.init(keyStore);
        return factory.getTrustManagers();
    }
}
