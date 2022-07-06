package git.hardyethan.secretnotebook.encryption;

public class EncryptionResponse {
    private byte[] iv;
    private byte[] salt;
    private String encryptedContent;

    public EncryptionResponse() {

    }

    public EncryptionResponse(byte[] iv, byte[] salt, String encryptedMessage) {
        this.iv = iv;
        this.salt = salt;
        this.encryptedContent = encryptedMessage;
    }

    public byte[] getIV() {
        return iv;
    }

    public byte[] getSalt() {
        return salt;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }
}
