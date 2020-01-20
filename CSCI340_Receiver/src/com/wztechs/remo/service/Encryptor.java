package com.wztechs.remo.service;

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


    public Encryptor(String key){
        System.out.println(key);
            byte[] decodedKey = Base64.getDecoder().decode(key);
            // rebuild key using SecretKeySpec
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            init();

    }

    public Encryptor(){
        try {
            secretKey = KeyGenerator.getInstance(ALGORITHM).generateKey();
            init();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public void init(){
        try {
            encrypt = Cipher.getInstance(ALGORITHM);
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey);

            decrypt = Cipher.getInstance(ALGORITHM);
            decrypt.init(Cipher.DECRYPT_MODE, secretKey);

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public String getKeyString(){
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }


    public boolean isPasscodeValid(String encryptedMsg){
        return decrypt(encryptedMsg).equals(PASSCODE);
    }

    public String encrypt(String str) {
        try {

            byte[] enc = encrypt.doFinal(str.getBytes());
            return Base64.getEncoder().encodeToString(enc);

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            return "";
        }
    }

    public String decrypt(String encoded) {
       try {

           byte[] decoded = Base64.getDecoder().decode(encoded);
           return new String(decrypt.doFinal(decoded));

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            return "";
        }
    }
}
