package lookingawesomechatserver;

public class CryptCaesar {
		
	public static String encrypt(String message, String skey) throws NumberFormatException{
		
		int key = Integer.parseInt(skey);		
		char[] oldMessage = message.toCharArray();
		char[] newMessage = new char[message.length()];
		
		for(int i=0; i<message.length();i++){
			int neu;
		
			if(oldMessage[i]<91){
				 neu = ((oldMessage[i]+key-'A')%26)+'A';
			}
			else{
				 neu = ((oldMessage[i]+key-'a')%26)+'a';
			}
			
			newMessage[i] =(char)(neu);			
		}
		return new String(newMessage);
	}
	
	public static String decrypt(String encryptedMessage, String skey ) throws NumberFormatException{

		int key = Integer.parseInt(skey);		
		char[] oldMessage = encryptedMessage.toCharArray();
		char[] newMessage = new char[encryptedMessage.length()];
		
		for(int i=0; i<encryptedMessage.length();i++){
			int niew;
			if(oldMessage[i]<91){
				if(((oldMessage[i]-key-'A')%26)<0){ 
					niew = ((oldMessage[i]-key-'A')%26)+'Z'+1;
				}
				else{
					niew = ((oldMessage[i]-key-'A')%26)+'A';		
				}
			}
			else{
				if(((oldMessage[i]-key-'a')%26)<0){ 
					niew = ((oldMessage[i]-key-'a')%26)+'z'+1;
				}
				else{
					niew = ((oldMessage[i]-key-'a')%26)+'a';
				}
			}
			newMessage[i] =(char)(niew);
		}
		
		
		return new String(newMessage);
	}
	
	


}
