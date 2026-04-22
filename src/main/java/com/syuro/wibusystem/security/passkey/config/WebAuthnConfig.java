package com.syuro.wibusystem.security.passkey.config;

import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PasskeyProperties.class)
public class WebAuthnConfig {

    @Bean
    public RelyingParty relyingParty(PasskeyProperties props, PasskeyCredentialRepository credentialRepository) {
        return RelyingParty.builder()
                .identity(RelyingPartyIdentity.builder()
                        .id(props.rpId())
                        .name(props.rpName())
                        .build())
                .credentialRepository(credentialRepository)
                .origins(props.origins())
                .build();
    }
}
