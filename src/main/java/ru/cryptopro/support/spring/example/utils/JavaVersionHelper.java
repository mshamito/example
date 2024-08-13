package ru.cryptopro.support.spring.example.utils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class JavaVersionHelper {
    public static int getVersion() {
        String version = System.getProperty("java.version");
        log.info("running in JVM {}", version);
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }
}
