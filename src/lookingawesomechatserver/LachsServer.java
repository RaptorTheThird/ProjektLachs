package lookingawesomechatserver;

import java.awt.Color;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import enigma.core.Enigma;
import enigma.console.*;

/**
 * @author RaptorTheThird
 *
 */
public class LachsServer {

	// CONNECTION HANDLING
	public static final int USER_CONNECT_ANONYMOUS = 101;
	public static final int USER_CONNECT = 102;
	public static final int USER_CONNECT_CONFIRM = 110;	
	public static final int USER_CONNECT_REFUSE = 111;	
	// USER HANDLING
	public static final int USER_REGISTER_CONFIRM = 119;
	public static final int USER_REGISTER = 120;
	public static final int USER_RENAME = 121;
	public static final int USER_CHANGEPASSWORD = 122;
	public static final int USER_DELETE = 123;
	public static final int USER_PROMOTE = 124;
	public static final int USER_DEMOTE = 125;	
	// CLIENT MESSAGES
	public static final int SEND_NORMALCHAT = 201;	
	public static final int SEND_UNICAST = 202; // PRIVATE MESSAGE
	public static final int SEND_MULTICAST = 203; // MULTI PRIVATE MESSAGE
	// SERVER MESSAGES
	public static final int SERVER_SEND_BROADCAST = 999;
	// ERROR MESSAGES
	public static final int SEND_ERROR_MESSAGE = 1000;
	public static final int ERROR_WRONGUSERPASSWORD = 1001;
	public static final int ERROR_USERALREADYCONNECTED = 1002;
	public static final int ERROR_USERNAMETAKEN = 1003;
	public static final int ERROR_SYNTAX = 1004;
	public static final int ERROR_ALREADYREGISTERED = 1005;
	public static final int ERROR_INSUFFICENTRIGHTS = 1006;
	// INFO MESSAGES
	public static final int SEND_INFO_MESSAGE = 2000;
	public static final int INFO_USERCONNECTED = 2001;
	public static final int INFO_USERDISCONNECTED = 2002;
	public static final int INFO_USERRENAMED = 2003;
	public static final int INFO_SEND_USERLIST = 2004;
	
	// COLORS
	public static final Color COLOR_NAME_OWNER = Color.ORANGE;
	public static final Color COLOR_NAME_ADMIN = Color.RED;
	public static final Color COLOR_NAME_MOD = Color.YELLOW;
	public static final Color COLOR_NAME_USER = Color.CYAN;
	public static final Color COLOR_NAME_GUEST = Color.GRAY;
	
	//DEBUG
	public static final int DBG_LPB_INIT = 32000;
	public static final int DBG_LPB_CONFIRM = 32001;
	
	
	public static final String LACHSUSERDBFILE = ".\\lachsUserDB.tin";
	public static final String LACHSSOUNDFOLDER = ".\\SoundFiles\\";
	public static final String LACHSLOGFOLDER = ".\\logs\\";

	
	// SETTINGS
	public static final boolean ALLOWDOUBLEUSER = false; // TODO: implement
	public static final boolean ALLOWGUESTS = true; // TODO: implement
	public static final boolean USEBASE64 = true;	
	
	public static final String VERSION = "2.9 RV2 Alpha";
	public static final String PREFIX = "->";			
	
	public static int SECURITY_PWHASH_ITERATIONS = 1000;
	public static String SECURTIY_PWHASH_SALT = "L4Ch5";	
	
	private static int serverport = 12321;
	private static String serverip = "localhost";
	
	private static LachsServer_ConHandler ubs;
	
	private LachsUpdateService lus;
	
	Console enigma = null;
	Utils u = null;
	
	public ArrayList<LachsUser> LachsUserDB = null;
	
	public LachsLogger log = null;
	
	public LachsServer()
	{
		init();
		initLachsUser();
		
		mainloop();		
		
		int i = 1;i++;//ignoreme
	}
	
	
	public static final void main(String... aArgs) throws IOException{
			
		new LachsServer();			
	}
	

	/** 
	 * Initialisierung von allem wichtigen..  
	 */
	private void init(){
		
		enigma = Enigma.getConsole("L.A.Ch.Server "+VERSION);
		u = new Utils(enigma, "english");
		
		
		TextAttributes ta_r = new TextAttributes(Color.RED);
		TextAttributes ta_o = new TextAttributes(Color.ORANGE);
		TextAttributes ta_y = new TextAttributes(Color.YELLOW);
		TextAttributes ta_g = new TextAttributes(Color.GREEN);
		TextAttributes ta_d = new TextAttributes(Color.red);
		TextAttributes ta_p = new TextAttributes(Color.PINK);
		TextAttributes ta_m = new TextAttributes(Color.MAGENTA);
		TextAttributes ta_c = new TextAttributes(Color.CYAN);
		TextAttributes ta_w = new TextAttributes(Color.WHITE);
		
		
		System.out.println("\n");
		enigma.setTextAttributes(ta_m);System.out.println("          __        ______    ____      __        ____	  "+VERSION+"");       
		enigma.setTextAttributes(ta_m);System.out.println("         /\\ \\      /\\  _  \\  /\\  _`\\   /\\ \\      /\\  _`\\      ");
		enigma.setTextAttributes(ta_r);System.out.println("         \\ \\ \\     \\ \\ \\L\\ \\ \\ \\ \\/\\_\\ \\ \\ \\___  \\ \\,\\L\\_\\	");   
		enigma.setTextAttributes(ta_o);System.out.println("          \\ \\ \\  __ \\ \\  __ \\ \\ \\ \\/_/_ \\ \\  _ `\\ \\/_\\__ \\	");   
		enigma.setTextAttributes(ta_y);System.out.println("           \\ \\ \\L\\ \\ \\ \\ \\/\\ \\ \\ \\ \\L\\ \\ \\ \\ \\ \\ \\  /\\ \\L\\ \\  ");
		enigma.setTextAttributes(ta_g);System.out.println("            \\ \\____/  \\ \\_\\ \\_\\ \\ \\____/  \\ \\_\\ \\_\\ \\ `\\____\\ ");
		enigma.setTextAttributes(ta_c);System.out.println("             \\/___/    \\/_/\\/_/  \\/___/    \\/_/\\/_/  \\/_____/ ");
		enigma.setTextAttributes(ta_p);System.out.println("               LOOKING       AWESOME       CHAT      SERVER\n\n\n");
		
		try {
			
			
			ubs = new LachsServer_ConHandler(serverport, null, this);
			log = new LachsLogger();
			
			lus = new LachsUpdateService(null, 32123, u, true);
			
			
			ubs.start();
			
		} catch (Exception ex) {
			
			ex.printStackTrace();
		}		
	}
	
	/**
	 * Initialisierung der Benutzerdatenbank.
	 */
	private void initLachsUser()
	{
		
		if((LachsUserDB = LachsFileHandling.getAllLachsUser(LACHSUSERDBFILE)) != null){
			
		}
		else{			
			LachsUserDB = new ArrayList<LachsUser>();			
			try {
			
				LachsUserDB.add(new LachsUser("User1",LachsSecurtiy_HASH.generateHashLight("user"),LachsUserRights.USER));		
				LachsUserDB.add(new LachsUser("Admin",LachsSecurtiy_HASH.generateHashLight("admin"),LachsUserRights.ADMIN));
							
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	/**
	 * Menufunktion listens die console und wartet auf befehle
	 * 
	 * 
	 */
	private void mainloop()
	{
		String cmd = null;
		int exit = 0;
		
		while(true)
		{	
			System.out.print(PREFIX);
			cmd = enigma.readLine();
			
			String[] cmd_h = cmd.split(" ");
			if(cmd_h == null){
				System.out.println(PREFIX+"ungueltige eingabe..\n");
				continue;
			}
			
			switch(cmd_h[0].toLowerCase())
			{			 	
				case "/exit":{exit = 1; break;}				
				case "/start":{ubs.start();break;}
				case "/savelachsdb":{LachsFileHandling.saveAllLachsUser(LachsUserDB, LACHSUSERDBFILE);break;}
				default:{					
					if(cmd.startsWith("/")) System.out.println("Error:"+PREFIX+cmd+" Befehl nicht gefunden..");
					else System.out.println("Chat"+PREFIX+cmd+"\n");
				}
			}			
			 
			
			if(exit == 1)break;
		}
	}
}
