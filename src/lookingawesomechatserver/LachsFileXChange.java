package lookingawesomechatserver;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import sun.misc.BASE64Decoder;

public class LachsFileXChange extends Thread{

	int port = 0;
	String host = null;
	LachsServer parent = null;
	
	ServerSocketChannel ssc = null;
	Utils u = null;
	
	boolean silent = false;
	boolean quit = false;
	
	public LachsFileXChange(int port,String host,Utils u,boolean silent)
	{
		this.port = port;
		this.host = host;
		this.u = u;
		this.silent = silent;
	}
	
	public LachsFileXChange(){};
	
	public void run() {
		
		if(!silent)u.printGrayln("LPL Server startet");
		
		SocketChannel fileChannel = null;
		try {
			
			ssc = ServerSocketChannel.open();
			ssc.bind(new InetSocketAddress(host,port));			
			fileChannel = ssc.accept();
			
			FileRecieve(".\\downloads\\test.jpg", fileChannel);		
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {fileChannel.close();} catch (Exception e) {if(!silent)u.printError("Channel already closed..");}
		}
	}
	
	
	
	public void copyFile(String SourceFilePath, String DestFilePath)
	{
		RandomAccessFile reqFile = null;
		
		RandomAccessFile destFile = null;
		
		try {
		
			//setup
			reqFile = new RandomAccessFile(SourceFilePath, "rw");
			FileChannel rfChannel = reqFile.getChannel();
			destFile = new RandomAccessFile(DestFilePath, "rw");
			FileChannel outChannel = destFile.getChannel();
			
			//fill stream from file
			ByteBuffer inBuff = ByteBuffer.allocate(48);
			
			while(rfChannel.read(inBuff) >= 0)
			{
				inBuff.flip();
				while(inBuff.hasRemaining())
				{
					outChannel.write(inBuff);
				}
				inBuff.clear();				
				System.out.print(".");
			}
			outChannel.force(true);
			System.out.print("\n");
			
			rfChannel.close();
			outChannel.close();
			reqFile.close();
			destFile.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void FileSendInit(String SourceFilePath)
	{
		SocketChannel channel = null;
		
		try {
			
			channel = SocketChannel.open();
			channel.connect(new InetSocketAddress(host, port));
			
			FileSend(SourceFilePath,channel);
			
		} catch (UnknownHostException e) {
			if(!silent)u.printError("Unkown host error..");
		} catch (IOException e) {
			if(!silent)u.printError("IOException..");
		}
		finally{
			try {channel.close();} catch (Exception e) {if(!silent)u.printError("Channel already closed..");}		
		}		
	}
	
	public void FileSend(String SourceFilePath,SocketChannel destChannel)
	{
		RandomAccessFile file = null;
		FileChannel rfChannel = null;
		
		try {
			
			file = new RandomAccessFile(SourceFilePath, "rw");
			rfChannel = file.getChannel();
			int count = 0;
			ByteBuffer buff = ByteBuffer.allocate(48);
			
			if(!silent)u.printGrayln("Bytes: "+file.length()+" Sending: ");
			while(rfChannel.read(buff) >= 0)
			{
				
				buff.flip();
				while(buff.hasRemaining()){
					
					destChannel.write(buff);
				}								
				buff.compact();
				if(!silent)u.printGray("|");
				count++;
			}
			if(!silent)u.printGrayln(" done_ transferedPackets:"+count+"\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try {file.close();} catch (Exception e) {if(!silent)u.printError("File already closed..");}
			try {rfChannel.close();} catch (Exception e) {if(!silent)u.printError("Channel already closed..");}
			try {destChannel.close();} catch (Exception e) {if(!silent)u.printError("Channel already closed..");}			
		}
		
	}
	
	public void FileRecieve(String DestFilePath,SocketChannel sourceChannel)
	{
		RandomAccessFile file = null;
		FileChannel rfChannel = null;
		
		try {
			
			file = new RandomAccessFile(DestFilePath, "rw");
			rfChannel = file.getChannel();
			int transferedBytes = 0;
			int packetcount = 0;
			
			ByteBuffer buff = ByteBuffer.allocate(48);
			int bytesRead = 0;			
			
			if(!silent)u.printGrayln("Downloading: ");
			while((bytesRead = sourceChannel.read(buff)) >= 0)
			{									
				transferedBytes += bytesRead;
				buff.flip();
				while(buff.hasRemaining())
				{									
					rfChannel.write(buff);					
				}
				buff.compact();				
				if(!silent)u.printGray("|");packetcount++;	
			}
			rfChannel.force(true);
			if(!silent)u.printGrayln(" done_\n Packets:"+packetcount+" transferedBytes: "+transferedBytes+"\n");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{

			try {file.close();} catch (Exception e) {if(!silent)u.printError("File already closed..");}
			try {rfChannel.close();} catch (Exception e) {if(!silent)u.printError("Channel already closed..");}
			try {sourceChannel.close();} catch (Exception e) {if(!silent)u.printError("Channel already closed..");}			
		}
	}	
}
