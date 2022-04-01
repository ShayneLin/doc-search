package com.doc.search.util;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CryptoUtil {

    /**
     * MD5摘要
     * @param plainText
     * @return
     */
    public static String md5(String plainText){
        try{
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] digest = md5.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(digest);
        }catch (Exception e){
            throw new RuntimeException("MD5异常");
        }

    }
}
