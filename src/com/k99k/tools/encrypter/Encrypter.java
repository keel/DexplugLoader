package com.k99k.tools.encrypter;

/**
 * @author keel
 * 
 */
//import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;


/**
 * 
 * 使用AES加密与解密,可对String类型进行加密与解密,密文可使用String存储.
 * 
 */

public class Encrypter {
	Cipher ecipher;

	Cipher dcipher;
	
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
	 * 用于加解密的key
	 */
	private static byte[] key = new byte[]{79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};

	/**
	 * 设置key
	 * @param keyIn
	 */
	public static final void setKey(byte[] keyIn){
		key = keyIn;
	}
	
	private Encrypter() throws Exception {
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
		
		//return SimpleCrypto.encrypt(this.key, str);
	
		// Encode the string into bytes using utf-8
		byte[] utf8 = str.getBytes("UTF8");

		// Encrypt
		byte[] enc = ecipher.doFinal(utf8);

		// Encode bytes to base64 to get a string
		return Base64Coder.encode(enc);
	
	}

	public String decrypt(String str) throws Exception {
		//return SimpleCrypto.decrypt(this.key, str);
		
		// Decode base64 to get bytes
		byte[] dec = Base64Coder.decode(str);

		byte[] utf8 = dcipher.doFinal(dec);

		// Decode using utf-8
		return new String(utf8, "UTF8");
		
		
	}

	
}
