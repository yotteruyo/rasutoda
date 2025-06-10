package com.pelosa.rasutoda.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct; // <-- javax.annotation 대신 jakarta.annotation으로 변경
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AES256Util {

    @Value("${aes.secret.key}")
    private String secretKeyBase64;

    @Value("${aes.secret.iv}")
    private String secretIvBase64;

    private byte[] keyBytes;
    private byte[] ivBytes;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @PostConstruct
    public void init() {
        try {
            this.keyBytes = Base64.getDecoder().decode(secretKeyBase64.getBytes(StandardCharsets.UTF_8));
            this.ivBytes = Base64.getDecoder().decode(secretIvBase64.getBytes(StandardCharsets.UTF_8));

            if (this.keyBytes.length != 32) {
                throw new IllegalArgumentException("AES key length must be 32 bytes (256 bits) after Base64 decoding, but got " + this.keyBytes.length + " bytes.");
            }
            if (this.ivBytes.length != 16) {
                throw new IllegalArgumentException("AES IV length must be 16 bytes (128 bits) after Base64 decoding, but got " + this.ivBytes.length + " bytes.");
            }
            System.out.println("AES Key and IV initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize AES Key or IV: " + e.getMessage());
            throw new RuntimeException("AES 암호화/복호화 키 초기화 실패", e);
        }
    }

    public String encrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}