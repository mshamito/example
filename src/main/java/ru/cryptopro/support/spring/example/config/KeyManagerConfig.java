package ru.cryptopro.support.spring.example.config;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.ssl.JavaTLSCertPathManagerParameters;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Log4j2
@Configuration
public class KeyManagerConfig {
    private final StoreConfig storeConfig;
    private final List<X509Certificate> certsFromFiles;
    private final char[] password;

    public KeyManagerConfig(
            StoreConfig storeConfig,
            @Qualifier("certsFromCACerts")
            List<X509Certificate> certsFromFiles
    ) {
        this.storeConfig = storeConfig;
        this.certsFromFiles = certsFromFiles;
        this.password = storeConfig.getPin().toCharArray();
    }

    @Bean("gostKeyManagers")
    @SneakyThrows
    public KeyManager[] getKeyManagers() {

        KeyManagerFactory factory = KeyManagerFactory.getInstance("GostX509");
        if (storeConfig.isChainNeeded()) {
            log.warn("Need certificate chain for your alias. using local certificates");
            KeyStore trustStore = KeyStore.getInstance(JCP.CERT_STORE_NAME);
            trustStore.load(null,null);
            for (X509Certificate certificate : certsFromFiles)
                trustStore.setCertificateEntry(UUID.randomUUID().toString(), certificate);
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            parameters.setRevocationEnabled(true);
            parameters.setCertStores(Collections.singletonList(CertStore.getInstance("Collection", new CollectionCertStoreParameters(certsFromFiles))));
            JavaTLSCertPathManagerParameters managerParameters = new JavaTLSCertPathManagerParameters(storeConfig.getKeyStore(), password);
            managerParameters.setParameters(parameters);
            factory.init(managerParameters);
            return factory.getKeyManagers();
        }
        factory.init(storeConfig.getKeyStore(), password);
        return factory.getKeyManagers();
    }
}
