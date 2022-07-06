package git.hardyethan.secretnotebook.encryption;

import java.util.Base64;

public class Utils {

    public static String byteArrayToBase64String(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64StringToByteArray(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
