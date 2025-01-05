package com.appstaticsx.app.agmeapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    private static final String AES = "AES";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 128;
    private static final int GCM_IV_LENGTH = 12; // GCM Nonce/IV length

    // Retrieve the SHA-1 signature of the app
    private static String getAppSignatureSHA1(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] signature = packageInfo.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(signature);
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // Generate AES SecretKey from SHA-1 signature
    private static SecretKey getSecretKeyFromSHA1(Context context) throws Exception {
        String sha1Signature = getAppSignatureSHA1(context);
        if (sha1Signature == null) throw new IllegalStateException("SHA-1 signature is null");

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] keyBytes = digest.digest(sha1Signature.getBytes(StandardCharsets.UTF_8));
        byte[] key = new byte[KEY_SIZE / 8];
        System.arraycopy(keyBytes, 0, key, 0, key.length);

        return new SecretKeySpec(key, AES);
    }

    // Encrypt the plain text
    public static String encrypt(Context context, String plainText) throws Exception {
        SecretKey secretKey = getSecretKeyFromSHA1(context);
        Cipher cipher = Cipher.getInstance(AES_MODE);
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedBytesWithIV = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedBytesWithIV, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedBytesWithIV, iv.length, encryptedBytes.length);

        return Base64.encodeToString(encryptedBytesWithIV, Base64.DEFAULT);
    }

    // Decrypt the encrypted text
    public static String decrypt(Context context, String encryptedText) throws Exception {
        SecretKey secretKey = getSecretKeyFromSHA1(context);
        Cipher cipher = Cipher.getInstance(AES_MODE);
        byte[] decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedBytes, 0, iv, 0, iv.length);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
        byte[] originalBytes = cipher.doFinal(decodedBytes, iv.length, decodedBytes.length - iv.length);

        return new String(originalBytes, StandardCharsets.UTF_8);
    }
}