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

/**
 * 
 * @author Some guy on the web.
 * @see http://blog.axxg.de/java-verschluesselung-beispiel-quickstart/
 */
public class CryptEasyCrypt {

	   private Key key = null;
	   private String verfahren = null;
	 
	   /**
	    * @param Key verwendeter Schluessel
	    * @param verfahren bestimmt das verwendete Verschluesselungsverfahren "RSA", "AES", ....
	    * @throws Exception
	    */
	   public CryptEasyCrypt(Key k, String verfahren) throws Exception {
	      this.key = k;
	      this.verfahren = verfahren;
	   }
	 
	   /**Verschluesselt einen Outputstream
	    * @param os Klartext-Outputstream
	    * @return verschluesselter Outputstream
	    * @throws Exception
	    */
	   public OutputStream encryptOutputStream(OutputStream os) throws Exception {
	      // integritaet pruefen
	      valid();
	       
	      // eigentliche Nachricht mit RSA verschluesseln
	      Cipher cipher = Cipher.getInstance(verfahren);
	      cipher.init(Cipher.ENCRYPT_MODE, key);
	      os = new CipherOutputStream(os, cipher);
	       
	      return os;
	   }
	 
	   /** Entschluesselt einen Inputstream
	    * @param is verschluesselter Inputstream
	    * @return Klartext-Inputstream
	    * @throws Exception
	    */
	   public InputStream decryptInputStream(InputStream is) throws Exception {
	      // integritaet pruefen
	      valid();
	       
	      // Daten mit AES entschluesseln
	      Cipher cipher = Cipher.getInstance(verfahren);
	      cipher.init(Cipher.DECRYPT_MODE, key);
	      is = new CipherInputStream(is, cipher);
	 
	      return is;
	   }
	 
	   /** Verschluesselt einen Text in BASE64
	    * @param text Klartext
	    * @return BASE64 String
	    * @throws Exception
	    */
	   public String encrypt(String text) throws Exception {
	      // integritaet pruefen
	      valid();
	       
	      // Verschluesseln
	      Cipher cipher = Cipher.getInstance(verfahren);
	      cipher.init(Cipher.ENCRYPT_MODE, key);
	      byte[] encrypted = cipher.doFinal(text.getBytes());
	 
	      // bytes zu Base64-String konvertieren
	      BASE64Encoder myEncoder = new BASE64Encoder();
	      String geheim = myEncoder.encode(encrypted);
	       
	      return geheim;
	   }
	 
	   /** Entschluesselt einen BASE64 kodierten Text
	    * @param geheim BASE64 kodierter Text
	    * @return Klartext
	    * @throws Exception
	    */
	   public String decrypt(String geheim) throws Exception {
	      // integritaet pruefen
	      valid();
	       
	      // BASE64 String zu Byte-Array
	      BASE64Decoder myDecoder = new BASE64Decoder();
	      byte[] crypted = myDecoder.decodeBuffer(geheim);     
	        
	      // entschluesseln
	      Cipher cipher = Cipher.getInstance(verfahren);
	      cipher.init(Cipher.DECRYPT_MODE, key);
	      byte[] cipherData = cipher.doFinal(crypted);
	      return new String(cipherData);
	   }
	    
	   //++++++++++++++++++++++++++++++
	   // Validierung
	   //++++++++++++++++++++++++++++++
	    
	   private boolean valid() throws Exception{
	      if(verfahren == null){
	         throw new NullPointerException("Kein Verfahren angegeben!");
	      }
	       
	      if(key == null){
	         throw new NullPointerException("Keinen Key angegeben!");
	      }
	       
	      if(verfahren.isEmpty()){
	         throw new NullPointerException("Kein Verfahren angegeben!");
	      }
	       
	      return true;
	   }
	    
	   //++++++++++++++++++++++++++++++
	   // Getter und Setter
	   //++++++++++++++++++++++++++++++
	    
	   public Key getKey() {
	      return key;
	   }
	 
	   public void setKey(Key key) {
	      this.key = key;
	   }
	 
	   public String getVerfahren() {
	      return verfahren;
	   }
	 
	   public void setVerfahren(String verfahren) {
	      this.verfahren = verfahren;
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
	   
	   
	   public static String decrypt(String geheim, Key key) throws Exception {
		      // integritaet pruefen
		      //valid();
		       
		      // BASE64 String zu Byte-Array
		      BASE64Decoder myDecoder = new BASE64Decoder();
		      byte[] crypted = myDecoder.decodeBuffer(geheim);     
		        
		      // entschluesseln
		      Cipher cipher = Cipher.getInstance("AES");
		      cipher.init(Cipher.DECRYPT_MODE, key);
		      byte[] cipherData = cipher.doFinal(crypted);
		      return new String(cipherData);
		   }
	   
	   
	   public static String encrypt(String text, Key key) throws Exception {
		      // integritaet pruefen
		      //valid();
		       
		      // Verschluesseln
		      Cipher cipher = Cipher.getInstance("AES");
		      cipher.init(Cipher.ENCRYPT_MODE, key);
		      byte[] encrypted = cipher.doFinal(text.getBytes());
		 
		      // bytes zu Base64-String konvertieren
		      BASE64Encoder myEncoder = new BASE64Encoder();
		      String geheim = myEncoder.encode(encrypted);
		       
		      return geheim;
		   }
	   
	   
	   public static void bsp1(){
		   
		   try {
			   // zufaelligen Schluessel erzeugen
			   KeyGenerator keygen = KeyGenerator.getInstance("AES");
			   keygen.init(128);
			   SecretKey aesKey = keygen.generateKey();
			    
			   // Klasse erzeugen
			   CryptEasyCrypt ec = new CryptEasyCrypt(aesKey, "AES");
			    
			   // Text ver- und entschluesseln
			   String text = "Hallo AxxG-Leser";
			   String geheim = ec.encrypt(text);
			   String erg = ec.decrypt(geheim);
			    
			   System.out.println("Normaler Text:" + text);
			   System.out.println("Geheimer Text:" + geheim);
			   System.out.println("decrypt  Text:" + erg);     
			} catch (Exception e) {
			   e.printStackTrace();
			}
		   
	   }
	   public static void bsp2(){
		   try {
		      // zufaelligen Schluessel erzeugen
		      KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		      keygen.initialize(1024);
		      KeyPair rsaKeys = keygen.genKeyPair();
		       
		      // Klasse erzeugen
		      CryptEasyCrypt ecPri = new CryptEasyCrypt(rsaKeys.getPrivate(), "RSA");
		      CryptEasyCrypt ecPub = new CryptEasyCrypt(rsaKeys.getPublic(), "RSA");
		       
		      // Text ver- und entschluesseln
		      String text = "Hallo AxxG-Leser";
		      String geheim = ecPri.encrypt(text);
		      String erg = ecPub.decrypt(geheim);
		       
		      System.out.println("Normaler Text:" + text);
		      System.out.println("Geheimer Text:" + geheim);
		      System.out.println("decrypt  Text:" + erg);     
		       
		      // oder
		       
		      text = "Hallo AxxG-Leser";
		      geheim = ecPub.encrypt(text);
		      erg = ecPri.decrypt(geheim);
		       
		      System.out.println("Normaler Text:" + text);
		      System.out.println("Geheimer Text:" + geheim);
		      System.out.println("decrypt  Text:" + erg);        
		   } catch (Exception e) {
		      e.printStackTrace();
		   }
	   }
}
