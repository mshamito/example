package ru.cryptopro.support.spring.example.utils;

import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.exception.CryptographicException;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CastX509Helper {
    public static List<X509Certificate> castCertificates(List<MultipartFile> files) {
        List<X509Certificate> result = new ArrayList<>();
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            for (MultipartFile file : files) {
                X509Certificate cert = (X509Certificate) factory.generateCertificate(EncodingHelper.decodeDerOrB64Stream(file.getInputStream()));
                result.add(cert);
            }
        } catch (CertificateException | IOException e) {
            throw new CryptographicException("Certificate cast failed: " + e.getMessage());
        }
        return result;
    }
    public static X509Certificate castCertificate(MultipartFile file) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(file.getInputStream());
            return (X509Certificate) factory.generateCertificate(tryToGuess);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
