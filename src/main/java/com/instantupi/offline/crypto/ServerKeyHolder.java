package com.instantupi.offline.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Stores server RSA keys (public + private)
 */
@Component
public class ServerKeyHolder {

    private static final Logger log = LoggerFactory.getLogger(ServerKeyHolder.class);

    private KeyPair keyPair; // holds both public & private keys

    /**
     * Runs automatically when app starts
     * Generates a new RSA key pair
     */
    @PostConstruct
    public void init() throws Exception {

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

        gen.initialize(2048); // key size

        this.keyPair = gen.generateKeyPair(); // generate keys

        log.info("Server RSA keypair generated (2048-bit). Public key fingerprint: {}",
                getPublicKeyBase64().substring(0, 32) + "...");
    }

    // Return public key (used for encryption)
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    // Return private key (used for decryption)
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    // Convert public key to Base64 string (for sending to client)
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}