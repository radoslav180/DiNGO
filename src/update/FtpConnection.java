package update;

import org.apache.commons.net.ftp.FTPClient;
import java.io.*;
import java.util.logging.Logger;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

//singletone class
/**
 * <p>Class responsible for connection to FTP server. The class is implemented as 
 * singleton. The article found on <a href='https://www.codejava.net/java-se/networking/ftp/java-ftp-file-download-
 * tutorial-and-example'>link</a>
 * was very helpful.
 * </p>
 * @author Radoslav Davidović
 */
public class FtpConnection{
	private static FtpConnection ftpConnection = null;
	private FTPClient ftpClient;
	private String ftpAddress;
	private String user = "anonymous";//username default value
	private String password = "dingo.lab180@gmail.com";//password. e-mail address
	int port = 21;
	
	private FtpConnection(){
		this.ftpClient = new FTPClient();
	}
	
	private FtpConnection(String ftpAddress){
		this();
		this.ftpAddress = ftpAddress;
	}
	
	private FtpConnection(String ftpAddress, String user, String password, int port){
                this.ftpClient = new FTPClient();
		this.ftpAddress = ftpAddress;
		this.user = user;
		this.password = password;
		this.port = port;
	}
	
	//make connection and login
	public void connectAndLogin() throws IOException{
		ftpClient.connect(ftpAddress, port);
		ftpClient.login(user, password);
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	}
	//disconnect
	public void disconnect(){
	if (this.ftpClient.isConnected()) {
            try {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
            } catch (IOException f) {
                System.out.println(f.getMessage());
            }
        }
	}
	
	//return Date when entry was modified last time
	public Date retrieveLastModTimeFromServer (String subFolder, String fileName) throws IOException{
		boolean isChanged = ftpClient.changeWorkingDirectory(subFolder);
		String modDate;
		if(isChanged){
			modDate = ftpClient.getModificationTime(fileName);
			ftpClient.changeWorkingDirectory("/");// reset to root directory
		}
		else{
			return null;
		}
		
		return formatServerDate(modDate);
	}
	
	//format date
	public static Date formatServerDate(String unformattedDate){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date formattedDate = null;
		          
		try{
			formattedDate = dateFormat.parse(unformattedDate.substring(unformattedDate.indexOf(" ") + 1));// izbacujemo kod pre formatiranja
		} catch(ParseException ex){
			Logger.getGlobal().info(ex.getMessage());
		}
		
		return formattedDate;
	}
        
	//return FtpConnection instance
	public static FtpConnection getInstance(){
		if(ftpConnection == null){
			ftpConnection = new FtpConnection();
		}
		
		return ftpConnection;
	}
        
        public static FtpConnection getInstance(String ftpAddress){
            if(ftpConnection == null){
                ftpConnection = new FtpConnection(ftpAddress);
            }
            
            return ftpConnection;
        }
        
        public static FtpConnection getInstance(String ftpAddress, String user, 
                String password, int port){
            if(ftpConnection == null){
                ftpConnection = new FtpConnection(ftpAddress, user, password, 
                        port);
            }
            
            return ftpConnection;
        }
	
	//getter and setter methods
	//--------------------------------------
	public FTPClient getFTPClient(){
		return ftpClient;
	}
	
	
	public String getFtpAddress(){
		return ftpAddress;
	}
	
	public String getUser(){
		return user;
	}
	
	public String getPassword(){
		return password;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setFtpAddress(String ftpAddress){
		this.ftpAddress = ftpAddress;
	}
	
	
}
