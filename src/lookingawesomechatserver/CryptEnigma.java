package lookingawesomechatserver;


public class CryptEnigma {
	
	char[] r1 = new char[52];
	char[] r2 = new char[52];
	char[] r3 = new char[52];
	char[] ro1 = {'k','X','Y','P','z','r','c','T','e','b','o','U','L','v','Z','I','A','E','g','p','j','M','w','h','K','q','B','H','a','t','V','d','m','G','u','s','i','l','f','Q','x','y','W','C','J','S','F','R','N','n','D','O'};
	char[] ro2 = {'E','I','n','C','z','H','A','V','j','i','m','Q','G','v','p','W','c','M','X','T','Z','x','F','R','l','q','S','u','d','w','U','h','K','N','g','t','e','r','O','f','y','Y','D','b','B','o','s','J','L','a','k','P'};
	char[] ro3 = {'q','c','e','G','l','z','g','b','t','V','d','F','O','C','u','W','S','D','Y','R','P','Z','J','Q','E','K','i','n','x','v','p','k','m','s','X','H','w','r','I','L','y','N','h','T','M','U','o','f','A','a','B','j'};
	char[] ro4 = {'s','m','x','b','Y','u','t','C','U','O','z','k','X','Q','e','a','I','i','N','R','D','n','A','W','P','r','d','G','E','q','w','l','o','f','y','B','T','h','H','S','Z','p','V','K','j','v','M','J','c','g','L','F'};
	char[] ro5 = {'d','G','O','w','Q','l','a','K','C','Y','m','u','N','S','f','P','H','o','g','B','q','t','V','X','j','F','E','A','n','k','T','Z','U','r','i','y','R','h','M','I','L','J','p','e','b','W','v','D','x','z','s','c'};
		
	
	
	
	public CryptEnigma(int rotor1, int rotor2, int rotor3){
		
		 switch(rotor1) {
		 case 1: 
			 	 r1=ro1;break;
		 case 2: 
			 	 r1=ro2;break;
		 case 3: 
			 	 r1=ro3;break;
		 case 4: 
			 	 r1=ro4;break;
		 case 5:
		 		 r1=ro5;break;
			 
		 }
		 switch(rotor2) {
		 case 1: 
			 	 r2=ro1;break;
		 case 2: 
			 	 r2=ro2;break;
		 case 3: 
			 	 r2=ro3;break;
		 case 4: 
			 	 r2=ro4;break;
		 case 5:
		 		 r2=ro5;break;
			 
		 }
		 switch(rotor3) {
		 case 1: 
			 	 r3=ro1;break;
		 case 2: 
			 	 r3=ro2;break;
		 case 3: 
			 	 r3=ro3;break;
		 case 4: 
			 	 r3=ro4;break;
		 case 5:
		 		 r3=ro5;break;
		 }
	}
		
	private int suche(char x, char[] r){
		for(int i = 0;i<r.length;i++){
			if(r[i]==x){
				return i;
			}
		}
		return -1;
	}
	
	public String encrypt(String message){
		
		message = CryptVigenere.unicoder(message);
		
		char[] verschluesselt = new char[message.length()];
		for(int i =0; i<message.length();i++){
			if(Character.isLetter(message.charAt(i))){
				int a =suche(message.charAt(i), r1);
				if((!(r2[a]=='z'))&&(!(r2[a]=='Z'))){
					a =suche((char)(r2[a]+1), r2);
					verschluesselt[i]=r1[a];
				}
				else{
					if((r2[a]=='z')){
					a =suche('A', r2);
					verschluesselt[i]=r1[a];
					}
					else{
						a =suche('a', r2);
						verschluesselt[i]=r1[a];
					}
				}
			}
			else{
				
				verschluesselt[i]=message.charAt(i);
			}
		}
		return new String(verschluesselt);
	}
		
	public String decrypt(String verschluesselter_text){
		char[] entschluesselt = new char[verschluesselter_text.length()];
		for(int i =0; i<verschluesselter_text.length();i++){
			if(Character.isLetter(verschluesselter_text.charAt(i))){
				int a =suche(verschluesselter_text.charAt(i), r1);
				if((!(r2[a]=='a'))&&(!(r2[a]=='A'))){
					a =suche((char)(r2[a]-1), r2);
					entschluesselt[i]=r1[a];
				}
				else{
					if((r2[a]=='a')){
					a =suche('Z', r2);
					entschluesselt[i]=r1[a];
					}
					else{
						a =suche('z', r2);
						entschluesselt[i]=r1[a];
					}
					
				}
			}
			else{
				
				entschluesselt[i]=verschluesselter_text.charAt(i);
			}
		}
		return new String(entschluesselt);
	}
	
}
//package lookingawesomechatserver;
//
//
//public class CryptEnigma {
//	
//	String message;
//	char[] r1 = new char[52];
//	char[] r2 = new char[52];
//	char[] ro1 = {'k','X','Y','P','z','r','c','T','e','b','o','U','L','v','Z','I','A','E','g','p','j','M','w','h','K','q','B','H','a','t','V','d','m','G','u','s','i','l','f','Q','x','y','W','C','J','S','F','R','N','n','D','O'};
//	char[] ro2 = {'E','I','n','C','z','H','A','V','j','i','m','Q','G','v','p','W','c','M','X','T','Z','x','F','R','l','q','S','u','d','w','U','h','K','N','g','t','e','r','O','f','y','Y','D','b','B','o','s','J','L','a','k','P'};
//	char[] ro3 = {'q','c','e','G','l','z','g','b','t','V','d','F','O','C','u','W','S','D','Y','R','P','Z','J','Q','E','K','i','n','x','v','p','k','m','s','X','H','w','r','I','L','y','N','h','T','M','U','o','f','A','a','B','j'};
//	char[] ro4 = {'s','m','x','b','Y','u','t','C','U','O','z','k','X','Q','e','a','I','i','N','R','D','n','A','W','P','r','d','G','E','q','w','l','o','f','y','B','T','h','H','S','Z','p','V','K','j','v','M','J','c','g','L','F'};
//	char[] ro5 = {'d','G','O','w','Q','l','a','K','C','Y','m','u','N','S','f','P','H','o','g','B','q','t','V','X','j','F','E','A','n','k','T','Z','U','r','i','y','R','h','M','I','L','J','p','e','b','W','v','D','x','z','s','c'};
//		
//	
//	
//	
//	public CryptEnigma(String benutzereingabe, int rotor1, int rotor2, int rotor3){
//		
//		 message = benutzereingabe;
//		 switch(rotor1) {
//		 case 1: 
//			 	 r1=ro1;break;
//		 case 2: 
//			 	 r1=ro2;break;
//		 case 3: 
//			 	 r1=ro3;break;
//		 case 4: 
//			 	 r1=ro4;break;
//		 case 5:
//		 		 r1=ro5;break;
//			 
//		 }
//		 switch(rotor3) {
//		 case 1: 
//			 	 r2=ro1;break;
//		 case 2: 
//			 	 r2=ro2;break;
//		 case 3: 
//			 	 r2=ro3;break;
//		 case 4: 
//			 	 r2=ro4;break;
//		 case 5:
//		 		 r2=ro5;break;
//			 
//		 }
//	}
//		
//	private int suche(char x, char[] r){
//		for(int i = 0;i<r.length;i++){
//			if(r[i]==x){
//				return i;
//			}
//		}
//		return -1;
//	}
//	
//	public String verschluesseln(){
//		char[] verschluesselt = new char[message.length()];
//		for(int i =0; i<message.length();i++){
//			if(Character.isLetter(message.charAt(i))){
//				int a =suche(message.charAt(i), r1);
//				if((!(r2[a]=='z'))&&(!(r2[a]=='Z'))){
//					a =suche((char)(r2[a]+1), r2);
//					verschluesselt[i]=r1[a];
//				}
//				else{
//					if((r2[a]=='z')){
//					a =suche('A', r2);
//					verschluesselt[i]=r1[a];
//					}
//					else{
//						a =suche('a', r2);
//						verschluesselt[i]=r1[a];
//					}
//				}
//			}
//			else{
//				
//				verschluesselt[i]=message.charAt(i);
//			}
//		}
//		return new String(verschluesselt);
//	}
//		
//	public String entschluesseln(String verschluesselter_text){
//		char[] entschluesselt = new char[verschluesselter_text.length()];
//		for(int i =0; i<verschluesselter_text.length();i++){
//			if(Character.isLetter(verschluesselter_text.charAt(i))){
//				int a =suche(verschluesselter_text.charAt(i), r1);
//				if((!(r2[a]=='a'))&&(!(r2[a]=='A'))){
//					a =suche((char)(r2[a]-1), r2);
//					entschluesselt[i]=r1[a];
//				}
//				else{
//					if((r2[a]=='a')){
//					a =suche('Z', r2);
//					entschluesselt[i]=r1[a];
//					}
//					else{
//						a =suche('z', r2);
//						entschluesselt[i]=r1[a];
//					}
//					
//				}
//			}
//			else{
//				
//				entschluesselt[i]=verschluesselter_text.charAt(i);
//			}
//		}
//		return new String(entschluesselt);
//	}
//	
//}
