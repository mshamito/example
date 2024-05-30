package ru.cryptopro.support.spring.example.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.service.CertService;

@RestController
@CrossOrigin
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public class CertController {
    private final CertService certService;

    public CertController(CertService certService) {
        this.certService = certService;
    }

    @PostMapping("${app.controller.cert}")
    public boolean validateCert(
            @RequestParam(value = "cert") MultipartFile cert
    ) {
        return certService.validateCertificate(cert);
    }
}
