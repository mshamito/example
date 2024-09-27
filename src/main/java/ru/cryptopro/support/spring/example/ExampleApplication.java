package ru.cryptopro.support.spring.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import ru.cryptopro.support.spring.example.utils.GostProvidersNames;
import ru.cryptopro.support.spring.example.utils.JavaVersionHelper;

import java.lang.reflect.InvocationTargetException;
import java.security.Security;
import java.security.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class ExampleApplication extends SpringBootServletInitializer {

    static {
        System.setProperty("com.ibm.security.enableCRLDP", "true"); //crl online
        System.setProperty("com.sun.security.enableCRLDP", "true"); //crl online
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true"); // для загрузки сертификатов по AIA из сети
        System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true"); // для загрузки сертификатов по AIA из сети
        System.setProperty("java.util.prefs.syncInterval", "99999"); // https://support.cryptopro.ru/index.php?/Knowledgebase/Article/View/315/6/warning-couldnt-flush-system-prefs-javautilprefsbackingstoreexception--sreate-failed

        int javaMajorVersion = JavaVersionHelper.getVersion();
        if (javaMajorVersion >= 10) {
            boolean isJCSPExists = addProvider("ru.CryptoPro.JCSP.JCSP");
            if (isJCSPExists) {
                if (!addProvider("ru.CryptoPro.sspiSSL.SSPISSL"))
                    addProvider("ru.CryptoPro.ssl.Provider");
            } else {
                addProvider("ru.CryptoPro.JCP.JCP");
                addProvider("ru.CryptoPro.Crypto.CryptoProvider");
                addProvider("ru.CryptoPro.ssl.Provider");
            }
            addProvider("ru.CryptoPro.reprov.RevCheck");
        }
    }

    private static boolean addProvider(String fullName) {
        List<String> providers = Arrays.stream(Security.getProviders()).map(Provider::getName).collect(Collectors.toList());
        try {
            String shortName = GostProvidersNames.mapNames(fullName);
            if (!providers.contains(shortName)) {
                Security.addProvider((Provider) Class.forName(fullName).getConstructor().newInstance());
                System.out.println("Provider registered " + shortName);
            }
            return true;
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | RuntimeException e) {
            System.out.println("Failed add provider: " + fullName);
            return false;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ExampleApplication.class);
    }

}
