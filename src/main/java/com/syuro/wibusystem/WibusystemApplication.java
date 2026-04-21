package com.syuro.wibusystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class WibusystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WibusystemApplication.class, args);
    }

}
