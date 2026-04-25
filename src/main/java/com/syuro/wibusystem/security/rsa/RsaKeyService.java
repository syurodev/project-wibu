package com.syuro.wibusystem.security.rsa;

import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * Generates an RSA-2048 key pair at startup.
 * Public key is exposed to clients for encrypting passwords before transmission.
 * Private key never leaves the server.
 */
@Service
public class RsaKeyService {

    private final KeyPair keyPair;

    public RsaKeyService() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            this.keyPair = gen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * Returns the public key as Base64-encoded DER (SPKI format) for the browser.
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    /**
     * Decrypts a Base64-encoded RSA-OAEP (SHA-256) ciphertext.
     * Throws INVALID_CREDENTIALS on failure so callers don't need to handle crypto exceptions.
     */
    public String decrypt(String encryptedBase64) {
        try {
            // Web Crypto RSA-OAEP with hash:SHA-256 uses SHA-256 for both OAEP and MGF1.
            // Java's OAEPWithSHA-256AndMGF1Padding defaults MGF1 to SHA-1 — must override explicitly.
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), oaepParams);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
