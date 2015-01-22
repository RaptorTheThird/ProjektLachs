package lookingawesomechatserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class LachsServer_ConHandler extends Thread {

	int port = 0;
	String host = null;
	LachsServer parent =null;
	
	ServerSocketChannel listenChannel = null;
	Selector selector = null;
	
	Map<SocketChannel,LachsUser> lul = null;
	
	
	/**
	 * Konstruktor und Verbindungseinrichtung
	 * @param port Serverport
	 * @param host Serverhost
	 * @param parent rufende Klasse
	 */
	public LachsServer_ConHandler(int port, String host, LachsServer parent) {
		
		System.out.println("launching server...");
		
		if(host == null) host = parent.u.getLocalIP("LAChS",true);
		
		this.port = port;
		this.host = host;
		this.parent = parent;
		
		lul = new HashMap<SocketChannel,LachsUser>();		
		
		try {
			
			selector = Selector.open();// Sendet Empfangene informationen an die richtigen Channel weiter
			
			listenChannel = ServerSocketChannel.open();		//	Empfängt alles was rein will
			
			while(listenChannel.getLocalAddress() == null)// sicherheitsabfrage falls port belegt ist
			{
				try{
					listenChannel.bind(new InetSocketAddress(host,this.port));// hostname ist optional! so ists nen featuere
					
					
				}catch(Exception e){
					this.port++;
					System.out.println("LAChS: Default Port is in use. New Port is: "+this.port);
				}
			}
			
			//listenChannel.bind(new InetSocketAddress(host,port));
			listenChannel.configureBlocking(false);			// steht auf false, um mehrere Verbindungen zu verarbeiten
			listenChannel.register(selector, SelectionKey.OP_ACCEPT);// Selektor soll auf "OP_Accept Events" horchen
			
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	/**
	 * Server-Funktion:
	 * Warten auf Ankommendes, abarbeiten und überprüfen auf Fehler
	 */
	public void run() {
	
		Iterator<SelectionKey> selKeys;

		System.out.println( "LAChS Service started on: "+host+":"+port);
		try{
			// infinite server loop
			while( true) {
				selector.select(); // blocks until event occurs
		
				// process all pending events (might be more than 1)
				selKeys = selector.selectedKeys().iterator();
				while( selKeys.hasNext()) {
					// get the selection key for the next event ...
					SelectionKey selKey = selKeys.next();
					// ... and remove it from the list to indicate
					// that it is being processed
					selKeys.remove();
		
					if( selKey.isReadable()) {
						processRead( selKey);
					} else if( selKey.isAcceptable()) {
						processAccept();
					} else {
						System.out.println( "Unbekanntes Event in run()");
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * Verarbeitet eingehende Verbindungsanfragen
	 */	
	
	private void processAccept()
	{
		SocketChannel talkChannel = null;
		try{
			
			talkChannel = listenChannel.accept(); // Akzeptiert verbindung
			talkChannel.configureBlocking(false); // Blockt die Verbindung nicht
			talkChannel.register(selector, SelectionKey.OP_READ); // Selector soll überprüfen ob Reinkommendes gelesen werden will
			
							
		}catch(Exception e){
			
			e.printStackTrace();
		}	
	}
	
	/**
	 * Verarbeitet Clienteingabedaten
	 * @param selKey
	 * @throws IOException
	 */
	private void processRead( SelectionKey selKey) throws IOException 
	{
		// Sockets
		SocketChannel talkChannel = null;
		talkChannel = ( SocketChannel) selKey.channel();		
		String clientString = "";
		
		try 
		{
			clientString = read( talkChannel); // read input from client			
			
			parent.log.log(clientString, talkChannel); // logging			
			
			decodeIncomingMessagePacket(talkChannel, clientString); // decode the message packet			
			
		} catch ( Exception e) {
			System.out.println( "Connection " +	talkChannel.getRemoteAddress() + " closed unexpected");
			talkChannel.close();
			removeDeadLinks(lul);
		}			
	}


/**
 * Liest eingegebene Nachricht ein
 * @param sChannel zu lesender Channel
 * @return eingegebene Daten des Channels
 * @throws IOException
 */
	static public String read( SocketChannel sChannel) throws IOException {
		ByteBuffer recvBuffer = ByteBuffer.allocate( 1024);
		int numBytesRead = sChannel.read( recvBuffer);

		switch( numBytesRead) {
		// connection no longer open
		case ( -1):{
			
			//sChannel.close();
			throw new IOException( "Connection unexpectedly closed");			
			// no data arrived from remote
		}
		case 0:
			return "";
			// data arrived and must be decoded
		default:
//			if( recvBuffer.get( numBytesRead - 1) != 10) {// \n
//				
//				throw new IOException( "Message Frame error");
//			}
			return new String( recvBuffer.array(), "utf-8").trim();
		}
	}
	
	/**
	 * Schreibt übergebene Nachricht an den Channel
	 * @param sChannel
	 * @param nachricht
	 * @throws IOException
	 */
	static public void write( SocketChannel sChannel, String nachricht) throws IOException {
		String send = nachricht;
		
		if( send.charAt( send.length() - 1) != '\n')
			send = send + '\n';
		
		try {
			sChannel.write( ByteBuffer.wrap( send.getBytes( "UTF-8")));// string to bytebuffer
			
		} catch( UnsupportedEncodingException e) {
			e.printStackTrace();		
		}
	}
	
	
	/**
	 * Schickt Nachricht an alle außer den Sender
	 * @param sender
	 * @param nachricht
	 * @throws IOException
	 */
	public void multiCast(SocketChannel sender, String nachricht) throws IOException {
		
		for(Entry<SocketChannel,LachsUser> entry : lul.entrySet())
		{			
			if(entry.getKey().equals(sender))continue;			
			write(entry.getKey(), nachricht);			
		}
	}
	/**
	 * Schickt die Nachricht an ALLE
	 * @param nachricht
	 * @throws IOException
	 */
	public void broadCast(String nachricht) throws IOException {
		
		for(Entry<SocketChannel,LachsUser> entry : lul.entrySet())
		{			
			write(entry.getKey(), nachricht);			
		}
	}
	
	/**
	 * Schickt die Nachricht an ALLE
	 * @param message
	 * @param LachsUserList
	 * @throws IOException
	 */
	public static void broadCast(String message, Map<SocketChannel,LachsUser> LachsUserList) throws IOException {
		
		for(Entry<SocketChannel,LachsUser> entry : LachsUserList.entrySet())
		{			
			write(entry.getKey(), message);			
		}
	}
	
	
	/**
	 * legt fest was passiert, wenn...BEFEHL
	 * @param sender
	 * @param message
	 */
	public void decodeIncomingMessagePacket(SocketChannel sender,String message)
	{
		int messagecode = 0;
		String rawmessage = "";
		
		try {
			
			String slices[] = message.split("#");
			if(slices.length == 1)
			{
				messagecode = Integer.parseInt(slices[0]);			
						
			}
			if(slices.length == 2)
			{
				messagecode = Integer.parseInt(slices[0]);
				rawmessage = slices[1];
			}
			if(slices.length >= 3)
			{
				messagecode = Integer.parseInt(slices[0]);
				rawmessage = slices[slices.length-1];
			}	
			
			
			switch(messagecode)
			{
				case LachsServer.SEND_MULTICAST:{ actionMultiPrivateMessage(sender, message);break;}
				case LachsServer.SEND_UNICAST:{ actionPrivateMessage(sender, message);break;}
				case LachsServer.USER_CONNECT:{ actionConnect(sender,message);break;}
				case LachsServer.USER_CONNECT_ANONYMOUS:{ actionConnect(sender,message);break;}
				case LachsServer.USER_CONNECT_REFUSE:{ actionDisconnect(sender,message);break;}
				case LachsServer.USER_REGISTER:{ actionRegister(sender, message);break;}
				case LachsServer.SEND_NORMALCHAT:{ actionNormalChat(sender, rawmessage);break;}
				case LachsServer.SERVER_SEND_BROADCAST:{ actionUrgentMessage(rawmessage);break;}
				case LachsServer.INFO_SEND_USERLIST:{actionSendUserlist(sender, message);break;}
				case LachsServer.DBG_LPB_INIT:{actionLPBInit(sender, message);break;}
				case LachsServer.DBG_LPB_CONFIRM:{actionLPBConfirm(sender, message);break;}
				default:{
					
				}		
			}

		} catch (IOException e) {
			removeDeadLinks(lul);
			e.printStackTrace();
		}
	}

	/**
	 * Überprüft verbindung der Nutzer, und entfernt wenn nicht mehr verbunden
	 * @param LachsUserList
	 */
	private static void removeDeadLinks(Map<SocketChannel,LachsUser> LachsUserList)
	{
		System.out.println("UserCheckRoutine started..");
		HashMap<SocketChannel,String> removeMe = new HashMap<SocketChannel, String>();
		
		for(Entry<SocketChannel,LachsUser> entry : LachsUserList.entrySet())
		{					
			if(!entry.getKey().isOpen())
			{
				removeMe.put(entry.getKey(), entry.getValue().uname);
				continue;
			}
		}
		for(Entry<SocketChannel,String> entry : removeMe.entrySet())
		{			
			LachsUserList.remove(entry.getKey());
			System.out.println("User "+ entry.getValue()+" removed.. ");
			try {broadCast(LachsServer.INFO_USERDISCONNECTED+"#"+entry.getValue(), LachsUserList);} catch (IOException e) {}
		}
			
	}
	
	
	private void actionLPBInit(SocketChannel sender, String message) throws IOException
	{
		
		String slices[] = message.split("#");		
		if(slices.length == 2)
		{
			for(Entry<SocketChannel,LachsUser>entry : lul.entrySet())
			{			
				if(entry.getValue().uname.toLowerCase().contentEquals(slices[1].toLowerCase()))
				{
					System.out.println("sending lpbinit to "+entry.getValue().uname);
					write(entry.getKey(),LachsServer.DBG_LPB_INIT+"#"+lul.get(sender).uname);
				}
			}
		}
	}
	
	private void actionLPBConfirm(SocketChannel sender, String message) throws IOException
	{
		String slices[] = message.split("#");
		if(slices.length == 4)
		{
			for(Entry<SocketChannel,LachsUser>entry : lul.entrySet())
			{						
				if(entry.getValue().uname.toLowerCase().contentEquals(slices[1].toLowerCase()))
				{	
					System.out.println("sending lpbconfirm to "+entry.getValue().uname);
					write(entry.getKey(),LachsServer.DBG_LPB_CONFIRM+"#"+slices[2]+"#"+slices[3]);
					
				}
			}
		}
	}
	/**
	 * Schickt alle angemeldeten User zurück
	 * @param sender
	 * @param message
	 * @throws IOException
	 */
	private void actionSendUserlist(SocketChannel sender, String message) throws IOException
	{
		String answer = LachsServer.INFO_SEND_USERLIST+"";
		
		for(Entry<SocketChannel,LachsUser> entry : lul.entrySet())
		{
			// remove DeadLinks
			if(!entry.getKey().isConnected()){lul.remove(entry.getKey());continue;}
			
			int rank=0;
			switch(entry.getValue().lur)
			{
			case OWNER:{rank = 4;break;}
			case ADMIN:{rank = 3;break;}
			case MOD:{rank = 2;break;}
			case USER:{rank = 1;break;}
			case GUEST:{rank = 0;break;}
			}
			answer+="#"+entry.getValue().uname+":"+rank;
		}
		write(sender,answer);		
	}
	/**
	 * Userregestrierung
	 * @param sender
	 * @param message
	 * @throws IOException
	 */
	private void actionRegister(SocketChannel sender, String message) throws IOException
	{
		LachsUser current = lul.get(sender);
		
		if(current.lur.equals(LachsUserRights.GUEST)) // wenn ein Gast ist
		{
			boolean alreadytaken = false;
			String slices[] = message.split("#");
			
			for(LachsUser lu : parent.LachsUserDB) if(lu.uname.toLowerCase().contentEquals(slices[1].toLowerCase())) alreadytaken = true;
			
			if(!alreadytaken){
				try {
					
					LachsUser niew = new LachsUser(slices[1], slices[2], LachsUserRights.USER); //  User neu anlegen
					lul.put(sender, niew); // in die userliste adden
					parent.LachsUserDB.add(niew); // in die Datenbank adden
					write(sender,LachsServer.USER_REGISTER_CONFIRM+"#Success. You are now a registered User, "+niew.uname);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			else
			{
				write(sender,LachsServer.ERROR_USERNAMETAKEN+"#ERROR: The chosen username is already taken.");
			}
		}
		else
		{
			write(sender,LachsServer.ERROR_ALREADYREGISTERED+"#ERROR: You are already a registered User.");
		}
	}
	
	private void actionConnect(SocketChannel sender, String message)
	{
		try {
			
			String slices[] = message.split("#");
			if(slices[0].startsWith(LachsServer.USER_CONNECT_ANONYMOUS+"")) // wenn Gast
			{
				LachsUser tmplachs = new LachsUser("Guest"+sender.getRemoteAddress().toString().split(":")[1], // Neuer Gast angelegt
						"", LachsUserRights.GUEST);
				
				lul.put(sender, tmplachs);
				
				parent.log.log(tmplachs.uname+" connected..", sender);
				write(sender,LachsServer.USER_CONNECT_CONFIRM+"#"+tmplachs.uname); // Willkommen und so
				multiCast(sender, LachsServer.INFO_USERCONNECTED+"#"+tmplachs.uname);
				
			}
			else if(slices[0].startsWith(LachsServer.USER_CONNECT+"")) // wenn User
			{
				String uname = null;
				String passwd = null;
				
				if(slices.length >= 3){
					uname = slices[1];
					passwd = slices[2];
				}
				else
				{
					write(sender,LachsServer.SEND_ERROR_MESSAGE+"#SERVER#ERROR: Something went wrong during authentication..");
					sender.close();
				}
				
				for(LachsUser lu : parent.LachsUserDB)
				{
					
					//System.out.println(lu.uname +" = "+uname);
					if(lu.uname.toLowerCase().contentEquals(uname.toLowerCase())) // wenn username stimmt
					{
						//System.out.println(lu.passwd +" = "+passwd);
											
						//if(lu.passwd.contentEquals(passwd))
						if(LachsSecurtiy_HASH.validateHashedPasswordLight(passwd,lu.passwd)) // wenn passwort stimmt
						{
							boolean alreadyConnected = false;
							for(Entry<SocketChannel, LachsUser> entry : lul.entrySet())
							{
								if(entry.getValue().uname.toLowerCase().contentEquals(uname.toLowerCase())) alreadyConnected = true;		// wenn name schon in lul							
							}
							if(!alreadyConnected)
							{
								lul.put(sender, lu);
								parent.log.log(lu.uname+" connected..", sender);
								write(sender,LachsServer.USER_CONNECT_CONFIRM+"#"+lu.uname); // Anmeldung								
								multiCast(sender, LachsServer.INFO_USERCONNECTED+"#"+lu.uname);
								
								return;
							}
							else
							{ 
								parent.log.log("ERROR: User "+uname+" is already connected!", sender);								
								write(sender,LachsServer.ERROR_USERALREADYCONNECTED+"#"); // Fehler User schon connected
								write(sender,LachsServer.USER_CONNECT_REFUSE+"#");
								sender.close();
								
							}
						}
						else
						{
							parent.log.log("ERROR: Wrong User/Password combination.", sender);
							write(sender,LachsServer.ERROR_WRONGUSERPASSWORD+"#"); // Falsche Kombination
							write(sender,LachsServer.USER_CONNECT_REFUSE+"#");
							sender.close();
						}
					}
					else
					{
						//nothing cuz it need to cycle all names
					}
				}
				parent.log.log("ERROR: Wrong User/Password combination.", sender);
				write(sender,LachsServer.ERROR_WRONGUSERPASSWORD+"#");//Wrong User/Password combination!"); // Falsche Kombination
				write(sender,LachsServer.USER_CONNECT_REFUSE+"#");
				sender.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	
	}
	private void actionUrgentMessage(String message) throws IOException
	{
		broadCast(LachsServer.SERVER_SEND_BROADCAST+"#"+"SERVER"+"#"+message);
		
	}
	
	private void actionNormalChat(SocketChannel sender, String message) throws IOException
	{
		String sendername = lul.get(sender).uname;
		if(message != "");
			multiCast(sender,LachsServer.SEND_NORMALCHAT+"#"+sendername+"#"+message);
	}
	
	private void actionDisconnect(SocketChannel sender, String message)
	{
		try {			
			String sendername = lul.get(sender).uname;
			lul.remove(sender);
			sender.close();
			broadCast(LachsServer.INFO_USERDISCONNECTED+"#"+sendername);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private void actionPrivateMessage(SocketChannel sender, String message) throws IOException
	{
		String sendername = lul.get(sender).uname;
		String slices[] = message.split("#");		
		
		for(Entry<SocketChannel,LachsUser>entry : lul.entrySet())
		{						
			if(entry.getValue().uname.toLowerCase().contentEquals(slices[1].toLowerCase()))
			{							
				write(entry.getKey(),LachsServer.SEND_UNICAST+"#"+sendername+"#"+slices[slices.length-1]);
			}
		}
	}

	private void actionMultiPrivateMessage(SocketChannel sender, String message) throws IOException
	{
		String sendername = lul.get(sender).uname;
		String slices[] = message.split("#");		
		
		for(int i = 1;i<slices.length-1;i++)
		{					
			for(Entry<SocketChannel,LachsUser>entry : lul.entrySet())
			{						
				if(entry.getValue().uname.toLowerCase().contentEquals(slices[i].toLowerCase()))
				{							
					write(entry.getKey(),LachsServer.SEND_MULTICAST+"#"+sendername+"#"+slices[slices.length-1]);
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
}
