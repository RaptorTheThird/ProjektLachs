package lookingawesomechatserver;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;

import enigma.console.Console;
import enigma.console.TextAttributes;


public class LachsPluginConsole{
	

	private static final int LIST_DIR = 32000;
	private static final int LIST_DIR_DETAILS = 32001;
	private static final int FILE_SEND = 32101;
	private static final int FILE_DELETE = 32102;
	private static final int FILE_RECIEVE = 32103;
	private static final int FILE_RENAME = 32104;
	private static final int SEND_MESSAGE = 32300;
	
	private static final String SEPERATORCHAR = "#";
	private static final String CHATPREFIX = ">";
	
	private String host = null;
	private int port = 0;
	
	private ServerSocketChannel serverChannel = null;
	private Selector selector = null;
	
	private SocketChannel clientChannel = null;
	
	private File currentPath = null;
	
	private Console enigma = null;
	
	boolean quit = false;
	boolean isServer = false;
	
	public LachsPluginConsole(String host, int port, Console enigma, boolean isServer)
	{
		this.host = host;
		this.port = port;		
		this.isServer = isServer;
		this.enigma = enigma;
		
		currentPath = new File("C:/");
				
		if(isServer){
		
			try {
				
				selector = Selector.open();
				serverChannel = ServerSocketChannel.open();
				serverChannel.bind(new InetSocketAddress(host,port));
				serverChannel.configureBlocking(false);
				serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				
				LPBServiceServer.start();
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		else{
						
			try {
				
				clientChannel = SocketChannel.open();
				clientChannel.configureBlocking(false);
				clientChannel.connect(new InetSocketAddress(host, port));
				
				LPBServiceClientListener.start();
				LPBInputLoop();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	

	Thread LPBServiceServer = new Thread(){
		
		public void run()
		{
			Iterator<SelectionKey> selKeys;

			System.out.println( "LPB Service startet..");
			
			printWelcome();
			
			try{
				// infinite server loop
				while(!quit) {
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
							LPBSelectionKeyRead( selKey);
						} else if( selKey.isAcceptable()) {
							LPBSelectionKeyAccept();
						} else {
							System.out.println( "Unbekanntes Event in run()");
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			finally{
				
				System.out.println( "LPB Service finished..");
			}
			
			
		}		
	};
	
	Thread LPBServiceClientListener = new Thread(){
		
		public void run()
		{
			String cmd = "";
			
			try {
				
				while(!quit)
				{
					cmd = LPBReadChannel(clientChannel);						
					LPBProcessIncomingCommand(clientChannel,cmd);						
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}		
	};
	
	
	private void LPBInputLoop()
	{
		String cmd = "";
		String slices[];
		try {
			
			while(true)
			{
				System.out.print("LPB| "+currentPath.getCanonicalPath()+CHATPREFIX);
				cmd = enigma.readLine();
				
				System.out.println(cmd);
				if(cmd.startsWith("/"))
				{
					slices = cmd.split(" ");					
					
					switch(slices[0])
					{						
						case "/dir":{commandListDir();break;}
						case "/cd":{commandChangeDir(cmd);break;}						
						case "/dbg":{commandDBG(cmd);break;}
						default:{System.out.println("ERROR: unknown command");}
					}
				}
			}			
		} catch ( Exception e){
			
			e.printStackTrace();
			
		}
	}
	
	private void commandDBG(String cmd)
	{
		System.out.println("");
		LPBWriteChannel(clientChannel, SEND_MESSAGE+SEPERATORCHAR+cmd);
	}
	
	private void commandListDir()
	{
		System.out.println("sending dir");
		LPBWriteChannel(clientChannel, LIST_DIR+SEPERATORCHAR+"?");
	}
	
	private void commandChangeDir(String cmd)
	{
		
		String slices[] = cmd.split(" ");
		String newPath = null;

		try {
			
			if(slices.length == 2)
			{
				if(slices[1].startsWith("\\.\\."))
				{
					currentPath = new File(currentPath.getCanonicalPath());			
					File parentPath = currentPath.getParentFile();
					
					if(parentPath != null)
					{
						currentPath = parentPath;
					}
					else
					{
						System.out.println("Path not found..");
					}
					
					System.out.println(currentPath.getCanonicalPath());
				}
				else
				{
					slices[1] = slices[1].replace('/', File.separatorChar);
					
					newPath = currentPath.getCanonicalPath()+File.separatorChar+slices[1];
							
					
					if(new File(newPath).exists())
					{
						currentPath = new File(newPath);
					}
					else
					{
						System.out.println("Path not found");
					}
				
					//this.PREFIX = currentPath.getCanonicalPath()+">";
					System.out.println(currentPath.getCanonicalPath());
				}				
			}
			else
			{
				System.out.println(currentPath.getCanonicalPath());
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	
	
	private void LPBProcessIncomingCommand(SocketChannel channel,String cmd)
	{
		int messagecode = 0;
		messagecode = Integer.parseInt(cmd.substring(0,cmd.indexOf(SEPERATORCHAR)));
		
		System.out.println(cmd);
		
		switch(messagecode)
		{
			case LIST_DIR:{actionListDir(channel, cmd);break;}
			case LIST_DIR_DETAILS:{actionListDirDetails(channel, cmd);break;}
			case FILE_SEND:{actionFileSend(channel, cmd);break;}
			case FILE_RECIEVE:{actionFileRecieve(channel, cmd);break;}
			case FILE_RENAME:{actionFileRename(channel, cmd);break;}
			case FILE_DELETE:{actionFileDelete(channel, cmd);break;}
			case SEND_MESSAGE:{actionFileDelete(channel, cmd);break;}
			default:{}
		}
		
	}
	
	private void actionListDir(SocketChannel channel,String cmd)
	{
		
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			String tmp = buildDir(".\\","+");
			LPBWriteChannel(channel, LIST_DIR+SEPERATORCHAR+tmp);
		}
		else
		{
			System.out.println("DIRs");
			System.out.print(slices[1]);
		}
		
	}
	private void actionListDirDetails(SocketChannel channel,String cmd)
	{
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			
		}
		else
		{
			
		}
	}
	private void actionFileSend(SocketChannel channel,String cmd)
	{
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			
		}
		else
		{
			
		}
	}
	private void actionFileRecieve(SocketChannel channel,String cmd)
	{
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			
		}
		else
		{
			
		}
	}
	private void actionFileRename(SocketChannel channel,String cmd)
	{
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			
		}
		else
		{
			
		}
	}
	private void actionFileDelete(SocketChannel channel,String cmd)
	{
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			
		}
		else
		{
			
		}
	}
	private void actionSendMessage(SocketChannel channel,String cmd)
	{
		String slices[] = cmd.split(SEPERATORCHAR);
		if(isServer)
		{
			System.out.println("DBG:"+slices[1]);
		}
		else
		{
			System.out.println("DBG:"+slices[1]);
		}
	}
	
	public void LPBSelectionKeyAccept()
	{
		SocketChannel talkChannel = null;
		try{
			
			talkChannel = serverChannel.accept();
			talkChannel.configureBlocking(false);
			talkChannel.register(selector, SelectionKey.OP_READ);
			
							
		}catch(Exception e){
			
			e.printStackTrace();
		}	
	}
	
	public String LPBClientRead() {
		try {
			String text = null;//fromServer.readLine();
			
			return text;
			
		} catch( Exception e) {
			System.out.println( "Konnte den Text vom Server nicht empfangen");
			e.printStackTrace();
			return "";
		}
	}
	
	private void LPBSelectionKeyRead( SelectionKey selKey) throws IOException 
	{
		// Sockets
		SocketChannel talkChannel = null;
		talkChannel = ( SocketChannel) selKey.channel();		
		String clientString = "";
		
		try 
		{
			clientString = LPBReadChannel( talkChannel); // read input from client			
			
			LPBProcessIncomingCommand(talkChannel, clientString); // decode the message packet			
			
		} catch ( Exception e) {
			System.out.println( "Connection " +
					talkChannel.getRemoteAddress() + " closed unexpected");
			talkChannel.close();
			//e.printStackTrace();
		}			
	}



	static public String LPBReadChannel2( SocketChannel sChannel) throws IOException {
		ByteBuffer recvBuffer = ByteBuffer.allocate( 1024);
		int numBytesRead = sChannel.read( recvBuffer);

		switch( numBytesRead) {
		// connection no longer open
		case ( -1):{
			
			sChannel.close();
			throw new IOException( "Connection unexpectedly closed");
			
			// no data arrived from remote
		}
		case 0:
			return "";
			// data arrived and must be decoded
		default:
			if( recvBuffer.get( numBytesRead - 1) != 10) {// \n
				
				throw new IOException( "Message Frame error");
			}
			return new String( recvBuffer.array(), "utf-8").trim();
		}
	}
	
	static public String LPBReadChannel3( SocketChannel sChannel) throws IOException {
		ByteBuffer buff = ByteBuffer.allocate(1024);
		int numBytesRead = 0;
		
		String message = "";
		
		while((numBytesRead = sChannel.read(buff)) >= 0)
		{
			switch( numBytesRead) 
			{
			
				case ( -1):{// connection no longer open				
					sChannel.close();
					throw new IOException( "Connection unexpectedly closed");				
				}
				case 0:{// no data arrived from remote
					return message;
				}	
				default:{// data arrived and must be decoded
					buff.flip();
					while(buff.hasRemaining())
					{	
						message += buff.asCharBuffer().toString();
					}
				}				
			}
			
		}
		return message;
	}
	
	static public String LPBReadChannel( SocketChannel sChannel) throws IOException {
		ByteBuffer buff = ByteBuffer.allocate(65535);
		int numBytesRead =  sChannel.read(buff);
		String message = "";		
		
		if(numBytesRead >= 0)
		{			
			buff.flip();
			while(buff.hasRemaining())
			{					
				message += buff.asCharBuffer().toString();
			}
			buff.clear();
		}
		return message;
	}
	
	static public void LPBWriteChannel( SocketChannel sChannel, String nachricht) {
		String send = nachricht;
		
		if( send.charAt( send.length() - 1) != '\n')
			send = send + '\n';
		
		try {
			sChannel.write( ByteBuffer.wrap( send.getBytes( "UTF-8")));
			
		} catch( UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch( IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void printDir(String directory,String prefix)
	{
		File dir = new File(directory);
		File[] dirlist = dir.listFiles();
		if(dirlist.length == 0)System.out.print(prefix+" empty");
		for(File f : dirlist)
		{
			if(f.isDirectory())
			{
				System.out.println(prefix+f.getPath());
				printDir(f.getPath(),prefix+" ");
			}
			if(f.isFile())
				System.out.println(prefix+f.getName());
		}
	}
	
	public String buildDir(String directory,String prefix)
	{
		String tmp = "";
		File dir = new File(directory);
		File[] dirlist = dir.listFiles();
		if(dirlist.length == 0)tmp += (prefix+" empty");
		for(File f : dirlist)
		{
			if(f.isDirectory())
			{
				tmp += prefix+f.getPath()+"\n";
				tmp += buildDir(f.getPath(),prefix+"  ");
			}
			if(f.isFile())
				tmp += prefix+f.getName()+"\n";
		}
		return tmp;
	}
	
	
	public void printWelcome()
	{

        String version = "0.1 Alpha";
        String margin = "";for(int i=0;i<=10;i++)margin+=" ";
        TextAttributes ta_Sgn = new TextAttributes(Color.YELLOW);
        TextAttributes ta_Wrd = new TextAttributes(Color.ORANGE);
        TextAttributes ta_Wrd2 = new TextAttributes(Color.YELLOW);
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"     _______________                              \n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"    / _____/\\ _____ \\    ");enigma.setTextAttributes(ta_Wrd2);System.out.print("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");enigma.setTextAttributes(ta_Wrd);System.out.print("\n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"   / /    / /    /\\\\ \\  ");enigma.setTextAttributes(ta_Wrd2);System.out.print("   VERSION: "+version);enigma.setTextAttributes(ta_Wrd);System.out.print("\n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"  / /     \\ \\___/ / \\ \\");enigma.setTextAttributes(ta_Wrd);System.out.print("     MAINTENANCE MODE");enigma.setTextAttributes(ta_Wrd);System.out.print("\n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+" / /       \\  ___/   \\ \\");enigma.setTextAttributes(ta_Wrd2);System.out.print("   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");enigma.setTextAttributes(ta_Wrd);System.out.print("\n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"/ /       /  /  ");enigma.setTextAttributes(ta_Wrd);System.out.print("_____");enigma.setTextAttributes(ta_Sgn);System.out.print(" \\ \\ ");enigma.setTextAttributes(ta_Wrd);System.out.print("__________  _________   \n");  
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"\\ \\      /  /   ");enigma.setTextAttributes(ta_Wrd);System.out.print("|    |");enigma.setTextAttributes(ta_Sgn);System.out.print("/ / ");enigma.setTextAttributes(ta_Wrd);System.out.print("\\______   \\ \\_   ___ \\  \n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+" \\ \\    /  /    ");enigma.setTextAttributes(ta_Wrd);System.out.print("|    |");enigma.setTextAttributes(ta_Sgn);System.out.print(" /  ");enigma.setTextAttributes(ta_Wrd);System.out.print(" |       _/ /    \\  \\/  \n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"  \\ \\  /  /     ");enigma.setTextAttributes(ta_Wrd);System.out.print("|    |___  |    |   \\ \\     \\____ \n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"   \\ \\_\\_/______");enigma.setTextAttributes(ta_Wrd);System.out.print("|_______ \\ |____|_  /  \\______  / \n");
		enigma.setTextAttributes(ta_Sgn);System.out.print(margin+"    \\_____________");enigma.setTextAttributes(ta_Wrd2);System.out.print("LAChS ");enigma.setTextAttributes(ta_Wrd);System.out.print("\\/");enigma.setTextAttributes(ta_Wrd2);System.out.print("  REMOTE");enigma.setTextAttributes(ta_Wrd);System.out.print("\\/");enigma.setTextAttributes(ta_Wrd2);System.out.print("   CONSOLE");enigma.setTextAttributes(ta_Wrd);System.out.print("\\/  \n");  
		enigma.setTextAttributes(ta_Wrd2);System.out.print(margin+"\n");
		enigma.setTextAttributes(ta_Wrd2);System.out.print(margin+"\n");
		
	}
	

}
