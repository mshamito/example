package ru.cryptopro.support.spring.example.config;

import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

@Log
@Getter
@Configuration
public class StoreConfig {
    private KeyStore keyStore;


    @Value("${app.cp.keystore}")
    private String keyStoreName;
    @Value("${app.cp.alias}")
    private String alias;
    @Value("${app.cp.pin}")
    private String pin;

    public KeyStore getKeyStore() {
        if (keyStore != null)
            return keyStore;
        try {
            keyStore = KeyStore.getInstance(keyStoreName);
            keyStore.load(new StoreInputStream(alias), null);
            log.info("KeyStore loaded: " + keyStoreName);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return keyStore;
    }


}
