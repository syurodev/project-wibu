package com.syuro.wibusystem.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.mail.resend")
public record MailProperties(String apiKey, String from) {}
