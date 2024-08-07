package ru.cryptopro.support.spring.example.config;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;

import java.io.File;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

@Log4j2
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
    public Set<X509Certificate> getCertsFromCacerts() {
        Set<X509Certificate> result = new HashSet<>();
        KeyStore cacerts = KeyStore.getInstance("JKS");
        String cacertsPath = System.getProperty("java.home") + "/lib/security/cacerts".replace("/", File.separator);
        cacerts.load(new FileInputStream(cacertsPath), "changeit".toCharArray());
        Enumeration<String> aliases = cacerts.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            result.add((X509Certificate) cacerts.getCertificate(alias));
        }
        return result;
    }
}
