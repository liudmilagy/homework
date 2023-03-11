package ru.liudmilagy.homework.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.*;
import java.util.*;

@EnableConfigurationProperties(CertificateStoreProperties.class)
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CertificateMetrics {
    MeterRegistry meterRegistry;
    CertificateStoreProperties certificateStoreProperties;

    @PostConstruct
    public void registerMetrics() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            String keyStoreFile = certificateStoreProperties.getPath();
            char[] password = certificateStoreProperties.getPass().toCharArray();
            keyStore.load(new FileInputStream(keyStoreFile), password);

            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate) {
                    X509Certificate x509Certificate = (X509Certificate) certificate;
                    Gauge.builder("certificate.days_until_expiry",
                            () -> getCertificateDurationByDays(x509Certificate))
                            .tag("alias", alias)
                            .register(meterRegistry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Long getCertificateDurationByDays(X509Certificate x509Certificate) {
        Instant now = Instant.now();
        Instant expiry = x509Certificate.getNotAfter().toInstant();
        return Duration.between(now, expiry).toDays();
    }
}
