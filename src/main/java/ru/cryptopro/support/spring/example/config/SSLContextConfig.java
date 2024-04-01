package ru.cryptopro.support.spring.example.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

@Configuration
public class SSLContextConfig {
    private final TrustManager[] trustManagers;
    private final KeyManager[] keyManagers;

    public SSLContextConfig(
            @Qualifier("gostTrustManagers")
            TrustManager[] trustManagers,
            @Qualifier("gostKeyManagers")
            KeyManager[] keyManagers
    ) {
        this.trustManagers = trustManagers;
        this.keyManagers = keyManagers;
    }

    @SneakyThrows
    public SSLContext getInstance(boolean mTLS) {
        SSLContext context = SSLContext.getInstance(Provider.ALGORITHM_12);
        context.init(mTLS? keyManagers : null, trustManagers, null);
        return context;
    }
}
