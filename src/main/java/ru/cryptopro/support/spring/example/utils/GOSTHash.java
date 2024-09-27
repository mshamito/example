package ru.cryptopro.support.spring.example.utils;

import lombok.SneakyThrows;
import ru.CryptoPro.JCP.JCP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class GOSTHash {
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    @SneakyThrows
    public static byte[] computeHash(InputStream inputStream) {
        MessageDigest digest = MessageDigest.getInstance(JCP.GOST_DIGEST_2012_256_NAME);
        int read;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((read = inputStream.read(buffer)) != -1)
            digest.update(buffer, 0, read);
        return digest.digest();
    }

    @SneakyThrows
    public static byte[] computeHash(byte[] bytes) {
        return computeHash(new ByteArrayInputStream(bytes));
    }
}
