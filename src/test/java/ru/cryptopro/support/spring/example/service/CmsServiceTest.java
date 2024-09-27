package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.cryptopro.support.spring.example.dto.SignatureParams;
import ru.cryptopro.support.spring.example.dto.VerifyRequest;
import ru.cryptopro.support.spring.example.utils.FileStreamWrapper;
import ru.cryptopro.support.spring.example.utils.GOSTHash;

import java.io.ByteArrayInputStream;
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
        byte[] data = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data, EncryptionKeyAlgorithm.ekaDefault);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallMagma() {
        byte[] data = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data, EncryptionKeyAlgorithm.ekaMagma);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallMagmaMac() {
        byte[] data = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data, EncryptionKeyAlgorithm.ekaMagmaMac);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallKuznechik() {
        byte[] data = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data, EncryptionKeyAlgorithm.ekaKuznechik);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionSmallKuznechikMac() {
        byte[] data = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data, EncryptionKeyAlgorithm.ekaKuznechikMac);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionMedium() {
        byte[] data = genFile(mediumFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionBig() {
        byte[] data = genFile(bigFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> GOSTHash.computeHash(data));
        assertDoesNotThrow(() -> {
                    byte[] hash = encryptDecryptAndHash(data);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void signAndVerifySmall() {
        byte[] data = genFile(smallFileSize);
        SignatureParams params = SignatureParams.builder()
                .encodeToB64(true)
                .detached(false)
                .build();
        assertDoesNotThrow(() -> {
                    FileStreamWrapper sign = cmsService.sign(new ByteArrayInputStream(data), params);
                    VerifyRequest verifyRequest = new VerifyRequest(
                            sign.getInputStream(),
                            new ByteArrayInputStream(data)
                    );
                    assertDoesNotThrow(() -> cmsService.verify(verifyRequest));
                }
        );
    }

    @Test
    void signAndVerifyMedium() {
        byte[] data = genFile(mediumFileSize);
        SignatureParams params = SignatureParams.builder()
                .encodeToB64(false)
                .detached(false)
                .build();
        assertDoesNotThrow(() -> {
                    FileStreamWrapper sign = cmsService.sign(new ByteArrayInputStream(data), params);
                    VerifyRequest verifyRequest = new VerifyRequest(
                            sign.getInputStream(),
                            new ByteArrayInputStream(data)
                    );
                    assertDoesNotThrow(() -> cmsService.verify(verifyRequest));
                }
        );
    }

    @Test
    void signAndVerifyBig() {
        byte[] data = genFile(bigFileSize);
        SignatureParams params = SignatureParams.builder()
                .encodeToB64(false)
                .detached(true)
                .build();
        assertDoesNotThrow(() -> {
                    FileStreamWrapper sign = cmsService.sign(new ByteArrayInputStream(data), params);
                    VerifyRequest verifyRequest = new VerifyRequest(
                            sign.getInputStream(),
                            new ByteArrayInputStream(data)
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

    private byte[] encryptDecryptAndHash(byte[] data) throws Exception {
        return encryptDecryptAndHash(data, null);
    }

    private byte[] encryptDecryptAndHash(byte[] data, EncryptionKeyAlgorithm algorithm) throws Exception {
        FileStreamWrapper encoded = cmsService.encrypt(new ByteArrayInputStream(data), Collections.emptyList(), algorithm, false);
        FileStreamWrapper decrypted = cmsService.decrypt(encoded.getInputStream());
        return GOSTHash.computeHash(decrypted.getInputStream());
    }
}
