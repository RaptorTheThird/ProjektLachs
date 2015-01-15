package lookingawesomechatserver;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import org.omg.CORBA_2_3.portable.OutputStream;

import sun.corba.OutputStreamFactory;

public class LachsFileHandling {

	public static ArrayList<LachsUser> getAllLachsUser(String LachsDBfileName)
	{
		ArrayList<LachsUser> lul = new ArrayList<LachsUser>();
		Path path = Paths.get(LachsDBfileName);
		try {
			
			BufferedReader reader = Files.newBufferedReader(path,Charset.forName("UTF-8"));
			String data = null;
			
			while((data = reader.readLine()) != null)
			{
				String subdata[] = data.split(";");
				LachsUser tmp = new LachsUser(subdata[0],subdata[1], LachsUserRights.valueOf(subdata[2]));
				
				lul.add(tmp);
				
			}	
			
			return lul;
		} catch (Exception e) {
			// TODO: handle exception
		}		
		return null;
	}
	
	public static void saveAllLachsUser(ArrayList<LachsUser>allu,String LachsDBfileName)
	{	
		Path path = Paths.get(LachsDBfileName);
		BufferedWriter writer = null;
		
	    try 
	    {
	    	writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));
	    	
	    	
	    	for(LachsUser data : allu)
		    {	
		        writer.write(data.uname+";"+data.passwd+";"+data.lur.toString());
		        writer.newLine();
		        writer.flush();
		    }
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    finally
	    {
	    	try {
				
	    		writer.close();
	    		
			} catch (Exception e2) {
				e2.printStackTrace();
			}
	    }
	}
	
	
	public static void saveClientConfig(LachsClient lc,String LCCFileName)
	{
		System.out.println("saving..");
		Properties prop = new Properties();
		FileOutputStream save = null;
		
		try{
			
			
			save = new FileOutputStream(LCCFileName);
			
			prop.setProperty("LAChS_IP", lc.serverip);
			prop.setProperty("LAChS_Port", String.valueOf(lc.serverport));
			prop.setProperty("Console_Color", String.valueOf(lc.consoleColor.getRGB()));
			prop.setProperty("Language", lc.language);
			
			prop.storeToXML(save, null);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{			
			if(save != null)
				try {
					save.close();
				} catch (Exception e) {
					e.printStackTrace();
				}			
		}
		System.out.println("..done");
	}
	
	public static void loadClientConfig(LachsClient lc,String LCCFileName)
	{
		System.out.println("loading Client data..");
		Properties prop = new Properties();
		FileInputStream load = null;
		
		try{
			load = new FileInputStream(LCCFileName);
			prop.loadFromXML(load);
			
			lc.serverip = prop.getProperty("LAChS_IP");
			lc.serverport = Integer.parseInt(prop.getProperty("LAChS_Port"));			
			lc.consoleColor = Color.decode(prop.getProperty("Console_Color"));
			lc.language = prop.getProperty("Language");
			
			
		}catch(Exception ex){
			
			System.out.println("configfile not found. using default settings..");
			
			lc.serverip = "localhost";
			lc.serverport = 12321;
			lc.consoleColor = Color.GREEN;	
			lc.language = "english";
			
		}finally{			
			if(load != null)
				try {
					load.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}
		System.out.println("..done");
	}
}
