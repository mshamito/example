package ru.cryptopro.support.spring.example.service;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;
import ru.cryptopro.support.spring.example.utils.StreamUpdateHelper;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.X509Certificate;

@Service
public class RawService {
    final X509Certificate certificate;
    final PrivateKey privateKey;

    public RawService(
            @Qualifier("cert") X509Certificate certificate,
            @Qualifier("key") PrivateKey privateKey
    ) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public byte[] sign(InputStream data, boolean encodeToB64, boolean invert) throws NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
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

    public boolean verify(InputStream data, byte[] sign, X509Certificate cert, boolean invert) throws InvalidKeyException, NoSuchAlgorithmException, IOException, SignatureException {
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
