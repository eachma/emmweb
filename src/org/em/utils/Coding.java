package org.em.utils;
import java.security.MessageDigest;

public class Coding {
	// MD5 SHA1
    public final static String encode(String s, String algorithm) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] byteInput = s.getBytes();
           
            MessageDigest mdInst = MessageDigest.getInstance(algorithm);
           
            mdInst.update(byteInput);
           
            byte[] md = mdInst.digest();
           
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
