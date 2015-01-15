package lookingawesomechatserver;
import java.util.Scanner;

public class CryptVigenereExtended {		   
		    public static String encrypt(String message, String skey){
		    	
		    	char[] plain = message.toCharArray();
		    	char[] key = skey.toCharArray();
		    	
		    	char[] rueckgabe = new char[plain.length];
		    	int resultat;
		    	for (int i = 0; i < plain.length; i++){
		   			resultat =  (plain[i] + key[i % key.length]) % 128;
		    		rueckgabe[i]= (char)resultat;
		      	}
		    	return rueckgabe.toString();
		    }
		    
		    public static String decrypt(String message, String skey){
		    	
		    	char[] plain = message.toCharArray();
		    	char[] key = skey.toCharArray();
		    	
		    	char[] rueckgabe = new char[plain.length];
		    	int result;
		    	for (int i = 0; i < plain.length; i++){
		    		if (plain[i] - key[i % key.length] < 0) {
		    			result = (plain[i] - key[i % key.length]) + 128;
					}else{ result = (plain[i] - key[i % key.length]) % 128; }
						rueckgabe[i]=(char)result;
		    		}
		    	return rueckgabe.toString();
		    }
//		    	    
//		    public static void main(String[] args)  {
//		    	String text, keytext;
//		    	char[] key, encrypted, decrypted, plain;
//		        Scanner scanner = new Scanner(System.in);	
//		        
//		        System.out.println("Zu verschlüsselnden Text eingeben:");
//		        text = scanner.nextLine();		 
//		        plain = text.toCharArray();
//		        
//		        System.out.println("Key zum verschlüsseln eingeben:");
//		        keytext = scanner.nextLine();
//		        key = keytext.toCharArray(); 
//		        
//		        encrypted = encrypting(plain, key);
//		        System.out.println("Der Text sieht verschlüsselt so aus:");
//		        System.out.println(encrypted);
//		        
//		        decrypted = decrypting(encrypted, key);
//		        System.out.println("Und zur Probe wieder zurückentschlüsselt:");		 
//		        System.out.println(decrypted);
//		        scanner.close();		 
//	}
}		 