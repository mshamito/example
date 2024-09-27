package ru.cryptopro.support.spring.example.utils;

import java.util.HashMap;
import java.util.Map;

public class GostProvidersNames {
    private static final Map<String,String> map = new HashMap<String,String>() {{
        put("ru.CryptoPro.JCSP.JCSP","JCSP");
        put("ru.CryptoPro.JCP.JCP", "JCP");
        put("ru.CryptoPro.Crypto.CryptoProvider", "Crypto");
        put("ru.CryptoPro.sspiSSL.SSPISSL", "JTLS");
        put("ru.CryptoPro.ssl.Provider", "JTLS");
        put("ru.CryptoPro.reprov.RevCheck", "RevCheck");
    }};

    public static String mapNames(String longName) {
        if (!map.containsKey(longName))
            throw new RuntimeException("provider name not recognized");
        return map.get(longName);
    }
}
