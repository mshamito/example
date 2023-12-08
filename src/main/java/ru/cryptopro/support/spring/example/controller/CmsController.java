package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.java.Log;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.dto.CmsDto;
import ru.cryptopro.support.spring.example.dto.DataDto;
import ru.cryptopro.support.spring.example.dto.SignatureParams;
import ru.cryptopro.support.spring.example.expection.CryptographicException;
import ru.cryptopro.support.spring.example.expection.DataException;
import ru.cryptopro.support.spring.example.service.SignService;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@Log
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public class CmsController {
    private final SignService signService;

    public CmsController(SignService signService) {
        this.signService = signService;
    }

    @PostMapping("${app.controller.encrypt}")
    public CmsDto encrypt(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false, value = "cert") List<MultipartFile> certs
    ) throws IOException {
        if (data.isEmpty())
            throw new DataException("Provided data is empty");
        CmsDto response = signService.encrypt(new DataDto(data.getBytes()), certs);
        if (response.isEmpty())
            throw new CryptographicException("encrypt failed");
        return response;
    }

    @PostMapping("${app.controller.sign}")
    public CmsDto sign(
            @RequestParam(value = "data") MultipartFile data,
            @RequestParam(required = false) boolean detached,
            @RequestParam(required = false) @Schema(defaultValue = "http://testca2012.cryptopro.ru/tsp/tsp.srf") String tsp,
            @RequestParam(required = false) @Schema(defaultValue = "bes") String type
    ) throws IOException {
        if (data.isEmpty())
            throw new DataException("Provided data is empty");
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
}
