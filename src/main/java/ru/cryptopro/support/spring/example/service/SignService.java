package ru.cryptopro.support.spring.example.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.CryptoPro.AdES.Options;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.CAdES.EnvelopedSignature;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.cryptopro.support.spring.example.dto.*;
import ru.cryptopro.support.spring.example.config.StoreConfig;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.utils.CadesTypeHelper;

import java.io.ByteArrayOutputStream;
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

    public CmsDto encrypt(DataDto data, List<MultipartFile> certs) {
        try {
            EnvelopedSignature envelopedSignature = new EnvelopedSignature();
            if (certs.isEmpty()) {
                envelopedSignature.addKeyAgreeRecipient(certificate);
            } else {
                List<X509Certificate> certificateList = certService.generateCertificate(certs);
                for (X509Certificate walk : certificateList)
                    envelopedSignature.addKeyAgreeRecipient(walk);
            }

            ByteArrayOutputStream cms = new ByteArrayOutputStream();
            envelopedSignature.open(cms);
            envelopedSignature.update(data.getData());
            envelopedSignature.close();
            return new CmsDto(cms.toByteArray());
        } catch (Exception e) {
            throw new CryptographicException(e.getMessage());
        }
    }

    public CmsDto sign(DataDto data, SignatureParams params) {
        try {
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

            ByteArrayOutputStream signature = new ByteArrayOutputStream();
            cAdESSignature.open(signature);
            cAdESSignature.update(data.getData());
            cAdESSignature.close();
            return new CmsDto(signature.toByteArray(), params);

        } catch (CAdESException e) {
            throw new CryptographicException(e.getMessage());
        }
    }

    public List<VerifyResult> verify(VerifyRequest request) {
        CAdESSignature signature;
        List<VerifyResult> results = new ArrayList<>();
        try {
            signature = new CAdESSignature(request.getSign(), request.getData(), null);
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
                result.setSubjectDN(cert.getSubjectDN());
                result.setIssuerDN(cert.getIssuerDN());
                result.setNotBefore(cert.getNotBefore());
                result.setNotAfter(cert.getNotAfter());

                results.add(result);
            }
        } catch (CAdESException e) {
            throw new CryptographicException(e.getMessage());
        }
        return results;
    }
}