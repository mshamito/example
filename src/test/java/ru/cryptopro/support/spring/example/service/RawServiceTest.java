package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RawServiceTest {
    @Autowired
    RawService rawService;
    @Autowired
    @Qualifier("cert")
    X509Certificate certificate;

    private final byte[] data = "Test Content".getBytes();


    @Test
    void rawTest() {
        assertDoesNotThrow(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            byte[] sign = rawService.sign(bais, false, false);
            bais.reset();
            assertNotEquals(0, sign.length);
            assertTrue(rawService.verify(bais, sign, certificate, false));
        });
    }

    @Test
    void rawInvertTest() {
        assertDoesNotThrow(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            byte[] sign = rawService.sign(bais, false, true);
            bais.reset();
            assertNotEquals(0, sign.length);
            assertTrue(sign.length == 64 || sign.length == 128);
            assertTrue(rawService.verify(bais, sign, certificate, true));
        });
    }

    @Test
    void rawBase64Test() {
        assertDoesNotThrow(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            byte[] signB64 = rawService.sign(bais, true, false);
            bais.reset();
            byte[] sign = EncodingHelper.decode(new String(signB64));
            assertNotEquals(0, sign.length);
            assertTrue(sign.length == 64 || sign.length == 128);
            assertTrue(rawService.verify(bais, sign, certificate, false));
        });
    }
}