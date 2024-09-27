package ru.cryptopro.support.spring.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
@SuppressWarnings("unused")
public class ListProvidersService {
    @EventListener(ApplicationReadyEvent.class)
    public void listProviders() {
        List<Provider> providers = Arrays.asList(Security.getProviders());
        log.info("java security providers:");
        for (Provider provider : providers)
            log.info("{}) {}", providers.indexOf(provider) + 1, provider.getName());
        log.info("security providers enumerated");
    }
}
