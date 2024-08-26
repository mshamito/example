package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.expection.ProvidedDataException;
import ru.cryptopro.support.spring.example.service.RawService;
import ru.cryptopro.support.spring.example.utils.CastX509Helper;
import ru.cryptopro.support.spring.example.utils.EncodingHelper;
import ru.cryptopro.support.spring.example.utils.HeadersHelper;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

@SuppressWarnings("unused")
@RestController
@RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public class RawController {
    private final X509Certificate certificate;
    private final RawService rawService;

    public RawController(
            RawService rawService,
            @Qualifier("cert")
            X509Certificate certificate
    ) {
        this.rawService = rawService;
        this.certificate = certificate;
    }

    @PostMapping(value = "${app.controller.raw-sign}")
    public ResponseEntity<byte[]> sign(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean encodeToB64,
            @RequestParam(required = false, defaultValue = "false") @Schema(defaultValue = "false", type = "boolean") boolean invert
    ) {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        HttpHeaders headers = HeadersHelper.prepareHeaders(data.getOriginalFilename(), ".bin");
        MediaType mediaType = encodeToB64 ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;
        try {
            byte[] sign = rawService.sign(data.getInputStream(), encodeToB64, invert);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(sign);
        } catch (NoSuchAlgorithmException | IOException | SignatureException | InvalidKeyException e) {
            throw new CryptographicException(e.getMessage());
        }
    }

    @PostMapping(value = "${app.controller.raw-verify}")
    public ResponseEntity<String> rawVerify(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false) MultipartFile cert,
            @RequestParam(required = false) MultipartFile signBinary,
            @RequestParam(required = false) String signBase64,
            @RequestParam(required = false, defaultValue = "false") @Schema(defaultValue = "false", type = "boolean") boolean invert
    ) {
        if (
                signBinary == null && Strings.isEmpty(signBase64) || data == null
        )
            throw new ProvidedDataException("Provided data is empty");

        X509Certificate x509Certificate = cert == null ? certificate : CastX509Helper.castCertificate(cert);
        try {
            byte[] sign = Strings.isEmpty(signBase64) ? signBinary.getBytes() : EncodingHelper.decode(signBase64);
            boolean result = rawService.verify(data.getInputStream(), sign, x509Certificate, invert);
            return ResponseEntity.ok(result ? "true" : " false");
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | SignatureException e) {
            throw new CryptographicException(e.getMessage());
        }
    }
}
