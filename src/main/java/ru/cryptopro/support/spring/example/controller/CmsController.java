package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.exception.EnvelopedException;
import ru.CryptoPro.CAdES.exception.EnvelopedInvalidRecipientException;
import ru.cryptopro.support.spring.example.config.CertConfig;
import ru.cryptopro.support.spring.example.dto.SignatureParams;
import ru.cryptopro.support.spring.example.dto.VerifyRequest;
import ru.cryptopro.support.spring.example.dto.VerifyResult;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.expection.ProvidedDataException;
import ru.cryptopro.support.spring.example.service.CmsService;
import ru.cryptopro.support.spring.example.utils.CastX509Helper;
import ru.cryptopro.support.spring.example.utils.HeadersHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
@CrossOrigin
@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public class CmsController {
    private final CmsService cmsService;
    private final CertConfig certConfig;

    @PostMapping(value = "${app.controller.encrypt}")
    public ResponseEntity<StreamingResponseBody> encrypt(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false, value = "cert") List<MultipartFile> certs,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean encodeToB64
    ) {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        List<X509Certificate> x509Certificates = certs == null ?
                Collections.singletonList(certConfig.getCertificate()) :
                CastX509Helper.castCertificates(certs);

        HttpHeaders headers = HeadersHelper.prepareHeaders(data.getOriginalFilename(), ".enc");
        MediaType mediaType = encodeToB64 ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        try (
                InputStream inputStream = data.getInputStream();
                ByteArrayOutputStream enveloped = cmsService.encrypt(inputStream, x509Certificates, encodeToB64)
        ) {
            StreamingResponseBody response = enveloped::writeTo;
            return ResponseEntity.ok().headers(headers).contentType(mediaType).body(response);
        } catch (Exception e) {
            throw new CryptographicException("Encrypt failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "${app.controller.decrypt}")
    public ResponseEntity<StreamingResponseBody> decrypt(
            @RequestParam(value = "cms") MultipartFile encryptedCms
    ) throws IOException {
        if (encryptedCms.isEmpty())
            throw new ProvidedDataException("Provided data is empty");

        HttpHeaders headers = HeadersHelper.prepareHeaders(encryptedCms.getOriginalFilename(), ".decrypted");

        try (
                InputStream inputStream = encryptedCms.getInputStream();
                ByteArrayOutputStream decrypted = cmsService.decrypt(inputStream)
        ) {
            StreamingResponseBody response = decrypted::writeTo;
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(response);
        } catch (EnvelopedException | EnvelopedInvalidRecipientException e) {
            throw new CryptographicException("Decrypt failed: " + e.getMessage());
        }
    }

    @PostMapping("${app.controller.sign}")
    public ResponseEntity<StreamingResponseBody> sign(
            @RequestParam MultipartFile data,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean detached,
            @RequestParam(required = false) @Schema(defaultValue = "http://testca2012.cryptopro.ru/tsp/tsp.srf") String tsp,
            @RequestParam(required = false) @Schema(defaultValue = "bes") String type,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean encodeToB64
    ) {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");

        SignatureParams.SignatureParamsBuilder builder = SignatureParams.builder();
        builder.detached(detached);
        builder.encodeToB64(encodeToB64);
        if (Strings.isNotBlank(tsp))
            builder.tsp(tsp);
        if (Strings.isNotBlank(type))
            builder.type(type);
        SignatureParams params = builder.build();

        HttpHeaders headers = HeadersHelper.prepareHeaders(data.getOriginalFilename(), ".sig");
        MediaType mediaType = encodeToB64 ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        try (
                InputStream inputStream = data.getInputStream();
                ByteArrayOutputStream signature = cmsService.sign(inputStream, params)
        ) {
            StreamingResponseBody response = signature::writeTo;
            return ResponseEntity.ok().headers(headers).contentType(mediaType).body(response);
        } catch (CAdESException | IOException e) {
            throw new CryptographicException("Sign failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "${app.controller.verify}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VerifyResult> verify(
            @RequestParam MultipartFile sign,
            @RequestParam(required = false) MultipartFile data
    ) {
        try {
            return cmsService.verify(new VerifyRequest(sign, data));
        } catch (CAdESException | IOException e) {
            throw new CryptographicException(e.getMessage());
        }
    }
}