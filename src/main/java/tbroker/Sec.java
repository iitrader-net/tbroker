/*-****************************************************************************/
/*-                                                                           */
/*-            Copyright (c) of tbroker                                       */
/*-                                                                           */
/*-  This software is copyrighted by and is the sole property of socware.net. */
/*-  All rights, title, ownership, or other interests in the software remain  */
/*-  the property of socware.net. The source code is FREE for short-term      */
/*-  evaluation, educational or non-commercial research only. Any commercial  */
/*-  application may only be used in accordance with the corresponding license*/
/*-  agreement. Any unauthorized use, duplication, transmission, distribution,*/
/*-  or disclosure of this software is expressly forbidden.                   */
/*-                                                                           */
/*-  Knowledge of the source code may NOT be used to develop a similar product*/
/*-                                                                           */
/*-  This Copyright notice may not be removed or modified without prior       */
/*-  written consent of socware.net.                                          */
/*-                                                                           */
/*-  socware.net reserves the right to modify this software                   */
/*-  without notice.                                                          */
/*-                                                                           */
/*-  To contact socware.net:                                                  */
/*-                                                                           */
/*-             socware.help@gmail.com                                        */
/*-                                                                           */
/*-****************************************************************************/
package tbroker;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.apache.commons.codec.binary.*;

public class Sec {
    private final String CIPHER_NAME = "AES/ECB/PKCS5Padding";
    private final String ALGORITHM_NAME = "AES";

    Cipher en, de;
    byte[] iv = new byte[16];
    org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();

    public Sec(String key) throws Exception {
        byte[] salt = {
            (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
            (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
        };
        Arrays.fill(iv, (byte) 0);
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            en = Cipher.getInstance("AES/CBC/PKCS5Padding");
            en.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
        }
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            de = Cipher.getInstance("AES/CBC/PKCS5Padding");
            de.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        }
    }

    byte[] en(byte[] plain) throws Exception {
        return en.doFinal(plain);
    }

    String en(String plain) throws Exception {
        return base64.encodeToString(en(plain.getBytes()));
    }

    byte[] de(byte[] cipher) throws Exception {
        return de.doFinal(cipher);
    }

    String de(String cipher) throws Exception {
        return new String(de(base64.decode(cipher)));
    }

    public static void main(String[] args) {
        try {
            Sec sec = new Sec(args[0]);
            String s = args[1];
            String c = sec.en(s);
            System.out.println("key : " + args[0]);
            System.out.println("test: " + s);
            System.out.println(sec.de(sec.en(s)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
