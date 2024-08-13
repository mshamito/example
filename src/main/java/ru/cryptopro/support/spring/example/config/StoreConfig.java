package ru.cryptopro.support.spring.example.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

@Log4j2
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
    private boolean isChainNeeded = false;

    public KeyStore getKeyStore() {
        if (keyStore != null)
            return keyStore;
        try {
            keyStore = KeyStore.getInstance(keyStoreName);
            keyStore.load(new StoreInputStream(alias), null);
            Certificate[] chain = keyStore.getCertificateChain(alias);

            if (chain == null) {
                KeyStore listKeyStore = KeyStore.getInstance(keyStoreName);
                listKeyStore.load(null, null);
                List<String> aliases = Collections.list(listKeyStore.aliases());
                if (aliases.isEmpty())
                    throw new RuntimeException("no alias on KeyStore " + keyStoreName);
                StringBuilder builder = new StringBuilder("configured alias not found. available aliases:").append(System.lineSeparator());
                for (String walk : aliases)
                    builder.append(walk).append(System.lineSeparator());
                throw new RuntimeException(builder.toString());
            }

            log.debug("chain length: {} ", chain.length);

            // chain needs for mTLS
            if (chain.length < 2)
                isChainNeeded = true;

            log.info("KeyStore loaded: {}", keyStoreName);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return keyStore;
    }


}
