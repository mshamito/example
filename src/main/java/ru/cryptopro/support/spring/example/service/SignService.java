package ru.cryptopro.support.spring.example.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.CryptoPro.AdES.Options;
import ru.CryptoPro.CAdES.*;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.exception.EnvelopedException;
import ru.CryptoPro.CAdES.exception.EnvelopedInvalidRecipientException;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.cryptopro.support.spring.example.config.StoreConfig;
import ru.cryptopro.support.spring.example.dto.SignatureParams;
import ru.cryptopro.support.spring.example.dto.VerifyRequest;
import ru.cryptopro.support.spring.example.dto.VerifyResult;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.utils.CadesTypeHelper;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SignService {
    private final StoreConfig storeConfig;
    private final CertService certService;
    final X509Certificate certificate;
    final PrivateKey privateKey;
    private final int BUFFER_SIZE = 16 * 1024 * 1024;
    private final int BAOS_SIZE = 16 * 1024;

    public SignService(
            StoreConfig storeConfig,
            CertService certService,
            @Qualifier("cert") X509Certificate certificate,
            @Qualifier("key") PrivateKey privateKey
    ) {
        this.storeConfig = storeConfig;
        this.certService = certService;
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public ByteArrayOutputStream encrypt(InputStream data, List<X509Certificate> certs) throws Exception {
        EnvelopedSignature envelopedSignature = new EnvelopedSignature(EncryptionKeyAlgorithm.ekaKuznechik);
        if (certs.isEmpty()) {
            envelopedSignature.addKeyAgreeRecipient(certificate);
        } else {
            for (X509Certificate walk : certs)
                envelopedSignature.addKeyAgreeRecipient(walk);
        }

        ByteArrayOutputStream enveloped = new ByteArrayOutputStream(BAOS_SIZE);
        try (OutputStream wrapped = EncodingHelper.encodeStream(enveloped)) {
            envelopedSignature.open(wrapped);
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = data.read(buffer)) != -1) {
                envelopedSignature.update(buffer, 0, read);
            }
            envelopedSignature.close();
            return enveloped;
        }
    }

    public ByteArrayOutputStream decrypt(InputStream encryptedCms) throws EnvelopedException, EnvelopedInvalidRecipientException, IOException {
        InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(encryptedCms);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BAOS_SIZE);
        EnvelopedSignature envelopedSignature = new EnvelopedSignature(tryToGuess);
        envelopedSignature.decrypt(certificate, privateKey, byteArrayOutputStream);
        tryToGuess.close();
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream sign(InputStream data, SignatureParams params) throws CAdESException, IOException {
        String digestOid = AlgorithmUtility.keyAlgToDigestOid(privateKey.getAlgorithm());
        String keyOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm());
        int cadesType = CadesTypeHelper.mapValue(params.getType());
        String tsp = params.getTsp();
        if (Strings.isBlank(tsp) && (cadesType == CAdESType.CAdES_T || cadesType == CAdESType.CAdES_X_Long_Type_1))
            throw new CryptographicException("Tsp address is empty");

        CAdESSignature cAdESSignature = new CAdESSignature(params.isDetached());
        if (cadesType == CAdESType.CAdES_BES || cadesType == CAdESType.CAdES_T)
            cAdESSignature.setOptions(new Options().disableCertificateValidation());
        cAdESSignature.addSigner(
                storeConfig.getKeyStore().getProvider().getName(),
                digestOid,
                keyOid,
                privateKey,
                Collections.singletonList(certificate),
                cadesType, // signature type
                tsp, // tsp address
                false, // countersign
                null, // signed attributes
                null, // unsigned attributes
                Collections.emptySet(), // set of crl
                true // add chain
        );

        ByteArrayOutputStream signature = new ByteArrayOutputStream(BAOS_SIZE);
        try (OutputStream wrapped = EncodingHelper.encodeStream(signature)) {
            cAdESSignature.open(wrapped);
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = data.read(buffer)) != -1) {
                cAdESSignature.update(buffer, 0, read);
            }
            cAdESSignature.close();
            data.close();
            return signature;
        }
    }

    public List<VerifyResult> verify(VerifyRequest request) throws CAdESException, IOException {
        List<VerifyResult> results = new ArrayList<>();
        InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(request.getSign());
        CAdESSignature signature = new CAdESSignature(tryToGuess, request.getData(), null);
        signature.verify(null, null); // no exception ? everything is ok.
        CAdESSigner[] signers = signature.getCAdESSignerInfos();
        for (int i = 0; i < signers.length; i++) {
            CAdESSigner signer = signers[i];
            VerifyResult result = new VerifyResult();
            result.setId(i);
            result.setCAdESType(CadesTypeHelper.mapValue(
                    signer.getSignatureType()
            ));
            X509Certificate cert = signer.getSignerCertificate();
            result.setSubjectDN(cert.getSubjectX500Principal());
            result.setIssuerDN(cert.getIssuerX500Principal());
            result.setNotBefore(cert.getNotBefore());
            result.setNotAfter(cert.getNotAfter());

            results.add(result);
        }
        tryToGuess.close();
        request.getData().close();
        return results;
    }
}