package ru.cryptopro.support.spring.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.dto.CertDto;
import ru.cryptopro.support.spring.example.dto.CertVerifyResult;
import ru.cryptopro.support.spring.example.utils.CastX509Helper;

import java.security.cert.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class CertService {
    private final Set<X509Certificate> certificateSet;

    public CertService(@Qualifier("certsFromCACerts") Set<X509Certificate> certificateSet) {
        this.certificateSet = certificateSet;
    }


    public CertVerifyResult validateCertificate(MultipartFile cert) {
        return validateCertificate(CastX509Helper.castCertificate(cert));
    }

    public CertVerifyResult validateCertificate(X509Certificate cert) {
        CertVerifyResult certVerifyResult = new CertVerifyResult();
        try {
            Set<TrustAnchor> trustAnchors = new HashSet<>();
            for (X509Certificate walk : certificateSet)
                trustAnchors.add(new TrustAnchor(walk, null));
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustAnchors, null);
            parameters.setSigProvider(null);
            CollectionCertStoreParameters certStoreParameters = new CollectionCertStoreParameters(certificateSet);
            CertStore store = CertStore.getInstance("Collection", certStoreParameters);
            parameters.addCertStore(store);
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(cert);
            parameters.setTargetCertConstraints(selector);
            parameters.setRevocationEnabled(false);
            PKIXCertPathBuilderResult result =
                    (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("CPPKIX", "RevCheck")
                            .build(parameters);
            CertPath certPath = result.getCertPath();
            log.info("certificate chain built");
            certVerifyResult.setTrusted(true);
            certVerifyResult.setCerts(
                    Stream.concat(
                                    certPath.getCertificates().stream(),
                                    Stream.of(result.getTrustAnchor().getTrustedCert())
                            )
                            .map(X509Certificate.class::cast)
                            .map(CertDto::new)
                            .collect(Collectors.toList())
            );

            CertPathValidator certPathValidator = CertPathValidator.getInstance("CPPKIX", "RevCheck");
            parameters.setRevocationEnabled(true);
            certPathValidator.validate(certPath, parameters);
            log.info("certificate chain validated");
            certVerifyResult.setRevoked(false);
            return certVerifyResult;
        } catch (Exception e) {
            log.error(e);
            return certVerifyResult;
        }
    }
}
