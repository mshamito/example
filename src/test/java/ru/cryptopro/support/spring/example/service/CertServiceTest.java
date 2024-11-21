package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.cryptopro.support.spring.example.dto.CertVerifyResult;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CertServiceTest {
    @Autowired
    CertService service;
    @Autowired
    X509Certificate certificate;

    @Test
    void validateCertificate() {
        CertVerifyResult result = service.validateCertificate(certificate);
        assertNotNull(result);
        assertTrue(result.isTrusted());
        assertFalse(result.isRevoked());
    }
}