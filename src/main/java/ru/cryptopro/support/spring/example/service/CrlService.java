package ru.cryptopro.support.spring.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.cryptopro.support.spring.example.config.CrlConfig;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.*;

@Log4j2
@Service
public class CrlService {

    private final CertificateFactory factory;
    private final String BASE_DIR;

    public CrlService(CrlConfig config) {
        this.BASE_DIR = config.getCrlFolder();
        try {
            factory = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFolderExist() {
        return new File(BASE_DIR).exists();
    }

    private Optional<X509CRL> generateCrl(String filename) {
        File file = new File(BASE_DIR, filename);
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            log.info("reading crl: {}", filename);
            X509CRL crl = (X509CRL) factory.generateCRL(
                    EncodingHelper.decodeDerOrB64Stream(stream)
            );
            if (crl.getNextUpdate().compareTo(new Date()) >= 0) {
                return Optional.of(crl);
            } else {
                log.warn("skipping expired crl: {}", filename);
                return Optional.empty();
            }
        } catch (CRLException | IOException e) {
            log.error(e);
            return Optional.empty();
        }
    }

    public Set<X509CRL> getLocalCRLs() {
        if (!isFolderExist())
            return null;
        File[] files = new File(BASE_DIR).listFiles((dir, name) -> name.toLowerCase().endsWith(".crl"));
        if (files == null || files.length == 0)
            return null;
        List<X509CRL> result = new ArrayList<>();
        for (File walk : files) {
            Optional<X509CRL> crl = generateCrl(walk.getName());
            crl.ifPresent(result::add);
        }
        if (result.isEmpty())
            return null;
        return new HashSet<>(result);
    }
}
