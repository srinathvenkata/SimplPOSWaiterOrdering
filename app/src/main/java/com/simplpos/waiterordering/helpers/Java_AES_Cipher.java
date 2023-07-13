package com.simplpos.waiterordering.helpers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.util.Base64;
public class Java_AES_Cipher {

    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static int CIPHER_KEY_LEN = 16; //128 bits

    /**
     * Encrypt data using AES Cipher (CBC) with 128 bit key
     *
     *
     * @param key  - key to use should be 16 bytes long (128 bits)
     * @param iv - initialization vector
     * @param data - data to encrypt
     * @return encryptedData data in base64 encoding with iv attached at end after a :
     */
    /*
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String key, String iv, String data) {
        try {
            if (key.length() < Java_AES_Cipher.CIPHER_KEY_LEN) {
                int numPad = Java_AES_Cipher.CIPHER_KEY_LEN - key.length();

                for(int i = 0; i < numPad; i++){
                    key += "0"; //0 pad to len 16 bytes
                }

            } else if (key.length() > Java_AES_Cipher.CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
            }


            IvParameterSpec initVector = new IvParameterSpec(iv.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(Java_AES_Cipher.CIPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, initVector);

            byte[] encryptedData = cipher.doFinal((data.getBytes()));

            String base64_EncryptedData = Base64.getEncoder().encodeToString(encryptedData);
            String base64_IV = Base64.getEncoder().encodeToString(iv.getBytes("UTF-8"));

            return base64_EncryptedData + ":" + base64_IV;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
*/
    /**
     * Decrypt data using AES Cipher (CBC) with 128 bit key
     *
     * @param key - key to use should be 16 bytes long (128 bits)
     * @param data - encrypted data with iv at the end separate by :
     * @return decrypted data string
     */

    /*
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(String key, String data) {
        try {

            String[] parts = data.split(":");

            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(parts[1]));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(Java_AES_Cipher.CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] decodedEncryptedData = Base64.getDecoder().decode(parts[0]);

            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static class DarKnight {
        private static final String ALGORITHM = "AES"; // AES/GCM/NoPadding for publishing in playstore, this mode has to be used
        // or change to MD5 encryption by joining the SALT
        private static final byte[] SALT = "hRshIkES1Bhairav".getBytes();// THE KEY MUST BE SAME
        private static final String X = DarKnight.class.getSimpleName();
        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String getEncrypted(String plainText) {
            if (plainText == null) {
                return null;
            }

            Key salt = getSalt();

            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, salt);
                byte[] encodedValue = cipher.doFinal(plainText.getBytes());
                return Base64.getEncoder().encodeToString(encodedValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new IllegalArgumentException("Failed to encrypt data");
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String getDecrypted(String encodedText) {
            if (encodedText == null) {
                return null;
            }

            Key salt = getSalt();
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, salt);
                byte[] decodedValue = Base64.getDecoder().decode(encodedText);
                byte[] decValue = cipher.doFinal(decodedValue);
                return new String(decValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        static Key getSalt() {
            return new SecretKeySpec(SALT, ALGORITHM);
        }
    }
 */
}
