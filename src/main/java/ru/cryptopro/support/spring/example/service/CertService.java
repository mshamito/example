package ru.cryptopro.support.spring.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;

import java.io.InputStream;
import java.security.cert.*;
import java.util.HashSet;
import java.util.Set;

@Service
@Log4j2
public class CertService {
    private final Set<X509Certificate> certificateSet;

    public CertService(@Qualifier("certsFromCACerts") Set<X509Certificate> certificateSet) {
        this.certificateSet = certificateSet;
    }

    public boolean validateCertificate(MultipartFile cert) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(cert.getInputStream());
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(tryToGuess);
            Set<TrustAnchor> trustAnchors = new HashSet<>();
            for (X509Certificate walk : certificateSet)
                trustAnchors.add(new TrustAnchor(walk, null));
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustAnchors, null);
            parameters.setSigProvider(null);
            CollectionCertStoreParameters certStoreParameters = new CollectionCertStoreParameters(certificateSet);
            CertStore store = CertStore.getInstance("Collection", certStoreParameters);
            parameters.addCertStore(store);
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(certificate);
            parameters.setTargetCertConstraints(selector);
            parameters.setRevocationEnabled(false);
            PKIXCertPathBuilderResult result =
                    (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("CPPKIX", "RevCheck")
                            .build(parameters);
            CertPath certPath = result.getCertPath();
            log.info("certificate chain builted");

            CertPathValidator certPathValidator = CertPathValidator.getInstance("CPPKIX", "RevCheck");
            parameters.setRevocationEnabled(true);
            certPathValidator.validate(certPath, parameters);
            log.info("certificate chain validated");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
