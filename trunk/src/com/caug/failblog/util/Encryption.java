package com.caug.failblog.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption 
{
	private static final byte[] defaultSalt = { 4, 23, -8, 93, -68, -22, -2, 74};
	public static final byte[] defaultIV = { 3, -32, 45, -1, 8, -59, 84, 21, -94, 43, 15, -31, -15, 123, -3, 1 };
		
	public static byte[] encrypt(byte[] data, byte[] password)throws Exception
	{
		return encrypt(data, password, defaultIV);
	}
	
	public static byte[] encrypt(byte[] data, byte[] password, byte[] iv)throws Exception
	{
		// Create a key from the supplied passphrase.
		SecretKeySpec keySpec = new SecretKeySpec(password, "AES");
		
		// Create the algorithm parameters.
	    AlgorithmParameterSpec aps = new IvParameterSpec(iv);
		
		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, aps);
		
		return cipher.doFinal(data);
	}
	
	public static String encryptToHex(String data, String password)throws Exception
	{
		return encodeHex(encrypt(data.getBytes(), password.getBytes()));
	}
	
	public static byte[] decryptFromHex(String data, String password)throws Exception
	{
		return decrypt(decodeHex(data), password.getBytes());
	}
	
	public static byte[] decrypt(byte[] data, byte[] password)throws Exception
	{
		return decrypt(data, password, defaultIV);
	}
	
	public static byte[] decrypt(byte[] data, byte[] password, byte[] iv)throws Exception
	{
		//byte[] raw = password.getBytes();
		
		// Create a key from the supplied passphrase.
		SecretKeySpec keySpec = new SecretKeySpec(password, "AES");	
		
		// Create the algorithm parameters.
		AlgorithmParameterSpec aps = new IvParameterSpec(iv);
		
		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, aps);
		
		return cipher.doFinal(data);
	}

	public static byte[] hash(byte[] data, byte[] salt)throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
			
		md.reset();
		
		md.update(data);
		md.update(salt);
		
		return md.digest();
	}

	public static byte[] hash(String data, byte[] salt)throws NoSuchAlgorithmException
	{		
		return hash(data.getBytes(), salt);
	}

	public static byte[] hash(byte[] data)throws NoSuchAlgorithmException
	{		
		return hash(data, defaultSalt);
	}

	public static byte[] hash(String data)throws NoSuchAlgorithmException
	{		
		return hash(data.getBytes(), defaultSalt);
	}
		
	public static String hashToHex(String data)throws NoSuchAlgorithmException
	{		
		byte[] bytes = hash(data.getBytes());
		StringBuffer buffer = new StringBuffer();
		
		for(byte b : bytes)
		{
			String value = Integer.toHexString(b + 128);
			if(value.length() == 1)
			{
				buffer.append("0" + value);
			}else{
				buffer.append(value);
			}
		}
		return buffer.toString().toUpperCase();
	}

	public static String encodeHex(byte[] bytes)
	{
		StringBuffer buffer = new StringBuffer();
		
		for(byte b : bytes)
		{
			String value = Integer.toHexString(b + 128);
			if(value.length() == 1)
			{
				buffer.append("0" + value);
			}else{
				buffer.append(value);
			}
		}
		return buffer.toString().toUpperCase();
	}
	
	public static byte[] decodeHex(String string)
	{
		int length = string.length() / 2;
		byte[] bytes = new byte[length];
		for(int x = 0 ; x < length ;x++)
		{
			int value = Integer.parseInt(string.substring(x * 2, (x + 1) * 2), 16);
			bytes[x] = (byte)(value - 128);
		}
		return bytes;
	}
}
