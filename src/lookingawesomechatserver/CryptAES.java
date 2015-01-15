package lookingawesomechatserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
 


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
 
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class CryptAES {
	  
	public static Key generateAESKeyFileAs(String AesKeyPath) throws IOException
	{
		// TODO: FIleIO 
		try {
			return generateAESKeyFile(AesKeyPath);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Key generateAESKeyFile(String AesKeyFilePath) throws IOException, NoSuchAlgorithmException
	   {
		  OutputStream AesKeyFile = null;
	      try {
	    	 AesKeyFile = new FileOutputStream(AesKeyFilePath);
	    	  
	         KeyGenerator keyGen = KeyGenerator.getInstance( "AES" );
	         keyGen.init(128);
	         SecretKey skey = keyGen.generateKey();
	         ObjectOutputStream out = new ObjectOutputStream( AesKeyFile );
	         try { out.writeObject( skey ); } finally { out.close(); }
	         
	         return (Key) skey;
	         
	      } finally {	         
	         AesKeyFile.close();
	      }
	   }
	   
	   public static Key importAESKeyFile(String AesKeyFilePath) throws IOException, ClassNotFoundException
	   {
			InputStream AesKeyFile = null;
			try {
				AesKeyFile = new FileInputStream(AesKeyFilePath);
			  
				ObjectInputStream in = new ObjectInputStream( AesKeyFile );
				Key skey = null;
				try { skey = (Key) in.readObject(); } finally { in.close(); }				 
				return skey;
				 
			} finally {	         
			AesKeyFile.close();
			}		   
	   }
	   
	   
	   public static String decrypt(String cryptedMessage, Key key) throws Exception 
	   {
		  BASE64Decoder myDecoder = new BASE64Decoder();
		  byte[] crypted = myDecoder.decodeBuffer(cryptedMessage);   
		  
		  Cipher cipher = Cipher.getInstance("AES");
		  cipher.init(Cipher.DECRYPT_MODE, key);
		  byte[] cipherData = cipher.doFinal(crypted);
		  return new String(cipherData);
	   }
	   
	   
	   public static String encrypt(String plainMessage, Key key) throws Exception
	   {		 
		  Cipher cipher = Cipher.getInstance("AES");
		  cipher.init(Cipher.ENCRYPT_MODE, key);
		  byte[] encrypted = cipher.doFinal(plainMessage.getBytes());		 
		 
		  BASE64Encoder myEncoder = new BASE64Encoder();
		  String cryptedMessage = myEncoder.encode(encrypted);
		   
		  return cryptedMessage;
	   }
	   
	   
	
}
