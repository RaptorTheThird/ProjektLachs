package lookingawesomechatserver;

import java.io.BufferedWriter;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LachsLogger {

	DateFormat dateFormat = null;
	
	Path path = null;
	BufferedWriter writer = null;
	
	Calendar referenceDate = null;
	
	
	public LachsLogger()
	{
		init();
	}
	
	public void close()
	{
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init()
	{
		dateFormat = new SimpleDateFormat("yyyyMMMdd HH:mm:ss");		
		referenceDate = Calendar.getInstance();
		
		path = Paths.get(LachsServer.LACHSLOGFOLDER+"lachsLogFile"+new SimpleDateFormat("yyyyMMMdd").format(new Date())+".tin");
		
		try {
			
			writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"),StandardOpenOption.CREATE);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void reinit()
	{
		try {
			System.out.println("Date changed! ReInit logfile..");
			
			writer.close();
			
			path = Paths.get(LachsServer.LACHSLOGFOLDER+"lachsLogFile"+new SimpleDateFormat("yyyyMMMdd").format(new Date())+".tin");
			
			writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"),StandardOpenOption.CREATE);			
			
			referenceDate = Calendar.getInstance();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String cmd)
	{
		if(referenceDate.get(Calendar.DAY_OF_YEAR)!=Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
		{
			reinit();
		}
		
		String msg = dateFormat.format(new Date())+":"+ cmd;
		
		try {
			
			writer.write(msg);
			writer.newLine();
			writer.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println(msg);
		
		
	}
	
	public void log(String cmd,SocketChannel sender)
	{				
		if(referenceDate.get(Calendar.DAY_OF_YEAR)!=Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
		{
			reinit();
		}
		
		try {
			
			String from = sender.getRemoteAddress().toString();
			
			String msg = dateFormat.format(new Date())+" from "+from+": "+ cmd;
			
			
			
			
			System.out.println(msg);
			//System.out.println("rd:"+referenceDate.getTimeInMillis()+" nd:"+Calendar.getInstance().getTimeInMillis());
		
			writer.write(msg);
			writer.newLine();
			writer.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	

	
}
