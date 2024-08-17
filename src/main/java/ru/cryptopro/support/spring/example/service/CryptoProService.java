package ru.cryptopro.support.spring.example.service;

import org.apache.commons.lang3.ArrayUtils;
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
import ru.cryptopro.support.spring.example.utils.CAdESTypeHelper;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;
import ru.cryptopro.support.spring.example.utils.StreamUpdateHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CryptoProService {
    private final StoreConfig storeConfig;
    final X509Certificate certificate;
    final PrivateKey privateKey;
    private final int BAOS_SIZE = 16 * 1024;

    public CryptoProService(
            StoreConfig storeConfig,
            @Qualifier("cert") X509Certificate certificate,
            @Qualifier("key") PrivateKey privateKey
    ) {
        this.storeConfig = storeConfig;
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public ByteArrayOutputStream encrypt(InputStream data, List<X509Certificate> certs, boolean encodeToB64) throws Exception {
        EnvelopedSignature envelopedSignature = new EnvelopedSignature(EncryptionKeyAlgorithm.ekaKuznechik);
        if (certs.isEmpty()) {
            // no certs provided. cert from alias will be used as recipient
            envelopedSignature.addKeyAgreeRecipient(certificate);
        } else {
            for (X509Certificate walk : certs)
                envelopedSignature.addKeyAgreeRecipient(walk);
        }

        try (
                InputStream inputStream = data;
                ByteArrayOutputStream enveloped = new ByteArrayOutputStream(BAOS_SIZE)
        ) {
            // DER output
            if (!encodeToB64) {
                envelopedSignature.open(enveloped);
                StreamUpdateHelper.streamUpdateEnvelopedSignature(inputStream, envelopedSignature);
                return enveloped;
            }

            // base64 output
            try (
                    OutputStream wrapped = EncodingHelper.encodeStream(enveloped)
            ) {
                envelopedSignature.open(wrapped);
                StreamUpdateHelper.streamUpdateEnvelopedSignature(inputStream, envelopedSignature);
                return enveloped;
            }
        }


    }

    public ByteArrayOutputStream decrypt(InputStream encryptedCms) throws EnvelopedException, EnvelopedInvalidRecipientException, IOException {
        try (
                InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(encryptedCms);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BAOS_SIZE)
        ) {
            EnvelopedSignature envelopedSignature = new EnvelopedSignature(tryToGuess);
            envelopedSignature.decrypt(certificate, privateKey, byteArrayOutputStream);
            return byteArrayOutputStream;
        }
    }

    public ByteArrayOutputStream sign(InputStream data, SignatureParams params) throws CAdESException, IOException {
        String digestOid = AlgorithmUtility.keyAlgToDigestOid(privateKey.getAlgorithm());
        String keyOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm());
        int signatureType = CAdESTypeHelper.mapValue(params.getType());
        String tsp = params.getTsp();
        if (Strings.isBlank(tsp) && (signatureType == CAdESType.CAdES_T || signatureType == CAdESType.CAdES_X_Long_Type_1))
            throw new CryptographicException("Tsp address is empty");

        CAdESSignature cAdESSignature = new CAdESSignature(params.isDetached());
        if (signatureType == CAdESType.CAdES_BES || signatureType == CAdESType.CAdES_T)
            cAdESSignature.setOptions(new Options().disableCertificateValidation());
        cAdESSignature.addSigner(
                storeConfig.getKeyStore().getProvider().getName(),
                digestOid,
                keyOid,
                privateKey,
                Collections.singletonList(certificate),
                signatureType, // signature type
                tsp, // tsp address
                false, // countersign
                null, // signed attributes
                null, // unsigned attributes
                Collections.emptySet(), // set of crl
                true // add chain
        );

        boolean encodeToB64 = params.isEncodeToB64();
        try (
                InputStream inputStream = data;
                ByteArrayOutputStream signature = new ByteArrayOutputStream(BAOS_SIZE)
        ) {
            // DER output
            if (!encodeToB64) {
                cAdESSignature.open(signature);
                StreamUpdateHelper.streamUpdateCAdESSignature(inputStream, cAdESSignature);
                return signature;
            }

            // base64 output
            try (
                    OutputStream wrapped = EncodingHelper.encodeStream(signature)
            ) {
                cAdESSignature.open(wrapped);
                StreamUpdateHelper.streamUpdateCAdESSignature(inputStream, cAdESSignature);
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
            signature.verify(null, null); // no exception ? everything is ok.
            CAdESSigner[] signers = signature.getCAdESSignerInfos();
            for (int i = 0; i < signers.length; i++) {
                CAdESSigner signer = signers[i];
                VerifyResult result = new VerifyResult();
                result.setId(i);
                result.setCAdESType(CAdESTypeHelper.mapValue(
                        signer.getSignatureType()
                ));
                X509Certificate cert = signer.getSignerCertificate();
                result.setSubjectDN(cert.getSubjectX500Principal());
                result.setIssuerDN(cert.getIssuerX500Principal());
                result.setNotBefore(cert.getNotBefore());
                result.setNotAfter(cert.getNotAfter());

                results.add(result);
            }
        }
        return results;
    }

    public byte[] signRaw(InputStream data, boolean encodeToB64, boolean invert) throws NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
        String signOid = AlgorithmUtility.keyAlgToSignatureOid(privateKey.getAlgorithm());
        String signatureAlgorithm = AlgorithmUtility.signOidToSignatureAlgorithm(signOid);
        Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initSign(privateKey);
        StreamUpdateHelper.streamUpdateRawSignature(data, signature);
        byte[] sign = signature.sign();
        if (invert)
            ArrayUtils.reverse(sign);
        if (encodeToB64)
            return EncodingHelper.encode(sign);
        return sign;
    }

    public boolean verifyRaw(InputStream data, byte[] sign, X509Certificate cert, boolean invert) throws InvalidKeyException, NoSuchAlgorithmException, IOException, SignatureException {
        String signOid = AlgorithmUtility.keyAlgToSignatureOid(cert.getPublicKey().getAlgorithm());
        String signatureAlgorithm = AlgorithmUtility.signOidToSignatureAlgorithm(signOid);
        Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initVerify(cert.getPublicKey());
        StreamUpdateHelper.streamUpdateRawSignature(data, signature);
        if (invert)
            ArrayUtils.reverse(sign);
        return signature.verify(sign);
    }
}