package ru.cryptopro.support.spring.example.utils;

import ru.CryptoPro.CAdES.CAdESType;

import java.util.HashMap;

public class CadesTypeHelper {
    private static final HashMap<String, Integer> map = new HashMap<String, Integer>(){{
        put("bes", CAdESType.CAdES_BES);
        put("t", CAdESType.CAdES_T);
        put("xlt1", CAdESType.CAdES_X_Long_Type_1);
        put("a", CAdESType.CAdES_A);
    }};
    public static int mapValue(String type) {
        String tmp = type.toLowerCase();
        if (map.containsKey(tmp))
            return map.get(tmp);
        // fallback
        return map.get("bes");
    }
}
