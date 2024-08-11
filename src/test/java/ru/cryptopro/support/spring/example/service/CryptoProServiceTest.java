package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class CryptoProServiceTest {

    final int smallFileSize = 8 * 1024 * 1024;
    final int mediumFileSize = 25 * 1024 * 1024;
    final int bigFileSize = 50 * 1024 * 1024;

    @Autowired
    CryptoProService cryptoProService;

    @Test
    void encryptionSmall() throws Exception {
        byte[] small = genFile(smallFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(small));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cryptoProService.encrypt(new ByteArrayInputStream(small), Collections.emptyList(), false).toByteArray();
                    byte[] decrypted = cryptoProService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionMedium() throws Exception {
        byte[] medium = genFile(mediumFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(medium));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cryptoProService.encrypt(new ByteArrayInputStream(medium), Collections.emptyList(), false).toByteArray();
                    byte[] decrypted = cryptoProService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
                    byte[] hash = gostHash(decrypted);
                    assertArrayEquals(asyncDataHash.get(), hash);
                }
        );
    }

    @Test
    void encryptionBig() throws Exception {
        byte[] big = genFile(bigFileSize);
        CompletableFuture<byte[]> asyncDataHash = CompletableFuture.supplyAsync(() -> gostHash(big));
        assertDoesNotThrow(() -> {
                    byte[] encoded = cryptoProService.encrypt(new ByteArrayInputStream(big), Collections.emptyList(), false).toByteArray();
                    byte[] decrypted = cryptoProService.decrypt(new ByteArrayInputStream(encoded)).toByteArray();
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
                    byte[] sign = cryptoProService.sign(new ByteArrayInputStream(small), params).toByteArray();
                    VerifyRequest verifyRequest = new VerifyRequest(
                            new ByteArrayInputStream(sign),
                            new ByteArrayInputStream(small)
                    );
                    assertDoesNotThrow(() -> cryptoProService.verify(verifyRequest));
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
                    byte[] sign = cryptoProService.sign(new ByteArrayInputStream(medium), params).toByteArray();
                    VerifyRequest verifyRequest = new VerifyRequest(
                            new ByteArrayInputStream(sign),
                            new ByteArrayInputStream(medium)
                    );
                    assertDoesNotThrow(() -> cryptoProService.verify(verifyRequest));
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
                    byte[] sign = cryptoProService.sign(new ByteArrayInputStream(big), params).toByteArray();
                    VerifyRequest verifyRequest = new VerifyRequest(
                            new ByteArrayInputStream(sign),
                            new ByteArrayInputStream(big)
                    );
                    assertDoesNotThrow(() -> cryptoProService.verify(verifyRequest));
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
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(JCP.GOST_DIGEST_2012_256_NAME);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return digest.digest(data);
    }
}
