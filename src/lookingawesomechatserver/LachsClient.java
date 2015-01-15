package lookingawesomechatserver;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import enigma.console.Console;
import enigma.console.TextAttributes;
import enigma.core.Enigma;

public class LachsClient {

	
	
	public static final String VERSION = "2.9 RV2 Alpha";
	public static final String PREFIX = ">";
	public static final String CONFIGFILENAME = "conf.properties";
	
	public Console enigma = null;
	public Color consoleColor = null;
	public LachsClient_ConHandler lcubs = null;
	
	public String serverip = "localhost";
	public int serverport = 12321;	
	String language = "english";
	
	public Utils u = null;
	
	public Object CryptKey = null;
	public CryptMode cryptMode = CryptMode.NONE;
	
	public LachsClient()
	{
		init();		
	}
	
	
	private void init()
	{
		enigma = Enigma.getConsole("L.A.Ch.S Client "+VERSION);
		
		TextAttributes ta_r = new TextAttributes(Color.RED);
		TextAttributes ta_o = new TextAttributes(Color.ORANGE);
		TextAttributes ta_y = new TextAttributes(Color.YELLOW);
		TextAttributes ta_g = new TextAttributes(Color.GREEN);
		TextAttributes ta_p = new TextAttributes(Color.PINK);
		TextAttributes ta_m = new TextAttributes(Color.MAGENTA);
		TextAttributes ta_c = new TextAttributes(Color.CYAN);
		TextAttributes ta_w = new TextAttributes(Color.WHITE);
		TextAttributes ta_b = new TextAttributes(Color.BLUE);

		enigma.setTextAttributes(ta_m);System.out.println("          __        ______    ____      __        ____	  "+VERSION+"");       
		enigma.setTextAttributes(ta_m);System.out.println("         /\\ \\      /\\  _  \\  /\\  _`\\   /\\ \\      /\\  _`\\      ");
		enigma.setTextAttributes(ta_r);System.out.println("         \\ \\ \\     \\ \\ \\L\\ \\ \\ \\ \\/\\_\\ \\ \\ \\___  \\ \\,\\L\\_\\	");   
		enigma.setTextAttributes(ta_o);System.out.println("          \\ \\ \\  __ \\ \\  __ \\ \\ \\ \\/_/_ \\ \\  _ `\\ \\/_\\__ \\	");   
		enigma.setTextAttributes(ta_y);System.out.println("           \\ \\ \\L\\ \\ \\ \\ \\/\\ \\ \\ \\ \\L\\ \\ \\ \\ \\ \\ \\  /\\ \\L\\ \\  ");
		enigma.setTextAttributes(ta_g);System.out.println("            \\ \\____/  \\ \\_\\ \\_\\ \\ \\____/  \\ \\_\\ \\_\\ \\ `\\____\\ ");
		enigma.setTextAttributes(ta_c);System.out.println("             \\/___/    \\/_/\\/_/  \\/___/    \\/_/\\/_/  \\/_____/ ");
		enigma.setTextAttributes(ta_p);System.out.println("               LOOKING     AWESOME     CHAT    SERVICE        \\n");		
		enigma.setTextAttributes(ta_b);System.out.println("            C O M E S   W I T H   E N C R Y P T I O N ! ! !");
		enigma.setTextAttributes(ta_y);System.out.println("                   NOW LESS RANDOM FEATURES!\n\n\n");
		
		
		
		try {			
			consoleColor = Color.green;
			
			LachsFileHandling.loadClientConfig(this, CONFIGFILENAME);
			enigma.setTextAttributes(new TextAttributes(consoleColor));
			u = new Utils(enigma,language);
			
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void startclient()
	{
		
		lcubs = new LachsClient_ConHandler(serverip, serverport, u, this);
		
		u.printGrayln("Attempting connection to "+serverip+":"+serverport+"...");
		lcubs.ConnectToNIOServer(null,null);				
		lcubs.start();
		
		LAChSInputLoop();
		
		lcubs.ConnectionsClose();	
	}
	
	public void startclient(String uname, String passwd)
	{
		
		lcubs = new LachsClient_ConHandler(serverip, serverport, u, this);
		
		u.printGrayln("Attempting connection to "+serverip+":"+serverport+"...");
		lcubs.ConnectToNIOServer(uname,passwd);				
		lcubs.start();
		
		LAChSInputLoop();
		
		lcubs.ConnectionsClose();	
	}
	
	private void LAChSInputLoop()
	{
		String cmd = "";
		try {
			
			while(!lcubs.disconnect)
			{
				System.out.print("chat"+PREFIX);
				cmd = enigma.readLine();
				
				if(lcubs.disconnect)return;
				
				lcubs.encodeMessagePacket(cmd);		        				
			}			
			
		} catch ( Exception e){
			
			e.printStackTrace();
			
		}
	}

	
	private void LCInputLoop()
	{
		String cmd = "";
		String slices[];
		try {
			
			while(true)
			{
				System.out.print("mainmenu"+PREFIX);
				cmd = enigma.readLine();
				
				if(cmd.startsWith("/"))
				{
					cmd = cmd.toLowerCase();
					slices = cmd.split(" ");
					
					switch(slices[0])
					{
						case "/conf":{}
						case "/configure":{actionConfigure(cmd);break;}
						case "/con":{}
						case "/connect":{actionConnect(cmd);break;}						
						case "/q":{}
						case "/quit":{}
						case "/exit":{return;}
						case "/lpl":{actionLPL();break;}
						case "/req":{actionLPL2();break;}						
						case "/cd":{;break;}						
						default:{u.printError("unknown command");}
					}
				}				
			}		
		
		} catch ( Exception e){e.printStackTrace();}
	}
	
	
	private void actionLPL()
	{
		LachsPluginLoader lpl = new LachsPluginLoader(32123, "192.168.1.103",u,false);
		lpl.start();		
	}
	
	private void actionLPL2()
	{
		LachsPluginLoader lpl = new LachsPluginLoader(32123, "192.168.1.103",u,true);
		lpl.FileSendInit(".\\SoundFiles\\epicwincassie.jpg");
	}
	
	private void actionConnect(String cmd)
	{		
		String slices[] = cmd.split(" ");
		
		if(slices.length == 1) this.startclient();// serververbindung mit voreingestellten daten
		else if(slices.length == 3){// serververbindung mit eingabe der verbindungsdaten oder benutzerkennung
			if(cmd.contains(" as "))// verbindung mit benutzerkennung
			{				
				System.out.print("Password:");
				String passwd = enigma.readPassword();
				
				
				String passhash = null;
				try {
				
					passhash = LachsSecurtiy_HASH.generateHashLight(passwd);//LachsSecurtiy_HASH.generateStorngPasswordHash(passwd);
										
				} catch (Exception e) {
					u.printError("Something with hashgeneration went wrong");
				}
				
				
				startclient(slices[2],passhash);
				
				return;
			}
			else // verbindung mit verbindungsdaten
			{	
				serverip = slices[1];
				serverport = Integer.parseInt(slices[2]);
				startclient();
				return;
			}
		}
		else if(slices.length == 5)// serververbindung mit eingabe der verbindungsdaten und benutzerkennung
		{
			if(cmd.contains(" as "))
			{
				serverip = slices[1];
				serverport = Integer.parseInt(slices[2]);
				
				System.out.print("password:");
				String passwd = enigma.readPassword();
				
				String passhash = null;
				try {
					
					passhash = LachsSecurtiy_HASH.generateHashLight(passwd);//.generateStorngPasswordHash(passwd);
										
				} catch (Exception e) {
					u.printError("Something with hashgeneration went wrong");
				}				
				
				startclient(slices[4],passhash);
			}
			else
				u.printError("Wrong usage!\nPlease use: /connect [serverip] [serverport] as [username]");
		}
		else u.printError("Wrong number of arguments!");
	}
	
	public void actionConfigure(String cmd)
	{
		String slices[] = cmd.split(" ");
		if(slices.length == 2)
		{
			if(slices[1].startsWith("sav")){LachsFileHandling.saveClientConfig(this, CONFIGFILENAME);}
		}
		if(slices.length >= 3)
		{
			if(slices[1].startsWith("col")){actionConfigureColors(slices);}
			if(slices[1].startsWith("con")){actionConfigureConnection(slices);}
			if(slices[1].startsWith("cry")){actionConfigureCrypt(slices);}
			if(slices[1].startsWith("lan")){actionConfigureLanguage(slices);}
		}
	}
	private void actionConfigureLanguage(String[] slices)
	{
		System.out.println("changelang");
		
		if(u.getTranslator().changeLanguage(slices[2]))
		{
			language = slices[2];
		}
	}
	private void actionConfigureCrypt(String[] slices)
	{
		if(slices[2].startsWith("mode"))
		{	
			if(slices.length == 4)
			{
				try
				{
					if(slices[3].startsWith("none"))actionConfigureCryptNone();
					if(slices[3].startsWith("caesar"))actionConfigureCryptCaesar();
					if(slices[3].startsWith("vigenere"))actionConfigureCryptVigenere();
					if(slices[3].startsWith("vigenereextended"))actionConfigureCryptVigenereExtended();
					if(slices[3].startsWith("enigma"))actionConfigureCryptEnigma();
					if(slices[3].startsWith("aes128"))actionConfigureCryptAes128();
					
					u.printGrayln("Cryptmode to "+this.cryptMode.toString()+" changed. To disable crypting choose 'None' as CryptMode.");
				}
				catch(Exception e)
				{
					u.printError("Invalid CryptMode. Valid modes are: None, Caesar, Viginere, ViginereExtended, Enigma, AES128.");
				}						
			}
			else u.printError("Not enough arguments.\nPlease use '/configure crypt mode [mode]'\nAvailable Cryptmethods are:None, Caesar, Viginere, ViginereExtended, Enigma, AES128.");
		}
		if(slices[2].startsWith("key"))
		{
			if(slices.length == 4)
			{
				this.CryptKey = slices[3].trim();
				u.printGrayln("CryptKey changed to '"+this.CryptKey.toString()+"'.");
			}
			else u.printError("Not enough arguments.\nPlease use '/configure crypt key [key]'");
		}
	}
	private void actionConfigureCryptAes128()
	{
		u.printGrayln("AES");
		try {
			System.out.print("Generate new KeyFile? y/n:");
			if(enigma.readLine().startsWith("y"))
			{
				CryptKey = CryptAES.generateAESKeyFile("./keys/aes/test.aes");
			}
			else
			{
				CryptKey = CryptAES.importAESKeyFile("./keys/aes/test.aes");
			}
		
			cryptMode = CryptMode.AES128;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void actionConfigureCryptCaesar()
	{
		cryptMode = CryptMode.CEASAR;
	}
	private void actionConfigureCryptVigenere()
	{
		cryptMode = CryptMode.VIGENERE;
	}	
	private void actionConfigureCryptVigenereExtended()
	{
		cryptMode = CryptMode.VIGENEREEXTENDED;
	}
	private void actionConfigureCryptEnigma()
	{
		int r1,r2,r3;
		System.out.println("Please choose your Rotors. Available Rotors are: 1,2,3,4,5");
		System.out.print("Rotor1:");
		r1 = Integer.parseInt(enigma.readLine());
		System.out.print("Rotor2:");
		r2 = Integer.parseInt(enigma.readLine());
		System.out.print("Rotor3:");
		r3 = Integer.parseInt(enigma.readLine());
		
		CryptKey = (CryptEnigma)new CryptEnigma(r1, r2, r3);
		cryptMode = CryptMode.ENIGMA;
	}
	private void actionConfigureCryptNone()
	{
		cryptMode = CryptMode.NONE;
	}
	private void actionConfigureConnection(String[] slices)
	{
		if(slices.length == 4)
		{
			this.serverip = slices[2];
			this.serverport = Integer.parseInt(slices[3]);
			
			u.printGrayln("Settings changed: "+this.serverip+":"+this.serverport);
		}
		else u.printError("Not enough arguments.\nPlease use '/configure connection [serverip] [port]'");
	}
	
	public void actionConfigureColors(String[] slices)
	{
		if(slices.length == 3)
		{
			switch(slices[2].toLowerCase())
			{
				case "blue":consoleColor = Color.blue;break;
				case "green":consoleColor = Color.green;break;
				case "gray":consoleColor = Color.gray;break;
				case "red":consoleColor = Color.red;break;
				case "pink":consoleColor = Color.pink;break;
				case "orange":consoleColor = Color.orange;break;
				case "yellow":consoleColor = Color.yellow;break;
				case "white":consoleColor = Color.white;break;
				case "cyan":consoleColor = Color.cyan;break;
				case "magenta":consoleColor = Color.magenta;break;
				case "black":consoleColor = Color.black;break;
				case "darkgray":consoleColor = Color.darkGray;break;
				case "lightgray":consoleColor = Color.lightGray;break;
				default:{u.printWarning("Color "+slices[2]+" not found..");return;							
				}
			}
			enigma.setTextAttributes(new TextAttributes(consoleColor));
			u.printGrayln("Settings changed! Textcolor is now: "+slices[2].toUpperCase());
		}
		else u.printError("Not enough arguments.\nPlease use '/configure color [color]' \nSome Colors are: blue,green,yellow,orange,red,pink,etc.");
		
	}
	public static void main(String[] args) {
        
    	LachsClient lc = null;
    	
        try {
          
        	lc = new LachsClient();        	
        	lc.LCInputLoop();
        	
        	
        } catch (Exception e) {           
            e.printStackTrace();
        } finally {
                              
              
        }
    } 
	
	
}
