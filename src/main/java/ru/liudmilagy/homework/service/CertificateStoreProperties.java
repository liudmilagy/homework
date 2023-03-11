package ru.liudmilagy.homework.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "certificate.key-store")
@Getter
@Setter
public class CertificateStoreProperties {
    private String path;
    private String pass;
}
