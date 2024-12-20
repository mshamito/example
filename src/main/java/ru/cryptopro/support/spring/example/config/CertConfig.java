package ru.cryptopro.support.spring.example.config;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

@Log4j2
@Configuration
public class CertConfig {
    private final StoreConfig storeConfig;

    private final KeyStore keyStore;

    public CertConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
        this.keyStore = this.storeConfig.getKeyStore();
    }

    @Bean("cert")
    public X509Certificate getCertificate() {
        String alias = storeConfig.getAlias();
        X509Certificate certificate;
        try {
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            log.info("Certificate loaded from KeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        return certificate;
    }

    @SuppressWarnings("unused")
    @Bean("key")
    public PrivateKey getKey() {
        JCPProtectionParameter parameter = new JCPProtectionParameter(storeConfig.getPin().toCharArray());
        PrivateKey privateKey = null;
        try {
            privateKey = ( //getEntry  нужен в случае работы jcsp и hsm
                    (JCPPrivateKeyEntry) keyStore.getEntry(storeConfig.getAlias(), parameter)
            ).getPrivateKey();
            log.info("PrivateKey loaded from alias: {}",storeConfig.getAlias());
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            log.error(e);
        }
        return privateKey;
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    @Bean("certsFromCACerts")
    public Set<X509Certificate> getCertsFromCaCerts() {
        Set<X509Certificate> result = new HashSet<>();
        KeyStore caCerts = KeyStore.getInstance("JKS");
        String caCertsPath = System.getProperty("java.home") + "/lib/security/cacerts".replace("/", File.separator);
        caCerts.load(Files.newInputStream(Paths.get(caCertsPath)), "changeit".toCharArray());
        Enumeration<String> aliases = caCerts.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            result.add((X509Certificate) caCerts.getCertificate(alias));
        }
        return result;
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    @Bean("certsFromResources")
    public Set<X509Certificate> getCertsFromResources() {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Set<X509Certificate> result = new HashSet<>();
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = resolver.getResources("classpath:/certs/*");
        if (resources.length == 0) {
            log.warn("no certs found in classpath:/certs/");
            return result;
        }
        for (Resource resource : resources) {
            log.info("found {} in resource folder", resource.getFilename());
            try {
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(EncodingHelper.decodeDerOrB64Stream(resource.getInputStream()));
                result.add(certificate);
                log.info("loaded ca certificate from resources, {}", certificate.getSubjectX500Principal());
            } catch (Exception e) {
                log.error("failed to read file as certificate {}, exception: {}", resource.getFilename(), e.getMessage());
                log.error(e);
            }
        }
        return result;
    }
}
