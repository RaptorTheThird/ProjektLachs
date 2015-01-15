package lookingawesomechatserver;


public class CryptVigenere {	
	
	
	public static String encrypt(String message, String skey){
    	
    	char[] plain = message.toCharArray();
    	char[] key = skey.toCharArray();
    	
    	char[] rueckgabe = new char[plain.length];
		int resultat;
		for (int i = 0; i < plain.length; i++){ 											//Laeuft die laengedes Char-Arrays ab um die Zeichen nacheinadner zu verschluesseln
			if(Character.isLetter(plain[i])){ 												//Fragt nach ob das Zeichen ein Buchstabe ist oder nicht
				if(plain[i] < 91){															//Ascii Tabelle 91 sind die kleinen Buchstaben, hier wird unterschieden
			    	resultat = (( plain[i] - 52 + key[i % key.length]) %26 ) + 65; 			// durch testen die verschiedenen Werte rausgefunden
			    }else{resultat = (( plain[i] - 90 + key[i % key.length]) %26 )  + 97;}		// Hier werden die Gro�buchstaben behandelt
			   	rueckgabe[i]= (char)resultat; 												//Der Eintrag vom Resultat in den Char-Array Rueckgabe
		    }else{rueckgabe[i]= plain[i];}  												// Falls nichts getan wurde der Eintrag in den Char-Array
		}
		return rueckgabe.toString(); 	
    }
	
	public static String decrypt(String message, String skey){
    	
    	char[] plain = message.toCharArray();
    	char[] key = skey.toCharArray();
    	
    	char[] rueckgabe = new char[plain.length];
		int resultat;
		for (int i = 0; i < plain.length; i++){												//Laeuft die laengedes Char-Arrays ab um die Zeichen nacheinadner zu verschluesseln
    		if(Character.isLetter(plain[i])){												//Fragt nach ob das Zeichen ein Buchstabe ist oder nicht
    			if( plain[i] < 91 ){														//Ascii Tabelle 91 sind die kleinen Buchstaben, hier wird unterschieden
    				resultat = (( plain[i] + 52 - key[i % key.length] ) %26 ) + 65;			// durch testen die verschiedenen Werte rausgefunden
			    	}else{resultat = (( plain[i] + 78 - key[i % key.length] ) %26  ) + 97;}	// Hier werden die Gro�buchstaben behandelt
			    	rueckgabe[i]=(char)resultat; 											//Der Eintrag vom Resultat in den Char-Array Rueckgabe
			    }else{rueckgabe[i]= plain[i];}  											//Falls nichts getan wurde der Eintrag in den Char-Array
		    }
	   	return rueckgabe.toString(); 	
    }
	
	/* Die Auswechselfunktion von vorgegebenen Zeichen im String */
	public static String unicoder(String text){
	    text = text.replace("0", "Null");
		text = text.replace("1", "Eins");
		text = text.replace("2", "Zwei");
		text = text.replace("3", "Drei");
	    text = text.replace("4", "Vier");
	    text = text.replace("5", "F�nf");
	    text = text.replace("6", "Sechs");
	    text = text.replace("7", "Sieben");
		text = text.replace("8", "Acht");
		text = text.replace("9", "Neun");
		text = text.replace(".", " Stop ");
		text = text.replace("�", "ae");
		text = text.replace("�", "oe");
		text = text.replace("�", "ue");
		text = text.replace("�", "ss");
	    text = text.replace("�", "Ae");
	    text = text.replace("�", "Oe");
	    text = text.replace("�", "Ue");	    
		return text;
	}
}		 