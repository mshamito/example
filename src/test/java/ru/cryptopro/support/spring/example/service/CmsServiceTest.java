package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.CryptoPro.JCP.JCP;
import ru.cryptopro.support.spring.example.dto.SignatureParams;
import ru.cryptopro.support.spring.example.dto.VerifyRequest;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@SpringBootTest
class CmsServiceTest {

    final int smallFileSize = 8 * 1024 * 1024;
    final int mediumFileSize = 25 * 1024 * 1024;
    final int bigFileSize = 50 * 1024 * 1024;

    @Autowired
    CmsService cmsService;

    @Test
    void encryptionSmallDefault() {
        byte[] small = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(small));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(small), Collections.emptyList(), EncryptionKeyAlgorithm.ekaDefault, false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallMagma() {
        byte[] small = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(small));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(small), Collections.emptyList(), EncryptionKeyAlgorithm.ekaMagma, false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }
    @Test
    void encryptionSmallMagmaMac() {
        byte[] small = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(small));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(small), Collections.emptyList(), EncryptionKeyAlgorithm.ekaMagmaMac, false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallKuznechik() {
        byte[] small = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(small));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(small), Collections.emptyList(), EncryptionKeyAlgorithm.ekaKuznechik, false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallKuznechikMac() {
        byte[] small = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(small));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(small), Collections.emptyList(), EncryptionKeyAlgorithm.ekaKuznechikMac, false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionMedium() {
        byte[] medium = genFile(mediumFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(medium));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(medium), Collections.emptyList(), null,false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionBig() {
        byte[] big = genFile(bigFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(big));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cmsService.encrypt(new ByteArrayInputStream(big), Collections.emptyList(), null, false).toByteArray();
                    byte[] decrypted = cmsService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void signAndVerifySmall() {
        byte[] small = genFile(smallFileSize);
        SignatureParams params = SignatureParams.builder()
                .encodeToB64(true)
                .detached(false)
                .build();
        assertDoesNotThrow(() -> {
                    byte[] sign = cmsService.sign(new ByteArrayInputStream(small), params).toByteArray();
                    VerifyRequest verifyRequest = new VerifyRequest(
                            new ByteArrayInputStream(sign),
                            new ByteArrayInputStream(small)
                    );
                    assertDoesNotThrow(() -> cmsService.verify(verifyRequest));
                }
        );
    }

    @Test
    void signAndVerifyMedium() {
        byte[] medium = genFile(mediumFileSize);
        SignatureParams params = SignatureParams.builder()
                .encodeToB64(false)
                .detached(false)
                .build();
        assertDoesNotThrow(() -> {
                    byte[] sign = cmsService.sign(new ByteArrayInputStream(medium), params).toByteArray();
                    VerifyRequest verifyRequest = new VerifyRequest(
                            new ByteArrayInputStream(sign),
                            new ByteArrayInputStream(medium)
                    );
                    assertDoesNotThrow(() -> cmsService.verify(verifyRequest));
                }
        );
    }

    @Test
    void signAndVerifyBig() {
        byte[] big = genFile(bigFileSize);
        SignatureParams params = SignatureParams.builder()
                .encodeToB64(false)
                .detached(true)
                .build();
        assertDoesNotThrow(() -> {
                    byte[] sign = cmsService.sign(new ByteArrayInputStream(big), params).toByteArray();
                    VerifyRequest verifyRequest = new VerifyRequest(
                            new ByteArrayInputStream(sign),
                            new ByteArrayInputStream(big)
                    );
                    assertDoesNotThrow(() -> cmsService.verify(verifyRequest));
                }
        );
    }

    private byte[] genFile(int size) {
        Random random = new Random();
        byte[] out = new byte[size];
        random.nextBytes(out);
        return out;
    }

    private byte[] gostHash(byte[] data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(JCP.GOST_DIGEST_2012_256_NAME);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return digest.digest(data);
    }
}
