package lookingawesomechatserver;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.Scanner;

import enigma.console.Console;
import enigma.console.TextAttributes;



/**
 * @author RaptorTheThird
 *
 */
public class LachsClient_ConHandler extends Thread {

	
	String server = "localhost";
	String input = "";
	String username = "";	
	
	
	LachsClient parent = null;
	Utils u = null;
	
	boolean disconnect = false;
	
	int port = 0;
	
	SocketChannel channel = null;
	
	public LachsClient_ConHandler(String server, int port, Utils u, LachsClient parent) 
	{
		this.port = port;
		this.server = server;				
		this.parent = parent;
		this.u = u;
	}
	
	public void run()
	{
		String cmd = "";
		
		try {
			
			while(!disconnect)
			{
				cmd = read();
	      	    decodeMessagePacket(cmd);
			}
		} catch (IOException e) {			
			u.translatedSystemMessageOutput("ERROR_CONNECTIONINTERRUPTED", Color.PINK, "zonk");
			ConnectionsClose();		
		} catch (Exception e) {
			ConnectionsClose();
		}		
	}
	
	/**
	*	REDBUTTON:
	*	Press to end connection :3	*	
	*/
	public void ConnectionsClose()
	{
		try {
			
			disconnect = true;
			if(channel != null && !channel.isConnected()) channel.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void ConnectToNIOServer(String uname,String passwd) {
		try {			

			disconnect = false;			
			channel = SocketChannel.open();			
			channel.connect(new InetSocketAddress(server,port));
			channel.configureBlocking(true);
			
			
			if(uname != null){send(LachsServer.USER_CONNECT+"#"+uname+"#"+passwd);}
			else{send(LachsServer.USER_CONNECT_ANONYMOUS+"#Hi");}
			
		} catch (UnknownHostException uhe) {			
			u.translatedSystemMessageOutput("ERROR_UNKNOWNIP", Color.PINK, "zonk");
		} catch (ConnectException ce) {
			u.translatedSystemMessageOutput("ERROR_SERVERNOTREACHABLE", Color.PINK, "zonk");					
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public String read() throws IOException{
		
			ByteBuffer buff = ByteBuffer.allocate(1024);
			
			String Return = "";
			
			int readBytes = channel.read(buff);
			buff.flip();
			
			Return +=  new String(buff.array(),java.nio.charset.StandardCharsets.UTF_8);
			
			buff.compact();
			return Return.trim();
		}

	private void send( String text) {
		
		if( text.charAt( text.length() - 1) != '\n')
			text = text + "\n";
		try {
			
			channel.write(ByteBuffer.wrap(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
			
		} catch( IOException e) {
			u.translatedSystemMessageOutput("ERROR_COULDNOTSENDMESSAGE", Color.RED, "zonk");
		}
	}
	
	
	public void encodeMessagePacket(String message)
	{		
		if(message.startsWith("/"))
		{
			String slices[] = message.split(" ");
			
			switch(slices[0])
			{
			case "/dbg":{send(message.substring(slices[0].length()+1));break;}
			case "/conf":{parent.actionConfigure(message);break;}
			case "/pm":{actionSendPrivateMessage(message);break;}
			case "/pmm":{actionSendMultiPrivateMessage(message);break;}
			case "/broadcast":{actionSendBroadcast(message);break;}
			case "/lpb":{actionLPBInit(message);break;}
			case "/exit":{}
			case "/bye":{}
			case "/quit":{}
			case "/q":{actionDisconnect(message);break;}
			case "/register":{actionRegister(message);break;}
			case "/userlist":{send(LachsServer.INFO_SEND_USERLIST+"#userlist");break;}			
			default:{}			
			}
			return;
		}
		message = message.trim();
		if(!message.isEmpty())
		{
			send(LachsServer.SEND_NORMALCHAT+"#"+encodeMessage(message));
		}
	}
	
	private String encodeMessage(String message)
	{
		switch(parent.cryptMode)
		{
			case CEASAR:{
				try 
				{
					return(CryptCaesar.encrypt(message, parent.CryptKey.toString()));
				} 
				catch (NumberFormatException e){u.translatedSystemMessageOutput("ERROR_INVALIDCRYPTKEY", Color.PINK, "zonk");}
				
				break;
			}
			case VIGENERE:{					
				try 
				{
					return(CryptVigenere.encrypt(message, parent.CryptKey.toString()));
				} 
				catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
			
				break;
			}
			case VIGENEREEXTENDED:{
				try 
				{
					return(CryptVigenereExtended.encrypt(message, parent.CryptKey.toString()));
				} 
				catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
				break;
			}
			case ENIGMA:{
				try {
				
					return ((CryptEnigma)parent.CryptKey).encrypt(message);
				}
				catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
				break;
			}
			case AES128:{
				try 
				{
					return(CryptAES.encrypt(message, (Key) parent.CryptKey));
				} 
				catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
				break;
			}
			default:{
				return(message);
			}
		}	
		return null;
	}
	
	private void actionLPBInit(String cmd)
	{
		String slices[] = cmd.split(" ");
		if(slices.length == 2)
		{	
			send(LachsServer.DBG_LPB_INIT+"#"+slices[1]);
			
		}		else
		{
			u.translatedSystemMessageOutput("ERROR_NOTENOUGHARGS", Color.PINK, "zonk");			
		}
	}
		
	private void actionSendPrivateMessage(String cmd)
	{
		String slices[] = cmd.split(" ");
		if(slices.length >= 2)
		{	
			send(LachsServer.SEND_UNICAST+"#"+slices[1]+"#"+encodeMessage(cmd.substring(slices[0].length()+slices[1].length()+2)));	
		}
		else
		{
			u.translatedSystemMessageOutput("ERROR_NOTENOUGHARGS", Color.PINK, "zonk");
			u.translatedSystemMessageOutput("HELP_LC_PM", Color.LIGHT_GRAY, null);
		}
	}
	
	private void actionSendMultiPrivateMessage(String cmd)
	{
		String slices[] = cmd.split(" ");
		if(slices.length >= 2)
		{
			
			String tmp = LachsServer.SEND_MULTICAST+"#";
			int messageindex = slices[0].length()+1;
			
			// Zielbenutzer an hängen
			for(int i=1;i<slices.length-1;i++)
			{				
				if(slices[i].endsWith(","))
				{
					tmp+=slices[i].substring(0,slices[i].length()-1)+"#";
					messageindex += slices[i].length()+1;
				}
			}
			// eigentliche nachricht anhängen
			tmp+=encodeMessage(cmd.substring(messageindex));
	
			send(tmp);
		}
		else
		{
			u.translatedSystemMessageOutput("ERROR_NOTENOUGHARGS", Color.PINK, "zonk");
			u.translatedSystemMessageOutput("HELP_LC_PMM", Color.LIGHT_GRAY, null);
		}
	}	
	private void actionSendBroadcast(String cmd)
	{
		String slices[] = cmd.split(" ");
		if(slices.length >= 2) send(LachsServer.SERVER_SEND_BROADCAST+"#"+cmd.substring(slices[0].length()+1));
		else u.translatedSystemMessageOutput("ERROR_EMPTYMESSAGE", Color.PINK, "zonk");
	}
	private void actionDisconnect(String cmd)
	{
		String slices[] = cmd.split(" ");
		
		
		this.disconnect = true;
		this.stop();
		if(slices.length >= 2)send( LachsServer.USER_CONNECT_REFUSE+"#"+cmd.substring(slices[0].length()+1));
		else send(LachsServer.USER_CONNECT_REFUSE+"#bye!");
		
		
	}
	private void actionRegister(String cmd)
	{
		String slices[] = cmd.split(" ");
		
		if(slices.length == 2)
		{
			System.out.print("password:");
			String passwd = u.readPassword();			
			
			String passhash = null;
			try {
				
				passhash = LachsSecurtiy_HASH.generateHashLight(passwd);
									
			} catch (Exception e) {
				u.translatedSystemMessageOutput("ERROR_HASHGENERATION", Color.PINK, "zonk");
				e.printStackTrace();
			}
			
			
			send(LachsServer.USER_REGISTER+"#"+slices[1]+"#"+passhash);
		}
		else
		{
			u.translatedSystemMessageOutput("ERROR_NOTENOUGHARGS", Color.PINK, "zonk");
			u.translatedSystemMessageOutput("HELP_LC_REGISTER", Color.LIGHT_GRAY, null);
		}
	}
	
	
	public void decodeMessagePacket(String message)
	{
		int messagecode = 0;
		String sendername = "Anonymous";
		String rawmessage = "";
		
		String slices[] = message.split("#");
		
		if(slices.length == 2)
		{
			messagecode = Integer.parseInt(slices[0]);
			rawmessage = slices[1];
		}
		if(slices.length >= 3)
		{
			messagecode = Integer.parseInt(slices[0]);
			sendername = slices[1];
			rawmessage = slices[slices.length-1];
		}
		
		
		switch(messagecode)
		{
		
			case LachsServer.SEND_NORMALCHAT:{getStandardMessage(sendername,rawmessage);break;}
			case LachsServer.SEND_UNICAST:{getPrivateMessage(sendername,rawmessage);break;}			
			case LachsServer.SEND_MULTICAST:{getPrivateMessage(sendername,rawmessage);break;}
			case LachsServer.SERVER_SEND_BROADCAST:{getBroadcast(sendername,rawmessage);break;}
				
			case LachsServer.USER_CONNECT_CONFIRM:{getConnectConfirm(rawmessage);break;}
			case LachsServer.USER_CONNECT_REFUSE:{getDisconnectOrder(message);break;}
			
			case LachsServer.SEND_INFO_MESSAGE:{getInfoMessage(rawmessage);break;}
			case LachsServer.INFO_USERCONNECTED:{getInfoUserConnected(message);break;}
			case LachsServer.INFO_USERDISCONNECTED:{getInfoUserDisconnected(message);break;}
			case LachsServer.INFO_USERRENAMED:{getInfoMessage(message);break;}
			case LachsServer.INFO_SEND_USERLIST:{getUserList(message);break;}
			case LachsServer.USER_REGISTER_CONFIRM:{getInfoMessage(rawmessage);break;}
			
			case LachsServer.SEND_ERROR_MESSAGE:{getErrorMessage(rawmessage);break;}
			case LachsServer.ERROR_WRONGUSERPASSWORD:{getErrorWrongUserPassword();break;}
			case LachsServer.ERROR_USERALREADYCONNECTED:{getErrorUserAlreadyConnected();break;}
			case LachsServer.ERROR_USERNAMETAKEN:{getErrorUsernameUnavailable();break;}
			case LachsServer.ERROR_SYNTAX:{getErrorSyntax();break;}
			case LachsServer.ERROR_ALREADYREGISTERED:{getAlreadyRegistered();break;}
			
			case LachsServer.DBG_LPB_INIT:{getLPBInitOrder(message);break;}
			case LachsServer.DBG_LPB_CONFIRM:{getLPBConfirm(message);break;}
			
			default:{}		
		}
	}	
	
	private String decodeMessage(String rawmessage, String sendername)
	{
		switch(parent.cryptMode)
		{
			case CEASAR:{
				try 
				{
					return(sendername+": "+CryptCaesar.decrypt( rawmessage, parent.CryptKey.toString()));
				} 
				catch (NumberFormatException e){u.translatedSystemMessageOutput("ERROR_INVALIDCRYPTKEY", Color.PINK, "zonk");}
				
				break;
			}
			case VIGENERE:{
				try 
				{
					return(sendername+": "+CryptVigenere.decrypt( rawmessage, parent.CryptKey.toString()));
				} 
				catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
				
				break;
			}
			case VIGENEREEXTENDED:{
				try 
				{
					return(sendername+": "+CryptVigenereExtended.decrypt( rawmessage, parent.CryptKey.toString()));
				} 
				catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
				
				break;
			}
			case ENIGMA:{
				try {
				
					return(sendername+": "+((CryptEnigma)parent.CryptKey).decrypt(rawmessage));
					
				}catch (Exception e){u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
					
				
				break;
			}
			case AES128:{
				try {
					
					return(sendername+": "+CryptAES.decrypt(rawmessage, (Key) parent.CryptKey));
					
				} catch (Exception e) {u.translatedSystemMessageOutput("ERROR_ENCRYPTING", Color.PINK, "zonk");}
				break;
			}
			default:{
				
				 return(sendername+": "+rawmessage);
			}
		}
		return null;
	}
	
	
	private void getLPBConfirm(String message)
	{
		System.out.println("LPBConfirm recieved!");
		String slices[] = message.split("#");
		if(slices.length == 3)
		{
			new LachsPluginConsole(slices[1], Integer.parseInt( slices[2]),u.getConsole(),false);			
		}
	}
	
	private void getLPBInitOrder(String message)
	{
		System.out.println("LPBInitOrder recieved!");		
		String slices[] = message.split("#");
		if(slices.length == 2)
		{			
			try {
				int port = Utils.findFreePort();
				new LachsPluginConsole(channel.getLocalAddress().toString(),port,u.getConsole(),true);
				send(LachsServer.DBG_LPB_CONFIRM+"#"+slices[1]+"#"+channel.getLocalAddress().toString()+"#"+port);
				
			} catch (IOException e) {				
				e.printStackTrace();
			}			
		}
	}	
	private void getDisconnectOrder(String message)
	{
		this.disconnect = true;
		
		u.translatedSystemMessageOutput("INFO_YOUDISCONNECTED", Color.PINK, "zonk");		
		
		this.stop();
	}	
	
	private void getStandardMessage(String sendername,String rawmessage)
	{	
		u.wavPlay("wololo");
		System.out.println(decodeMessage(rawmessage, sendername));
		
		
	}

	private void getBroadcast(String sendername,String rawmessage)
	{			
		u.MessageOutput("LAChS",": "+rawmessage, Color.red, "wololo");		
	}	
	private void getPrivateMessage(String sendername,String rawmessage)
	{		
		u.MessageOutput(decodeMessage(rawmessage, sendername),Color.ORANGE,"wololo");		
	}
//SYTEMMESSAGES	
	private void getErrorWrongUserPassword()
	{
		u.translatedSystemMessageOutput("ERROR_WRONGPASSWD", Color.PINK, "zonk");
	}	
	private void getErrorUserAlreadyConnected()
	{
		u.translatedSystemMessageOutput("ERROR_ALREADYCONNECTED", Color.PINK, "zonk");
	}	
	private void getErrorUsernameUnavailable()
	{
		u.translatedSystemMessageOutput("ERROR_NAMEUNAVAILABLE", Color.PINK, "zonk");
	}	
	private void getErrorSyntax()
	{
		u.translatedSystemMessageOutput("ERROR_SYNTAX", Color.PINK, "zonk");
	}	
	private void getAlreadyRegistered()
	{
		u.translatedSystemMessageOutput("ERROR_ALREADYREGISTERED", Color.PINK, "zonk");
	}	
	private void getInfoMessage(String rawmessage)
	{	
		u.systemMessageOutput(rawmessage,Color.GRAY,"wololo");		
	}
	private void getInfoUserConnected(String rawmessage)
	{	
		u.translatedSystemMessageOutput("INFO_USERCONNECTED",new String[]{rawmessage.split("#")[1]}, Color.GRAY, "wololo");
	}
	private void getInfoUserDisconnected(String rawmessage)
	{
		u.translatedSystemMessageOutput("INFO_USERDISCONNECTED",new String[]{rawmessage.split("#")[1]}, Color.GRAY, "wololo");
	}
	private void getInfoUserRenamed(String rawmessage)
	{			
		u.translatedSystemMessageOutput("INFO_USERRENAMED", Color.GRAY, "wololo");
	}	
	private void getErrorMessage(String rawmessage)
	{
		u.systemMessageOutput(rawmessage,Color.PINK,"zonk");
	}	
	private void getConnectConfirm(String rawmessage)
	{				
		u.translatedSystemMessageOutput("CONNECT_CONFIRM",new String[]{rawmessage}, Color.GRAY, "arschgaul");
	}
	
	/**
	 * 
	 * 
	 * @param message
	 */
	private void getUserList(String message)
	{
		String slices[] = message.split("#");
		
		Color tmpCol = Color.WHITE;
		
		TextAttributes bkup = u.getConsole().getTextAttributes();
				
		System.out.println("Online User are:");
		for(int i=1;i<slices.length;i++)
		{
			String data[] = slices[i].split(":");
			
			switch(Integer.parseInt(data[1]))
			{
				case 4:{tmpCol = LachsServer.COLOR_NAME_OWNER;break;}
				case 3:{tmpCol = LachsServer.COLOR_NAME_ADMIN;break;}
				case 2:{tmpCol = LachsServer.COLOR_NAME_MOD;break;}
				case 1:{tmpCol = LachsServer.COLOR_NAME_USER;break;}
				case 0:{tmpCol = LachsServer.COLOR_NAME_GUEST;break;}			
			}			
			u.getConsole().setTextAttributes(new TextAttributes(tmpCol));System.out.print(data[0]);u.getConsole().setTextAttributes(bkup);
			
			if(i!=slices.length-1)System.out.print(", ");
			else System.out.print(". \n");			
		}		
	}
}
