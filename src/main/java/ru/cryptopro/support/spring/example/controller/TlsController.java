package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.cryptopro.support.spring.example.dto.ClienTlsDto;
import ru.cryptopro.support.spring.example.service.ClientTlsService;

@CrossOrigin
@RestController
public class TlsController {
    private final ClientTlsService tlsService;

    public TlsController(ClientTlsService tlsService) {
        this.tlsService = tlsService;
    }

    @PostMapping("${app.controller.tls}")
    public String tls(
            @RequestParam boolean mTLS,
            @RequestParam @Schema(defaultValue = "https://cryptopro.ru") String url
            ) {
        return tlsService.connect(ClienTlsDto.builder()
                .url(url)
                .mTLS(mTLS)
                .build());
    }
}
