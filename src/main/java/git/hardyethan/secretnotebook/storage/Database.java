package git.hardyethan.secretnotebook.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import git.hardyethan.secretnotebook.encryption.AES256;
import git.hardyethan.secretnotebook.encryption.EncryptionResponse;
import git.hardyethan.secretnotebook.encryption.Utils;

public class Database {
    public static int NOT_IN_DB = -1;
    public static int SUCCESS = 0;
    public static int DB_SAVE_ERROR = -2;

    File databaseFile;
    JSONObject databaseObject;

    public Database(String pathString) {
        databaseFile = new File(pathString);
        if (databaseFile.exists()) {
            try {
                databaseObject = (JSONObject) new JSONParser().parse(Files.readString(Paths.get(pathString)));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(new JFrame("ERROR"), "UNABLE TO READ DATABASE FILE");
                System.exit(1);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(new JFrame("ERROR"), "INVALID DATABASE FILE");
                System.exit(1);
            }
        } else {
            databaseObject = new JSONObject();
            write();
        }
    }

    // EntryID: Title
    public HashMap<String, String> getEntryTitles(String password) {
        HashMap<String, String> content = new HashMap<String, String>();
        for (Object entryIDObject : databaseObject.keySet()) {
            String entryID = (String) entryIDObject;
            JSONObject entryObject = (JSONObject) databaseObject.get(entryID);
            String titleIV = (String) entryObject.get("titleIV");
            String titleSalt = (String) entryObject.get("titleSalt");
            String encryptedTitle = (String) entryObject.get("encryptedTitle");
            String decryptedTitle = AES256.decrypt(Utils.base64StringToByteArray(titleIV),
                    Utils.base64StringToByteArray(titleSalt), password, encryptedTitle);

            /*
             * When encrypting title "ToDo" and message "Cut the grass" with password
             * "nope", using password "nope2" does not throw an error
             * 
             * This pattern does not repeat when the title is changed to "ToDoo"
             * 
             * We can check for an invalid decryption by seeing if any of the chars in the
             * string have a value below 0, which means they should not be valid for our
             * purposes
             * 
             * Effected entry:
             * "ba84ae2a-1650-4dfa-9551-8c5f2d7310a2": {
             * "titleIV": "9RiVzR1Vlgx3m+iF7Kmfbw==",
             * "encryptedMessage": "nk6ACCriIQbUUpLL4sh70w==",
             * "encryptedTitle": "AsQ9ld+pSc06L7ua8ueXOQ==",
             * "messageSalt": "qUVXJ0Wjma\/7MNITZryDVfXVe2WuQ\/I7nreZtYht2m4=",
             * "titleSalt": "NJVvpVkgdWfkqaZrJ8GfbtOkKTSWv1aeRyEhd\/r3qTA=",
             * "messageIV": "A3oigTkFxZq6jbQCA35GDw=="
             * }
             */
            boolean containsNegativeByte = false;
            for (byte b : decryptedTitle.getBytes()) {
                if (b < 0) {
                    containsNegativeByte = true;
                    break;
                }
            }
            if (containsNegativeByte || decryptedTitle.equals("INCORRECT_PASSWORD"))
                continue;

            content.put(entryID, decryptedTitle);
        }
        return content;
    }

    public String getEntry(String entryID, String password) {
        if (!databaseObject.containsKey(entryID)) {
            return "ENTRY_NOT_IN_DATABASE";
        }
        JSONObject entryObject = (JSONObject) databaseObject.get(entryID);
        String messageIV = (String) entryObject.get("messageIV");
        String messageSalt = (String) entryObject.get("messageSalt");
        String encryptedMessage = (String) entryObject.get("encryptedMessage");
        String decryptedMessage = AES256.decrypt(Utils.base64StringToByteArray(messageIV),
                Utils.base64StringToByteArray(messageSalt), password, encryptedMessage);
        return decryptedMessage;
    }

    public int setEntry(String entryID, String message, String password) {
        if (!databaseObject.containsKey(entryID)) {
            return NOT_IN_DB;
        }
        EncryptionResponse newMessageResponse = AES256.encrypt(message, password);

        JSONObject entryObject = (JSONObject) databaseObject.get(entryID);
        entryObject.put("messageIV", Utils.byteArrayToBase64String(newMessageResponse.getIV()));
        entryObject.put("messageSalt", Utils.byteArrayToBase64String(newMessageResponse.getSalt()));
        entryObject.put("encryptedMessage", newMessageResponse.getEncryptedContent());

        return write();
    }

    public int addEntry(EncryptionResponse titleResponse, EncryptionResponse messageResponse) {
        String entryID = UUID.randomUUID().toString();
        while (databaseObject.containsKey(entryID)) {
            entryID = UUID.randomUUID().toString();
        }
        JSONObject entryObject = new JSONObject();
        entryObject.put("titleIV", Utils.byteArrayToBase64String(titleResponse.getIV()));
        entryObject.put("titleSalt", Utils.byteArrayToBase64String(titleResponse.getSalt()));
        entryObject.put("encryptedTitle", titleResponse.getEncryptedContent());
        entryObject.put("messageIV", Utils.byteArrayToBase64String(messageResponse.getIV()));
        entryObject.put("messageSalt", Utils.byteArrayToBase64String(messageResponse.getSalt()));
        entryObject.put("encryptedMessage", messageResponse.getEncryptedContent());
        databaseObject.put(entryID, entryObject);
        return write();
    }

    public int deleteEntry(String entryID) {
        if (!databaseObject.containsKey(entryID)) {
            return NOT_IN_DB;
        }
        databaseObject.remove(entryID);

        return write();
    }

    private int write() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(databaseFile)) {
            String bitey = databaseObject.toJSONString();
            fileOutputStream.write(bitey.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            return DB_SAVE_ERROR;
        }
        return SUCCESS;
    }

    public String toString() {
        return databaseObject.toJSONString();
    }

}
