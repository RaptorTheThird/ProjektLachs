package lookingawesomechatserver;


import java.io.*;
import java.security.*;
import javax.crypto.*;

/**
 * 
 * @author Thorsten Horn
 * @see http://www.torsten-horn.de/techdocs/java-crypto.htm
 *
 */
public class CryptRsaAes {
	 static final String ASYMMETRIC_ALGO = "RSA";
	   static final String SYMMETRIC_ALGO  = "AES"; // oder z.B. "Blowfish"

	   public static void main( String[] args ) throws GeneralSecurityException, ClassNotFoundException, IOException
	   {
	      if( args.length == 4 ) {
	         if( args[0].toLowerCase().startsWith( "-g" ) ) {        // generateKeyPair
	            generateKeyPair( args[1], args[2], Integer.parseInt( args[3] ) );
	            return;
	         } else if( args[0].toLowerCase().startsWith( "-e" ) ) { // encrypt
	            encrypt( args[1], args[2], args[3] );
	            return;
	         } else if( args[0].toLowerCase().startsWith( "-d" ) ) { // decrypt
	            decrypt( args[1], args[2], args[3] );
	            return;
	         }
	      }
	      System.out.println( "\nFehler: Es werden vier Parameter benoetigt:\n" +
	            "Zum Generieren des privaten und oeffentlichen RSA-Schluessels (RsaKeySize z.B. 2048):\n" +
	            "  -g PrivateKeyFile PublicKeyFile RsaKeySize\n" +
	            "Zum Verschluesseln:\n" +
	            "  -e PublicKeyFile InputFile EncryptedFile\n" +
	            "Zum Entschluesseln:\n" +
	            "  -d PrivateKeyFile EncryptedFile OutputFile\n" );
	   }

	   /** Generiere privaten und oeffentlichen RSA-Schluessel */
	   public static void generateKeyPair( String privateKeyFile, String publicKeyFile, int rsaKeySize )
	         throws NoSuchAlgorithmException, IOException
	   {
	      generateKeyPair( new FileOutputStream( privateKeyFile ), new FileOutputStream( publicKeyFile ), rsaKeySize );
	   }

	   /** Generiere privaten und oeffentlichen RSA-Schluessel (Streams werden mit close() geschlossen) */
	   public static void generateKeyPair( OutputStream privateKeyFile, OutputStream publicKeyFile, int rsaKeySize )
	         throws NoSuchAlgorithmException, IOException
	   {
	      try {
	         KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance( ASYMMETRIC_ALGO );
	         keyPairGen.initialize( rsaKeySize );
	         KeyPair keyPair = keyPairGen.generateKeyPair();
	         ObjectOutputStream out = new ObjectOutputStream( publicKeyFile );
	         try { out.writeObject( keyPair.getPublic() ); } finally { out.close(); }
	         out = new ObjectOutputStream( privateKeyFile );
	         try { out.writeObject( keyPair.getPrivate() ); } finally { out.close(); }
	      } finally {
	         privateKeyFile.close();
	         publicKeyFile.close();
	      }
	   }

	   /** Verschluesseln */
	   public static void encrypt( String publicKeyFile, String inputFile, String encryptedFile )
	         throws GeneralSecurityException, ClassNotFoundException, IOException
	   {
	      encrypt( new FileInputStream( publicKeyFile ), new FileInputStream( inputFile ), new FileOutputStream( encryptedFile ) );
	   }

	   /** Verschluesseln (Streams werden mit close() geschlossen) */
	   public static void encrypt( InputStream publicKeyFile, InputStream inputFile, OutputStream encryptedFile )
	         throws GeneralSecurityException, ClassNotFoundException, IOException
	   {
	      try {
	         KeyGenerator keyGen = KeyGenerator.getInstance( SYMMETRIC_ALGO );
	         keyGen.init( Math.min( 256, Cipher.getMaxAllowedKeyLength( SYMMETRIC_ALGO ) ) );
	         SecretKey symKey = keyGen.generateKey();
	         Key publicKey;

	         ObjectInputStream keyIn = new ObjectInputStream( publicKeyFile );
	         try { publicKey = (Key) keyIn.readObject(); } finally { keyIn.close(); }

	         Cipher cipher = Cipher.getInstance( ASYMMETRIC_ALGO );
	         cipher.init( Cipher.WRAP_MODE, publicKey );
	         byte[] wrappedKey = cipher.wrap( symKey );

	         DataOutputStream out = new DataOutputStream( encryptedFile );
	         try {
	            out.writeInt( wrappedKey.length );
	            out.write( wrappedKey );
	            cipher = Cipher.getInstance( SYMMETRIC_ALGO );
	            cipher.init( Cipher.ENCRYPT_MODE, symKey );
	            transform( inputFile, out, cipher );
	         } finally {
	            out.close();
	         }
	      } finally {
	         publicKeyFile.close();
	         inputFile.close();
	         encryptedFile.close();
	      }
	   }

	   /** Entschluesseln */
	   public static void decrypt( String privateKeyFile, String encryptedFile, String outputFile )
	         throws GeneralSecurityException, ClassNotFoundException, IOException
	   {
	      decrypt( new FileInputStream( privateKeyFile ), new FileInputStream( encryptedFile ), new FileOutputStream( outputFile ) );
	   }

	   /** Entschluesseln (Streams werden mit close() geschlossen) */
	   public static void decrypt( InputStream privateKeyFile, InputStream encryptedFile, OutputStream outputFile )
	         throws GeneralSecurityException, ClassNotFoundException, IOException
	   {
	      try {
	         DataInputStream in = new DataInputStream( encryptedFile );
	         try {
	            int length = in.readInt();
	            byte[] wrappedKey = new byte[length];
	            in.read( wrappedKey, 0, length );

	            Key privateKey;
	            ObjectInputStream keyIn = new ObjectInputStream( privateKeyFile );
	            try { privateKey = (Key) keyIn.readObject(); } finally { keyIn.close(); }

	            Cipher cipher = Cipher.getInstance( ASYMMETRIC_ALGO );
	            cipher.init( Cipher.UNWRAP_MODE, privateKey );
	            Key symKey = cipher.unwrap( wrappedKey, SYMMETRIC_ALGO, Cipher.SECRET_KEY );

	            cipher = Cipher.getInstance( SYMMETRIC_ALGO );
	            cipher.init( Cipher.DECRYPT_MODE, symKey );
	            transform( in, outputFile, cipher );
	         } finally {
	            in.close();
	         }
	      } finally {
	         privateKeyFile.close();
	         encryptedFile.close();
	         outputFile.close();
	      }
	   }

	   private static void transform( InputStream in, OutputStream out, Cipher cipher )
	         throws IOException, GeneralSecurityException
	   {
	      int    blockSize = cipher.getBlockSize();
	      byte[] input     = new byte[blockSize];
	      byte[] output    = new byte[cipher.getOutputSize( blockSize )];
	      int    len;
	      while( (len = in.read( input )) == blockSize ) {
	         int outLength = cipher.update( input, 0, blockSize, output );
	         out.write( output, 0, outLength );
	      }
	      out.write( ( len > 0 ) ? cipher.doFinal( input, 0, len ) : cipher.doFinal() );
	   }
}
