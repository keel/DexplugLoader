package com.k99k.tools.encrypter;

/**
 * @author keel
 * 
 */
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/**
 * 
 * 使用AES加密与解密,可对String类型进行加密与解密(使用自定义base64包装),可适用于url参数传输(不需要进行URLEncode).
 * 
 */

public class Encrypter {
	private Cipher ecipher;

	private Cipher dcipher;
	
	private static Encrypter mEnc;
	
	public static final Encrypter getInstance(){
		if (mEnc == null) {
			try {
				mEnc = new Encrypter();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mEnc;
	}
	
	/**
	 * 用于加解密的key，可修改
	 */
	private byte[] key = new byte[]{79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};

	private final static String ENCODING = "utf-8";
	
	/**
	 * 设置key
	 * @param keyIn
	 */
	public final void setKey(byte[] keyIn){
		key = keyIn;
		init();
	}
	
	private Encrypter() throws Exception {
		init();
	}
	
	private void init(){
		try {
			ecipher = Cipher.getInstance("AES");
			dcipher = Cipher.getInstance("AES");
			SecretKey key2 = new SecretKey(){
				private static final long serialVersionUID = 1L;

				@Override
				public String getAlgorithm() {
					return "AES";
				}
				@Override
				public byte[] getEncoded() {
					return key;
				}

				@Override
				public String getFormat() {
					return "RAW";
				}};
			ecipher.init(Cipher.ENCRYPT_MODE, key2);
			dcipher.init(Cipher.DECRYPT_MODE, key2);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] encrypt(byte[] src){
		try {
			return ecipher.doFinal(src);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] dec){
		try {
			return dcipher.doFinal(dec);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * base64 out
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public String encrypt(String str) throws Exception {
		byte[] utf8 = str.getBytes(ENCODING);

		byte[] enc = ecipher.doFinal(utf8);

		return Base64Coder.encode(enc);
	
	}

	public String decrypt(String str) throws Exception {
		byte[] dec = Base64Coder.decode(str);

		byte[] utf8 = dcipher.doFinal(dec);

		return new String(utf8, ENCODING);
		
		
	}

	
}
