package com.carshare.rentalsystem.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryptionUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final byte[] KEY = "Bar12345Bar12345".getBytes(StandardCharsets.UTF_8);
    private static final byte[] IV = "ThisIsASecretKet".getBytes(StandardCharsets.UTF_8);

    public static String encrypt(String data) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES Encryption failed. Ensure correct key/IV"
                    + " and algorithm.", e);
        }
    }

    public static String decrypt(String encrypted) {

        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encrypted));
            return new String(original);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES Decryption failed. Ensure valid ciphertext,"
                    + " key/IV, and algorithm.", e);
        }
    }

    private static Cipher getCipher(int mode) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, keySpec, ivSpec);
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize AES cipher. Check"
                    + " key, IV, and algorithm.", e);
        }
    }
}
