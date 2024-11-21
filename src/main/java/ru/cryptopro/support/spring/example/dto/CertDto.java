package ru.cryptopro.support.spring.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ru.CryptoPro.reprov.x509.X500Name;
import ru.cryptopro.support.spring.example.exception.CryptographicException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

@Log4j2
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertDto {
    @JsonIgnore
    @Getter(onMethod_ = @__(@JsonIgnore))
    private final X509Certificate cert;
    private final String thumbprint;
    private final String keyAlg;
    private final String serial;
    private final Map<String, String> subject;
    private final Map<String, String> issuer;
    private final String[] dns;
    private final Date notBefore;
    private final Date notAfter;
    private final String base64;

    public CertDto(X509Certificate cert) {
        this.cert = cert;
        try {
            this.thumbprint = getThumbprintFromCert(cert);
            this.serial = new BigInteger(cert.getSerialNumber().toByteArray()).toString(16);
            this.keyAlg = cert.getPublicKey().getAlgorithm();
            this.subject = parseRdns(new X500Name(cert.getSubjectX500Principal().getName()).rdns());
            this.issuer = parseRdns(new X500Name(cert.getIssuerX500Principal().getName()).rdns());
            this.dns = parseHostNames(cert);
            this.notBefore = cert.getNotBefore();
            this.notAfter = cert.getNotAfter();
            this.base64 = Base64.getEncoder().encodeToString(cert.getEncoded());
        } catch (CertificateEncodingException | IOException | NoSuchAlgorithmException e) {
            throw new CryptographicException(e.getMessage());
        }

    }

    private String getThumbprintFromCert(X509Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(cert.getEncoded());
        return new BigInteger(1, md.digest()).toString(16);
    }

    private Map<String, String> parseRdns(List<?> list) {
        Map<String, String> parsedMap = new HashMap<>();
        for (Object walk : list) {
            String tmp = walk.toString();
            if (!tmp.contains("="))
                continue;
            String[] split = tmp.split("=", 2);
            parsedMap.put(
                    split[0], // key
                    split[1]  // value
                            .replaceAll("^\"|\"$", "")
                            .replaceAll("\\\\\"", "\"")
            );
        }
        return parsedMap;
    }

    private String[] parseHostNames(X509Certificate cert) {
        List<String> hostNameList = new ArrayList<>();
        try {
            Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
            if (altNames == null)
                return null;
            for (List<?> altName : altNames) {
                if (altName.size() < 2) continue;
                // https://docs.oracle.com/javase/8/docs/api/java/security/cert/X509Certificate.html
                switch ((Integer) altName.get(0)) {
                    case 2, 7 -> { // 2 - DnsName, 7 - IPAddress
                        Object data = altName.get(1);
                        if (data instanceof String) {
                            hostNameList.add(((String) data));
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (CertificateParsingException e) {
            throw new CryptographicException("Can't parse hostNames from this cert.");
        }
        return hostNameList.toArray(new String[0]);
    }
}
