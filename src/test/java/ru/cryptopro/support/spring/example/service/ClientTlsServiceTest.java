package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.cryptopro.support.spring.example.dto.ClientTlsDto;
import ru.cryptopro.support.spring.example.dto.TlsConnectionResult;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ClientTlsServiceTest {
    @Autowired
    ClientTlsService service;

    @Test
    void connect() {
        TlsConnectionResult result = service.connect(
                ClientTlsDto.builder()
                        .mTLS(false)
                        .url("https://cryptopro.ru")
                        .build()
        );
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}