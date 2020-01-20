package com.wztechs.remo.service.connection;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encryptor {

    private SecretKey secretKey;
    private final static String PASSCODE = "pass";
    private final static String ALGORITHM = "DES";
    private Cipher encrypt;
    private Cipher decrypt;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Encryptor(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        // rebuild key using SecretKeySpec
        secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
        init();
    }

    public Encryptor(){
        try {
            secretKey = KeyGenerator.getInstance(ALGORITHM).generateKey();
            init();
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }




    public void init() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
            encrypt = Cipher.getInstance(ALGORITHM);
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey);

            decrypt = Cipher.getInstance(ALGORITHM);
            decrypt.init(Cipher.DECRYPT_MODE, secretKey);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getKeyString(){
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isPasscodeValid(String encryptedMsg){
        return decrypt(encryptedMsg).equals(PASSCODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String encrypt(String str) {
        try {

            byte[] enc = encrypt.doFinal(str.getBytes());
            return Base64.getEncoder().encodeToString(enc);

        } catch (BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            return "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decrypt(String encoded) {
        try {

            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decrypt.doFinal(decoded));

        } catch (BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            return "";
        }
    }
}
