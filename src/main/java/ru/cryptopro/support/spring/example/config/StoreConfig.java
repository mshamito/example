package ru.cryptopro.support.spring.example.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

@Log4j2
@Getter
@Configuration
@ConfigurationProperties(prefix = "app.cp")
public class StoreConfig {
    private KeyStore keyStore;

    @Setter
    @NotBlank
    private String providerName;
    @Setter
    @NotBlank
    private String keyStoreName;
    @Setter
    @NotBlank
    private String alias;
    @Setter
    private String pin;
    private boolean isChainNeeded = false;

    public KeyStore getKeyStore() {
        if (keyStore != null)
            return keyStore;
        try {
            validateStore();
            keyStore = KeyStore.getInstance(keyStoreName, providerName);
            keyStore.load(new StoreInputStream(alias), null);
            Certificate[] chain = keyStore.getCertificateChain(alias);

            if (chain == null) {
                KeyStore listKeyStore = KeyStore.getInstance(keyStoreName, providerName);
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
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        return keyStore;
    }

    private void validateStore() {
        try {
            KeyStore fullKeyStore = KeyStore.getInstance(keyStoreName, providerName);
            fullKeyStore.load(null,null);
            List<String> aliases = Collections.list(fullKeyStore.aliases());
            if (aliases.isEmpty())
                throw new RuntimeException("empty KeyStore");
            log.info("available aliases on {} :", keyStoreName);
            for (String walk : aliases)
                log.info("{}) {}", aliases.indexOf(walk) + 1, walk);
            log.info("aliases enumerated");
            log.info("checking configured alias");
            if (!aliases.contains(alias)) {
                log.error("alias {} not found in {}", alias, keyStoreName);
                throw new RuntimeException(String.format("configured alias %s not found", alias));
            }
            log.info("configured alias found. trying to load");

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException |
                 NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }


}
