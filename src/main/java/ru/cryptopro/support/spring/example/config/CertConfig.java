package ru.cryptopro.support.spring.example.config;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Log
@Configuration
public class CertConfig {
    private final StoreConfig storeConfig;

    private X509Certificate certificate;

    public CertConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Bean("cert")
    public X509Certificate getCertificate() {
        if (certificate != null)
            return certificate;

        try {
            certificate = (X509Certificate) storeConfig.getKeyStore().getCertificate(storeConfig.getAlias());
            log.info("Certificate loaded from KeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        return certificate;
    }

    @Bean("key")
    public PrivateKey getKey() {
        JCPProtectionParameter parameter = new JCPProtectionParameter(storeConfig.getPin().toCharArray());
        PrivateKey privateKey = null;
        try {
            privateKey = ( //getEntry  нужен в случае работы jcsp и hsm
                     (JCPPrivateKeyEntry) storeConfig.getKeyStore().getEntry(storeConfig.getAlias(), parameter)
            ).getPrivateKey();
            log.info("PrivateKey loaded from alias: " + storeConfig.getAlias());

        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    @SneakyThrows
    @Bean("certsFromCACerts")
    public List<X509Certificate> getCertsFromCacerts() {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        List<TrustManager> trustManagers = Arrays.asList(trustManagerFactory.getTrustManagers());
        return trustManagers.stream()
                .filter(X509TrustManager.class::isInstance)
                .map(X509TrustManager.class::cast)
                .map(trustManager -> Arrays.asList(trustManager.getAcceptedIssuers()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
