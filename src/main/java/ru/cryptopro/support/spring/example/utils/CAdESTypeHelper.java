package ru.cryptopro.support.spring.example.utils;

import ru.CryptoPro.CAdES.CAdESType;

import java.util.HashMap;
import java.util.Map;

public class CAdESTypeHelper {
    private static final HashMap<String, Integer> map = new HashMap<String, Integer>() {{
        put("bes", CAdESType.CAdES_BES);
        put("t", CAdESType.CAdES_T);
        put("xlt1", CAdESType.CAdES_X_Long_Type_1);
        put("a", CAdESType.CAdES_A);
    }};

    public static int mapValue(String type) {
        String key = type.toLowerCase();
        if (map.containsKey(key))
            return map.get(key);
        // fallback
        return map.get("bes");
    }

    public static String mapValue(int type) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == type)
                return entry.getKey();
        }
        return "detect failure";
    }
}
