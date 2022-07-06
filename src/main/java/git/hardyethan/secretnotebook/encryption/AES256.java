package git.hardyethan.secretnotebook.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256 {

    private static int inclusiveRand(int min, int max) {
        return new Random().nextInt(max + 1 - min) + min;
    }

    // https://howtodoinjava.com/java/java-security/aes-256-encryption-decryption/
    public static EncryptionResponse encrypt(String message, String password) {
        byte[] iv = new byte[16];
        for (int idx = 0; idx < iv.length; idx++) {
            iv[idx] = (byte) inclusiveRand(0, 255);
        }
        byte[] salt = new byte[32];
        for (int idx = 0; idx < salt.length; idx++) {
            salt[idx] = (byte) inclusiveRand(0, 255);
        }
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            // https://en.wikipedia.org/wiki/PBKDF2
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return new EncryptionResponse(iv, salt, Utils.byteArrayToBase64String(encrypted));
        } catch (Exception e) {
            e.printStackTrace();
            return new EncryptionResponse();
        }
    }

    // https://howtodoinjava.com/java/java-security/aes-256-encryption-decryption/
    public static String decrypt(byte[] iv, byte[] salt, String password, String encryptedString) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            // https://en.wikipedia.org/wiki/PBKDF2
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            return new String(cipher.doFinal(Utils.base64StringToByteArray(encryptedString)));
        } catch (BadPaddingException e) {
            return "INCORRECT_PASSWORD";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED_TO_DECRYPT";
        }
    }

}
