package ru.cryptopro.support.spring.example.utils;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.EnvelopedSignature;
import ru.CryptoPro.CAdES.exception.CAdESException;

import java.io.IOException;
import java.io.InputStream;
import java.security.Signature;
import java.security.SignatureException;

public class StreamUpdateHelper {
    private final static int BUFFER_SIZE = 2 * 1024 * 1024;

    public static void streamUpdateCAdESSignature(InputStream inputStream, CAdESSignature signature) throws CAdESException, IOException {
        int read;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((read = inputStream.read(buffer)) != -1) {
            signature.update(buffer, 0, read);
        }
        inputStream.close();
    }

    public static void streamUpdateEnvelopedSignature(InputStream inputStream, EnvelopedSignature signature) throws Exception {
        int read;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((read = inputStream.read(buffer)) != -1) {
            signature.update(buffer, 0, read);
        }
        inputStream.close();
    }

    public static void streamUpdateRawSignature(InputStream inputStream, Signature signature) throws IOException, SignatureException {
        int read;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((read = inputStream.read(buffer)) != -1) {
            signature.update(buffer, 0, read);
        }
        inputStream.close();
    }
}
