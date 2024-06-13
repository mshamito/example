package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.dto.*;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.expection.ProvidedDataException;
import ru.cryptopro.support.spring.example.service.SignService;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@Log4j2
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public class CmsController {
    private final SignService signService;

    public CmsController(SignService signService) {
        this.signService = signService;
    }

    @PostMapping(value = "${app.controller.encrypt}", produces =MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> encrypt(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false, value = "cert") List<MultipartFile> certs
    ) throws IOException {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        return signService.encrypt(new DataDto(data.getBytes()), certs);
    }

    @PostMapping(value = "${app.controller.decrypt}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> decrypt(
            @RequestParam(value = "cms") MultipartFile encryptedCms
    ) throws IOException {
        if (encryptedCms.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        return signService.decrypt(encryptedCms);
    }

    @PostMapping("${app.controller.sign}")
    public CmsDto sign(
            @RequestParam MultipartFile data,
            @RequestParam(required = false) boolean detached,
            @RequestParam(required = false) @Schema(defaultValue = "http://testca2012.cryptopro.ru/tsp/tsp.srf") String tsp,
            @RequestParam(required = false) @Schema(defaultValue = "bes") String type
    ) throws IOException {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        SignatureParams.SignatureParamsBuilder builder = SignatureParams.builder();
        builder.detached(detached);
        if (Strings.isNotBlank(tsp))
            builder.tsp(tsp);
        if (Strings.isNotBlank(type))
            builder.type(type);
        SignatureParams params = builder.build();
        CmsDto response = signService.sign(new DataDto(data.getBytes()), params);
        if (response.isEmpty())
            throw new CryptographicException("sign failed");
        return response;
    }

    @PostMapping("${app.controller.verify}")
    public List<VerifyResult> verify(
            @RequestParam MultipartFile sign,
            @RequestParam(required = false) MultipartFile data
    ) throws IOException {
        return signService.verify(new VerifyRequest(sign.getBytes(), data == null ? null : data.getBytes()));
    }
}
