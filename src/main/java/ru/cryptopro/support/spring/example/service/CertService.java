package ru.cryptopro.support.spring.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CertService {
    public List<X509Certificate> generateCertificate(List<MultipartFile> files) {
        List<X509Certificate> result = new ArrayList<>();
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            for (MultipartFile file : files) {
                X509Certificate cert = (X509Certificate) factory.generateCertificate(file.getInputStream());
                result.add(cert);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
