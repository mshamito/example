package ru.cryptopro.support.spring.example.service;

import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.CollectionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.CryptoPro.AdES.Options;
import ru.CryptoPro.CAdES.*;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.exception.EnvelopedException;
import ru.CryptoPro.CAdES.exception.EnvelopedInvalidRecipientException;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.cryptopro.support.spring.example.config.StoreConfig;
import ru.cryptopro.support.spring.example.dto.CertDto;
import ru.cryptopro.support.spring.example.dto.SignatureParams;
import ru.cryptopro.support.spring.example.dto.VerifyRequest;
import ru.cryptopro.support.spring.example.dto.VerifyResult;
import ru.cryptopro.support.spring.example.exception.CryptographicException;
import ru.cryptopro.support.spring.example.utils.CAdESTypeHelper;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;
import ru.cryptopro.support.spring.example.utils.FileStreamWrapper;
import ru.cryptopro.support.spring.example.utils.StreamUpdateHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class CmsService {
    private final StoreConfig storeConfig;
    private final X509Certificate certificate;
    private final PrivateKey privateKey;
    private final Set<X509CRL> localCRLs;

    public CmsService(
            StoreConfig storeConfig,
            @Qualifier("cert") X509Certificate certificate,
            @Qualifier("key") PrivateKey privateKey,
            CrlService crlService
    ) {
        this.storeConfig = storeConfig;
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.localCRLs = crlService.getLocalCRLs();
    }

    public FileStreamWrapper encrypt(InputStream data, List<X509Certificate> certs, EncryptionKeyAlgorithm algorithm, boolean encodeToB64) throws Exception {
        EncryptionKeyAlgorithm encryptionKeyAlgorithm = algorithm == null ? EncryptionKeyAlgorithm.ekaKuznechik : algorithm;
        EnvelopedSignature envelopedSignature = new EnvelopedSignature(encryptionKeyAlgorithm);
        if (certs.isEmpty()) {
            // no certs provided. cert from alias will be used as recipient
            envelopedSignature.addKeyAgreeRecipient(certificate);
        } else {
            for (X509Certificate walk : certs)
                envelopedSignature.addKeyAgreeRecipient(walk);
        }

        File file = File.createTempFile("encrypt-", ".enc");
        FileStreamWrapper enveloped = new FileStreamWrapper(file);
        try (
                InputStream inputStream = data
        ) {
            // DER output
            if (!encodeToB64) {
                envelopedSignature.open(enveloped.getOutputStream());
                StreamUpdateHelper.streamUpdateEnvelopedSignature(inputStream, envelopedSignature);
                envelopedSignature.close();
                return enveloped;
            }

            // base64 output
            try (
                    OutputStream wrapped = EncodingHelper.encodeStream(enveloped.getOutputStream())
            ) {
                envelopedSignature.open(wrapped);
                StreamUpdateHelper.streamUpdateEnvelopedSignature(inputStream, envelopedSignature);
                envelopedSignature.close();
                return enveloped;
            }
        }
    }

    public FileStreamWrapper decrypt(InputStream encryptedCms) throws EnvelopedException, EnvelopedInvalidRecipientException, IOException {
        File file = File.createTempFile("decrypt-", ".bin");
        FileStreamWrapper streamWrapper = new FileStreamWrapper(file);
        try (
                InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(encryptedCms);
                OutputStream outputStream = streamWrapper.getOutputStream()
        ) {
            EnvelopedSignature envelopedSignature = new EnvelopedSignature(tryToGuess);
            envelopedSignature.decrypt(certificate, privateKey, outputStream);
            return streamWrapper;
        }
    }

    public FileStreamWrapper sign(InputStream data, SignatureParams params) throws CAdESException, IOException, CertificateEncodingException {
        File file = File.createTempFile("sign-", ".sig");
        FileStreamWrapper signature = new FileStreamWrapper(file);
        String digestOid = AlgorithmUtility.keyAlgToDigestOid(privateKey.getAlgorithm());
        String keyOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm());
        int signatureType = CAdESTypeHelper.mapValue(params.getType());
        String tsp = params.getTsp();
        if (Strings.isBlank(tsp) && (signatureType == CAdESType.CAdES_T || signatureType == CAdESType.CAdES_X_Long_Type_1))
            throw new CryptographicException("Tsp address is empty");

        CAdESSignature cAdESSignature = new CAdESSignature(params.isDetached());
        Set<X509CRL> crlSet;
        if (signatureType == CAdESType.CAdES_BES || signatureType == CAdESType.CAdES_T) {
            cAdESSignature.setOptions(new Options().disableCertificateValidation());
            crlSet = null;
        } else {
            crlSet = localCRLs;
        }
        boolean addCertChainToSign = params.isAddChain();
        cAdESSignature.addSigner(
                storeConfig.getProviderName(),
                digestOid,
                keyOid,
                privateKey,
                Collections.singletonList(certificate),
                signatureType, // signature type
                tsp, // tsp address
                false, // countersign
                null, // signed attributes
                null, // unsigned attributes
                crlSet, // set of crl
                addCertChainToSign // add chain
        );

        if (!addCertChainToSign) { // add only one certificate
            Collection<X509CertificateHolder> certificateHolders = new ArrayList<>();
            certificateHolders.add(new X509CertificateHolder(certificate.getEncoded()));
            CollectionStore<X509CertificateHolder> store = new CollectionStore<>(certificateHolders);
            cAdESSignature.setCertificateStore(store);
        }

        boolean encodeToB64 = params.isEncodeToB64();
        try (
                InputStream inputStream = data
        ) {
            // DER output
            if (!encodeToB64) {
                cAdESSignature.open(signature.getOutputStream());
                StreamUpdateHelper.streamUpdateCAdESSignature(inputStream, cAdESSignature);
                cAdESSignature.close();
                return signature;
            }

            // base64 output
            try (
                    OutputStream wrapped = EncodingHelper.encodeStream(signature.getOutputStream())
            ) {
                cAdESSignature.open(wrapped);
                StreamUpdateHelper.streamUpdateCAdESSignature(inputStream, cAdESSignature);
                cAdESSignature.close();
                return signature;
            }
        }
    }

    public List<VerifyResult> verify(VerifyRequest request) throws CAdESException, IOException {
        List<VerifyResult> results = new ArrayList<>();
        try (
                InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(request.getSign());
                InputStream dataStream = request.getData()
        ) {
            CAdESSignature signature = new CAdESSignature(tryToGuess, dataStream, null);
            signature.verify(null, localCRLs); // no exception ? everything is ok.
            CAdESSigner[] signers = signature.getCAdESSignerInfos();
            for (int i = 0; i < signers.length; i++) {
                CAdESSigner signer = signers[i];
                VerifyResult result = new VerifyResult();
                result.setId(i);
                result.setCAdESType(CAdESTypeHelper.mapValue(
                        signer.getSignatureType()
                ));
                X509Certificate cert = signer.getSignerCertificate();
                result.setCert(new CertDto(cert));
                results.add(result);
            }
        }
        return results;
    }
}