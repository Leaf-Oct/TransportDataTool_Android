package com.leafoct.textmessage;

import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Security {
	private static byte[] secret_key;
	private static Cipher en,de;
	private static SecretKey sk;
	public static void initial(byte[] key) {
		secret_key=key;
		sk=new SecretKeySpec(secret_key, "AES");
		try {
			en=Cipher.getInstance("AES/ECB/PKCS5Padding");
			de=Cipher.getInstance("AES/ECB/PKCS5Padding");
			en.init(Cipher.ENCRYPT_MODE, sk);
			de.init(Cipher.DECRYPT_MODE, sk);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public static byte[] encrypt(String s) throws GeneralSecurityException, UnsupportedEncodingException {
		return en.doFinal(s.getBytes("UTF-8"));
	}
	public static String decrypt(byte[] s) throws GeneralSecurityException, UnsupportedEncodingException{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return new String(de.doFinal(s), StandardCharsets.UTF_8);
		}
		else{
			return new String(de.doFinal(s),"UTF-8");
		}
	}
}
