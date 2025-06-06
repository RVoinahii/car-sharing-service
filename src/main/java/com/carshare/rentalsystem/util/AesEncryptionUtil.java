package com.carshare.rentalsystem.util;

import com.carshare.rentalsystem.exception.CipherInitializationException;
import com.carshare.rentalsystem.exception.DecryptionException;
import com.carshare.rentalsystem.exception.EncryptionException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesEncryptionUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

    @Value("${encryption.key}")
    private String keyString;

    @Value("${encryption.iv}")
    private String ivString;

    private byte[] key;
    private byte[] iv;

    @PostConstruct
    public void init() {
        this.key = keyString.getBytes(StandardCharsets.UTF_8);
        this.iv = ivString.getBytes(StandardCharsets.UTF_8);
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException(
                    "Encryption failed: unable to process data with provided AES key and IV."
            );
        }
    }

    public String decrypt(String encrypted) {

        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encrypted));
            return new String(original);
        } catch (GeneralSecurityException e) {
            throw new DecryptionException(
                    "Decryption failed: invalid ciphertext or mismatched AES configuration.");
        }
    }

    private Cipher getCipher(int mode) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, keySpec, ivSpec);
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new CipherInitializationException("Cipher initialization failed: check"
                    + " AES key, IV, and algorithm configuration.");
        }
    }
}
