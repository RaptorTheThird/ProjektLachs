package lookingawesomechatserver;

import java.util.Scanner;

public class Crypt1337{
	public static String encrypting(String text){	
		text = text.replace("a", "4");
		text = text.replace("A", "4");
		text = text.replace("b", "8");
		text = text.replace("B", "8");
		text = text.replace("c", "(");
		text = text.replace("C", "(");
		text = text.replace("d", "|)");
		text = text.replace("D", "|)");
		text = text.replace("e", "3");
		text = text.replace("E", "3");
		text = text.replace("f", "F");
		text = text.replace("F", "F");
		text = text.replace("g", "6");
		text = text.replace("G", "6");
		text = text.replace("h", "|-|");
		text = text.replace("H", "|-|");
		text = text.replace("i", "|");
		text = text.replace("I", "|");
		text = text.replace("j", "_|");
		text = text.replace("J", "_|");
		text = text.replace("k", "|<");
		text = text.replace("K", "|<");
		text = text.replace("l", "1");
		text = text.replace("L", "1");
		text = text.replace("m", "|V|");
		text = text.replace("M", "|V|");
		text = text.replace("n", "/V");
		text = text.replace("N", "/V");
		text = text.replace("o", "0");
		text = text.replace("O", "0");
		text = text.replace("p", "|°");
		text = text.replace("P", "|°");
		text = text.replace("q", "Q");
		text = text.replace("Q", "Q");
		text = text.replace("r", "R");
		text = text.replace("R", "R");
		text = text.replace("s", "5");
		text = text.replace("S", "5");
		text = text.replace("t", "7");
		text = text.replace("T", "7");
		text = text.replace("u", "|_|");
		text = text.replace("U", "|_|");
		text = text.replace("v", "V");
		text = text.replace("V", "V");
		text = text.replace("w", "VV");
		text = text.replace("W", "VV");
		text = text.replace("x", "}{");
		text = text.replace("X", "}{");
		text = text.replace("y", "Y");
		text = text.replace("Y", "Y");
		text = text.replace("z", "2");
		text = text.replace("Z", "2");
			
		
		text = text.replace(".", " Stop ");
		text = text.replace("ä", "43");
		text = text.replace("ö", "03");
		text = text.replace("ü", "|_|3");
		text = text.replace("ß", "ss");
	    text = text.replace("Ä", "43");
	    text = text.replace("Ö", "03");
	    text = text.replace("Ü", "|_|3");	    
		return text;
	}		
	public static String decrypting(String text){
	   	return text; 																
	 }			
	
	public static void main(String[] args){
    	String text;
        Scanner scanner = new Scanner(System.in);	
        
        System.out.println("Zu verschlüsselnden Text eingeben:");
        text=encrypting(scanner.nextLine());
        
        System.out.println("Der Text sieht verschlüsselt so aus:");
        System.out.println(text);
        
        scanner.close();		 
	}
}