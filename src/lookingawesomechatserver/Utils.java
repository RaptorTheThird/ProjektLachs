package lookingawesomechatserver;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import enigma.console.Console;
import enigma.console.TextAttributes;

public class Utils {

	private HashMap<String,File> WavDB = null;
	private Console enigma = null;
	private LachsLanguageController lc = null;
	
	public Utils(Console enigma, String language)
	{
		this.enigma = enigma;
		
		lc = new LachsLanguageController(language);
		
		wavInit();		
	}	
	
	public void wavPlay(String wavName)
	{
		if(wavName == null || wavName.startsWith("none")) return;
		try {

			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(WavDB.get(wavName));
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        clip.start();
	        
		} catch (Exception e) {
			System.out.println("ERROR: "+wavName+".wav could not be played :'( ");
			e.printStackTrace();
		}
		
	}
	public void wavInit()
	{
		try {
			
			this.WavDB = new HashMap<String,File>();
			String wavFolder = LachsServer.LACHSSOUNDFOLDER;
			
			
	        WavDB.put("wololo", new File(wavFolder+"wololo.wav"));
	        WavDB.put("arschgaul", new File(wavFolder+"arschgaul.wav"));
	        WavDB.put("zonk", new File(wavFolder+"zonk.wav"));

			
		} catch (Exception e) {
			System.out.println("Wav init went wrong :'(");
			e.printStackTrace();
		}		
	}
	public LachsLanguageController getTranslator()
	{
		return lc;
	}
	public Console getConsole()
	{
		return enigma;
	}
	public String readPassword()
	{
		return enigma.readPassword();
	}
	public String readInput()
	{
		return enigma.readLine();
	}
	public void printError(String cmd)
	{
		wavPlay("zonk");
		
		Color bkup = enigma.getTextAttributes().getForeground();
		enigma.setTextAttributes(new TextAttributes(Color.PINK));
		
		System.out.println("ERROR:"+cmd);
		
		enigma.setTextAttributes(new TextAttributes(bkup));
	}
	public void printWarning(String cmd)
	{
		wavPlay("zonk");
		
		Color bkup = enigma.getTextAttributes().getForeground();
		enigma.setTextAttributes(new TextAttributes(Color.YELLOW));
		
		System.out.println("WARNING:"+cmd);
		
		enigma.setTextAttributes(new TextAttributes(bkup));
	}
	public void printGrayln(String cmd)
	{
		wavPlay("wololo");
		
		Color bkup = enigma.getTextAttributes().getForeground();
		enigma.setTextAttributes(new TextAttributes(Color.GRAY));
		
		System.out.println(cmd);
		
		enigma.setTextAttributes(new TextAttributes(bkup));
	}
	public void printGray(String cmd)
	{
		Color bkup = enigma.getTextAttributes().getForeground();
		enigma.setTextAttributes(new TextAttributes(Color.GRAY));
		System.out.print(cmd);
		enigma.setTextAttributes(new TextAttributes(bkup));
	}
	
	/**
	 * Findet die lokale IP4 Adresse heraus auf der der Server laufen soll.
	 * @return Die lokale IP. Bei Fehler: localhost
	 */
	public String getLocalIP(String servicename, boolean chooseNIC)
	{
		
		try{
			
			boolean ok = false;
			while(!ok)
			{
			
				ArrayList<String> IPs = new ArrayList<String>();
				IPs.add("localhost");
				
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements())// für alle elemente der NetzwerkInterfaces
				{
				    NetworkInterface current = interfaces.nextElement();
				    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;// Ungewollte IOs ausschliessen.
				    
				    Enumeration<InetAddress> addresses = current.getInetAddresses();
				    while (addresses.hasMoreElements())// für alle netzadressen der einzelnen NetzwerkInterfaces
				    {
				    	InetAddress current_addr = addresses.nextElement();
				    	if (current_addr.isLoopbackAddress()) continue;
				    	if(current_addr instanceof Inet4Address)// wenn es eine IP4 ist dann gib diese zurück
				    	{
				    		
				    		//System.out.println(current_addr.getHostAddress());
				    		IPs.add( current_addr.getHostAddress());	
				    	}
				    }
				}
				
				if(chooseNIC)
				{
					try {

						if(IPs.size()>0){
							System.out.println("Choose NIC for "+servicename+":");
							for(int i=0; i<IPs.size();i++)
							{
								System.out.println("\t"+i+": "+IPs.get(i));
							}
							System.out.print("->");
							return IPs.get(Integer.parseInt(enigma.readLine()));
							
						}
						else return IPs.get(0);
					
						
					} catch (Exception e) {
						System.out.println("Invalid input. Try again!");
					}
					
				}
				else
				{
					return IPs.get(0);
					
				}
			}
			
		}catch(Exception e){// Falls kaputt gib localhost zurück
			System.out.println("Local InetAddress could not be discovered.. Defaulting: 'localhost'");
			//e.printStackTrace();
		}
		return "localhost";
	}
	
	
	
	/**
	 * 
	 * @param message: The message.
	 * @param args[]: the vars that need to be put in the message.
	 * @param col: The Color of the message. null if default
	 * @param soundname: The Name of the Soundfile. null if none.
	 */
	public void systemMessageOutput(String message,String[] args,Color col,String soundname)
	{
		if(args != null) for(int i=0;i<args.length;i++) message = message.replace("["+(i+1)+"]", args[i]);
		
		if(soundname != null && soundname != "")wavPlay(soundname);
		Color bkup = getConsole().getTextAttributes().getForeground();
		getConsole().setTextAttributes(new TextAttributes(col));
		System.out.println("LAChS: "+message);
		getConsole().setTextAttributes(new TextAttributes(bkup));
	}
	/**
	 * 
	 * @param message: The message.
	 * @param args[]: the vars that need to be put in the message.
	 * @param col: The Color of the message. null if default
	 * @param soundname: The Name of the Soundfile. null if none.
	 */
	public void systemMessageOutput(String message,Color col,String soundname)
	{	
		systemMessageOutput(message,null, col, soundname);
	}
	/**
	 * 
	 * @param key: The keycode for the translation function,
	 * @param args[]: the vars that need to be put in the message.
	 * @param col: The Color of the message. null if default
	 * @param soundname: The Name of the Soundfile. null if none.
	 */
	public void translatedSystemMessageOutput(String key,String[] args,Color col,String soundname)
	{
		String message = "Unknown error..";
		try {
			message = lc.translate(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(args != null) for(int i=0;i<args.length;i++) message = message.replace("["+(i+1)+"]", args[i]);
		
		if(soundname != null && soundname != "")wavPlay(soundname);
		Color bkup = getConsole().getTextAttributes().getForeground();
		getConsole().setTextAttributes(new TextAttributes(col));
		System.out.println("LAChS: "+message);
		getConsole().setTextAttributes(new TextAttributes(bkup));
	}	
	/**
	 * 
	 * @param key: The keycode for the translation function.	 
	 * @param col: The Color of the message. null if default
	 * @param soundname: The Name of the Soundfile. null if none.
	 */
	public void translatedSystemMessageOutput(String key,Color col,String soundname)
	{
		translatedSystemMessageOutput(key, null, col, soundname);
	}
	/**
	 * 
	 * @param sendername: The one who sent the message
	 * @param message: The message.
	 * @param col: The Color of the message. null if default
	 * @param soundname: The Name of the Soundfile. null if none.
	 */
	public void MessageOutput(String sendername,String message,Color col,String soundname)
	{
		if(soundname != null && soundname != "")wavPlay(soundname);
		Color bkup = getConsole().getTextAttributes().getForeground();
		getConsole().setTextAttributes(new TextAttributes(col));
		System.out.println(sendername+message);
		getConsole().setTextAttributes(new TextAttributes(bkup));
	}
	
	public void MessageOutput(String message,Color col,String soundname)
	{
		if(soundname != null && soundname != "")wavPlay(soundname);
		Color bkup = getConsole().getTextAttributes().getForeground();
		getConsole().setTextAttributes(new TextAttributes(col));
		System.out.println(message);
		getConsole().setTextAttributes(new TextAttributes(bkup));
	}
	
	public static int findFreePort()
	{
		ServerSocket socket = null;
	
		try
		{
			socket = new ServerSocket(0);
			int port = socket.getLocalPort();
			return port;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally
		{
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	
	
}
