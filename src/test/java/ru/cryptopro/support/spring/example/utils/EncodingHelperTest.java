package ru.cryptopro.support.spring.example.utils;

import org.junit.jupiter.api.Test;
import ru.CryptoPro.CAdES.CAdESSignature;

import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EncodingHelperTest {
    final String TEXT = "Test content";
    final String B64 = "VGVzdCBjb250ZW50";
    final int BUFFER_SIZE = 16;

    @Test
    void encodeStream() throws IOException {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStream encoded = EncodingHelper.encodeStream(baos)
        ) {
            encoded.write(TEXT.getBytes());
            assertEquals(B64, baos.toString());
        }
    }

    @Test
    void decodeNormalBytes() {
        // DER
        try (
                ByteArrayInputStream beforeStream = new ByteArrayInputStream(TEXT.getBytes());
                InputStream afterStream = EncodingHelper.decodeDerOrB64Stream(beforeStream);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = afterStream.read(buffer)) != -1)
                out.write(buffer, 0, read);

            assertEquals(TEXT, out.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void decodeB64StreamCert() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        // BASE64
        try (
                InputStream beforeStream = classLoader.getResourceAsStream("Base64.cer");
                InputStream afterStream = EncodingHelper.decodeDerOrB64Stream(beforeStream);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = afterStream.read(buffer)) != -1)
                out.write(buffer, 0, read);

            CertificateFactory factory = CertificateFactory.getInstance("X509");
            try (ByteArrayInputStream stream = new ByteArrayInputStream(out.toByteArray())) {
                assertDoesNotThrow(() -> factory.generateCertificate(stream));
            }
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void decodeStreamSign() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        byte[] decoded;
        // BASE64
        try (
                InputStream beforeStream = classLoader.getResourceAsStream("Base64Encoded.sig");
                InputStream afterStream = EncodingHelper.decodeDerOrB64Stream(beforeStream);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = afterStream.read(buffer)) != -1)
                out.write(buffer, 0, read);
            decoded = out.toByteArray();

            assertDoesNotThrow(() -> new CAdESSignature(decoded, null, null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // DER
        try (
                InputStream beforeStream = new ByteArrayInputStream(decoded);
                InputStream afterStream = EncodingHelper.decodeDerOrB64Stream(beforeStream);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = afterStream.read(buffer)) != -1)
                out.write(buffer, 0, read);

            assertDoesNotThrow(() -> new CAdESSignature(out.toByteArray(), null, null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}