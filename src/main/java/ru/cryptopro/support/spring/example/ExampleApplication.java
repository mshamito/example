package ru.cryptopro.support.spring.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import ru.cryptopro.support.spring.example.utils.JavaVersionHelper;

import java.security.Security;
import java.security.Provider;

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
            try {
                addProvider("ru.CryptoPro.JCSP.JCSP");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Security.addProvider(new ru.CryptoPro.JCP.JCP());
            Security.addProvider(new ru.CryptoPro.reprov.RevCheck());
            Security.addProvider(new ru.CryptoPro.Crypto.CryptoProvider());
        }
    }

    static void addProvider(String fullName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Security.addProvider((Provider) Class.forName(fullName).newInstance());
    }

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ExampleApplication.class);
    }

}
