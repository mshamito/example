package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
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
import ru.cryptopro.support.spring.example.service.CryptoProService;
import ru.cryptopro.support.spring.example.utils.HeadersHelper;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

@RestController
@RequiredArgsConstructor
@RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public class RawController {
    private final CryptoProService cryptoProService;

    @PostMapping(value = "${app.controller.raw-sign}")
    public ResponseEntity<?> rawSign(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean encodeToB64
    ) {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        HttpHeaders headers = HeadersHelper.prepareHeaders(data.getOriginalFilename(), ".bin");
        MediaType mediaType = encodeToB64 ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;
        try {
            byte[] sign = cryptoProService.signRaw(data.getInputStream(), encodeToB64).toByteArray();
            if (encodeToB64)
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(mediaType)
                        .body(new String(sign).replace("\r", "").replace("\n", ""));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(sign);
        } catch (NoSuchAlgorithmException | IOException | SignatureException | InvalidKeyException e) {
            throw new CryptographicException(e.getMessage());
        }
    }
}
