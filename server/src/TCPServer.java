import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class TCPServer
{
	static String dir = "C:\\";
	static int Port = 21;
	private static ServerSocket s;
	
	public static void main(String[] args)
	{
	    for(int i = 0; i < args.length; i++)
	    {
	        if(args[i].equals("-d"))
	        {
	        	dir = args[i+1];
	        	System.out.println("Set to " + dir);
	            i++;
	        }
	    }
	    
		int i = 1;
		try
		{
			s = new ServerSocket(Port);
			while(true)
			{				
				Socket client = s.accept();
				Thread t = new FtpServer(client, i, dir);
				t.start();				
				i++;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
	
class FtpServer extends Thread
{
	static Socket client;
	int counter;
	String dir = "C:\\"; 
	
	String reply;
	InputStream inputStream;
	BufferedReader in;
	OutputStream output;
	PrintWriter out;
	byte[] buffer = new byte[4096];
	int temp;
	
	boolean portReady, pasvReady, login, rename;
	int filePort;
	String fileIP;
	ServerSocket fileServerSocket;
	Socket fileSocket;
	String input;
	StringTokenizer st;			
	String cmd;
	String param;
	String renameFile;
	
	FtpServer(Socket client, int i, String dir) throws IOException
	{
		FtpServer.client = client;
		this.counter = i;
		this.dir = dir;
		this.inputStream = FtpServer.client.getInputStream();
		this.in = new BufferedReader(new InputStreamReader(FtpServer.client.getInputStream()));
		this.output = FtpServer.client.getOutputStream();
		this.out = new PrintWriter(output,true);
		this.portReady = false;
		this.pasvReady = false;
		this.login = false;
		this.rename = false;
	}
	
	public void run()
	{
		try
		{
			reply = "220 Service ready for new user.\r\n";
			output.write(reply.getBytes());
			output.flush();
			
			while(!login && !client.isClosed())
			{
				input = in.readLine();
				st = new StringTokenizer(input);
				if(!st.hasMoreTokens())
					continue;
				cmd = st.nextToken();
		
				switch(cmd)
				{
					case "USER":
						user();
						break;
					case "QUIT":
						quit();
						break;
					case "ABOR":
						abor();
						break;
					case "SYST":
						syst();
						break;
					case "TYPE":
						type();
						break;
					default:
						commandNotFound();
				}
			}

			
			while(!client.isClosed())
			{
				input = in.readLine();
				st = new StringTokenizer(input);
				if(!st.hasMoreTokens())
					continue;
				cmd = st.nextToken();				
				switch(cmd)
				{
					case "QUIT":
						quit();
						break;
					case "ABOR":
						abor();
						break;
					case "SYST":
						syst();
						break;
					case "TYPE":
						type();
						break;
					case "PORT":
						port();
						break;
					case "PASV":
						pasv();
						break;
					case "RETR":
						retr();
						break;
					case "STOR":
						stor();
						break;
					case "CWD":
						cwd();
						break;
					case "CDUP":
						cdup();
						break;
					case "DELE":
						dele();
						break;
					case "MKD":
						mkd();
						break;
					case "PWD":
						pwd();
						break;
					case "RMD":
						rmd();
						break;
					case "RNFR":
						rnfr();
						break;
					case "RNTO":
						rnto();
						break;
					case "LIST":
						list();
						break;
					default:
						commandNotFound();
				}
			}
		}
		catch(IOException e)
		{
			System.out.println("Thread " + counter + " error.\r\n");
		}
	}
	
	public void user()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = st.nextToken();
				if(st.hasMoreTokens())
				{
					reply = "501 Syntax error in parameters or arguments.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
				else if(param.equals("anonymous"))
				{
					reply = "331 User name okay, need password.\r\n";
					output.write(reply.getBytes());
					output.flush();
					
					input = in.readLine();
					st = new StringTokenizer(input);
					cmd = st.nextToken();
				
					switch(cmd)
					{
						case "PASS":
							pass();
							break;
						case "QUIT":
							quit();
							break;
						case "ABOR":
							abor();
							break;
						case "SYST":
							syst();
							break;
						case "TYPE":
							type();
							break;
						default:
							commandNotFound();
					}
				}
				else
				{
					reply = "530 Not logged in.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("USER error.\r\n");
		}
	}	

	public void pass()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = st.nextToken();
				if(st.hasMoreTokens())
				{
					reply = "501 Syntax error in parameters or arguments.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
				else
				{
					reply = "230 User logged in, proceed.\r\n";
					output.write(reply.getBytes());
					output.flush();
					login = true;
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("PASS error.\r\n");
		}
	}
	
	public void quit()
	{
		try{
			if(!st.hasMoreTokens())
			{
				reply = "221 Service closing control connection.\r\n";
				output.write(reply.getBytes());
				output.flush();
				client.close();
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("QUIT error.\r\n");
		}
	}
	
	public void abor()
	{
		try{
			if(!st.hasMoreTokens())
			{
				reply = "221 Service closing control connection.\r\n";
				output.write(reply.getBytes());
				output.flush();
				client.close();
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("ABOR error.\r\n");
		}
	}

	public void syst()
	{
		try{
			if(!st.hasMoreTokens())
			{
				reply = "215 UNIX Type: L8\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("SYST error.\r\n");
		}
	}

	public void type()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = st.nextToken();
				if(param.equals("I"))
				{
					if(!st.hasMoreTokens())
					{
						reply = "200 Type set to I.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					else
					{
						reply = "501 Syntax error in parameters or arguments.\r\n";
						output.write(reply.getBytes());	
						output.flush();
					}						
				}
				else
				{
					reply = "501 Syntax error in parameters or arguments.\r\n";
					output.write(reply.getBytes());	
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("TYPE error.\r\n");
		}
	}

	public void port()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = st.nextToken();
				if(param.matches("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\,){5}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))"))
				{
					if(!st.hasMoreTokens())
					{
						String[] nums = param.split(",");
						fileIP = nums[0] + "." + nums[1] + "." + nums[2] + "." + nums[3];
						filePort = Integer.parseInt(nums[4]) * 256 + Integer.parseInt(nums[5]);
						if(pasvReady)
							if(!fileServerSocket.isClosed())
								fileServerSocket.close();
						portReady = true;
						pasvReady = false;
						reply = "200 Command okay.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					else
					{
						reply = "501 Syntax error in parameters or arguments.\r\n";
						output.write(reply.getBytes());	
						output.flush();
					}						
				}
				else
				{
					reply = "501 Syntax error in parameters or arguments.\r\n";
					output.write(reply.getBytes());	
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("PORT error.\r\n");
		}
	}

	public void pasv()
	{
		try{
			if(!st.hasMoreTokens())
			{
				try
				{
					if(pasvReady)
						if(!fileServerSocket.isClosed())
							fileServerSocket.close();
					fileServerSocket = new ServerSocket(0);
					fileIP = client.getLocalAddress() + "";
					fileIP = fileIP.substring(1);
					filePort = fileServerSocket.getLocalPort();
					portReady = false;
					pasvReady = true;
					reply = "227 Entering Passive Mode (" + fileIP.replace('.', ',') + "," + (filePort / 256) + "," + (filePort % 256) + ").\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
				catch(IOException e)
				{
					reply = "425 Can¡¯t open data connection.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("PASV error.\r\n");
		}
	}
	
	public void retr()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				if(portReady)
				{
					portReady = false;
					try
					{
						fileSocket = new Socket(fileIP, filePort);
//						fileSocket = new Socket(fileIP, filePort, client.getLocalAddress(), 20);
						File localFile = new File(dir + param);
						if(!localFile.exists())
						{
							reply = "550 Requested action not taken.\r\n";
							output.write(reply.getBytes());
							output.flush();
							fileSocket.close();
						}
						else
						{
							reply = "125 Data connection already open; transfer starting.\r\n";
							output.write(reply.getBytes());
							try
							{
								FileInputStream fis = new FileInputStream(localFile);
								DataOutputStream download = new DataOutputStream(fileSocket.getOutputStream());
								byte[] buffer= new byte[4096];
								int bytes;
								while((bytes = fis.read(buffer)) != -1)
								{	
									download.write(buffer, 0, bytes);			
								}
								fis.close();
								download.close();
								fileSocket.close();
								reply = "226 Closing data connection.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(FileNotFoundException e)
							{
								fileSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(IOException e)
							{
								fileSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
						}
					}
					catch(IOException e)
					{
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else if(pasvReady)
				{
					pasvReady = false;
					try
					{
						fileSocket = fileServerSocket.accept();
						File localFile = new File(dir + param);
						if(!localFile.exists())
						{
							reply = "550 Requested action not taken.\r\n";
							output.write(reply.getBytes());
							output.flush();
							fileSocket.close();
							fileServerSocket.close();							
						}
						else
						{
							reply = "125 Data connection already open; transfer starting.\r\n";
							output.write(reply.getBytes());
							output.flush();
							try
							{
								FileInputStream fis = new FileInputStream(localFile);
								DataOutputStream download = new DataOutputStream(fileSocket.getOutputStream());
								byte[] buffer= new byte[4096];
								int bytes;
								while((bytes = fis.read(buffer)) != -1)
								{	
									download.write(buffer, 0, bytes);			
								}
								fis.close();
								download.close();
								fileSocket.close();
								fileServerSocket.close();
								reply = "226 Closing data connection.\r\n";
								output.write(reply.getBytes());
							}
							catch(FileNotFoundException e)
							{
								fileSocket.close();
								fileServerSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(IOException e)
							{
								fileSocket.close();
								fileServerSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
						}
					}
					catch(IOException e)
					{
						fileServerSocket.close();
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else
				{
					reply = "425 Can't open data connection.\r\n";
					output.write(reply.getBytes());	
					output.flush();
				}					
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());	
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("RETR error.\r\n");
		}
	}
	
	public void stor()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				if(portReady)
				{
					portReady = false;
					try
					{
						fileSocket = new Socket(fileIP, filePort);
//						fileSocket = new Socket(fileIP, filePort, client.getLocalAddress(), 20);
						reply = "125 Data connection already open; transfer starting.\r\n";
						output.write(reply.getBytes());
						output.flush();
						try
						{
							File localFile = new File(dir + param);
							FileOutputStream fos = null;
							InputStream download = fileSocket.getInputStream();
							fos = new FileOutputStream(localFile);
							byte[] content = new byte[4096];
							int bytes;
							while((bytes = (download.read(content))) != -1)
							{
								fos.write(content, 0, bytes);
							}							
							fos.close();
							download.close();
							fileSocket.close();
							reply = "226 Closing data connection.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
						catch(FileNotFoundException e)
						{
							fileSocket.close();
							reply = "426 Connection closed; transfer aborted.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
						catch(IOException e)
						{
							fileSocket.close();
							reply = "426 Connection closed; transfer aborted.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
					}
					catch(IOException e)
					{
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else if(pasvReady)
				{
					pasvReady = false;
					try
					{
						fileSocket = fileServerSocket.accept();
						reply = "125 Data connection already open; transfer starting.\r\n";
						output.write(reply.getBytes());
						output.flush();
						try
						{
							File localFile = new File(dir + param);
							FileOutputStream fos = null;
							InputStream download = fileSocket.getInputStream();
							fos = new FileOutputStream(localFile);
							byte[] content = new byte[4096];
							int bytes;
							while((bytes = (download.read(content))) != -1)
							{
								fos.write(content, 0, bytes);
							}							
							fos.close();
							download.close();
							fileSocket.close();
							fileServerSocket.close();
							reply = "226 Closing data connection.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
						catch(FileNotFoundException e)
						{
							fileSocket.close();
							fileServerSocket.close();
							reply = "426 Connection closed; transfer aborted.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
						catch(IOException e)
						{
							fileSocket.close();
							fileServerSocket.close();
							reply = "426 Connection closed; transfer aborted.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
					}
					catch(IOException e)
					{
						fileServerSocket.close();
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else
				{
					reply = "425 Can't open data connection.\r\n";
					output.write(reply.getBytes());	
					output.flush();
				}				
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("STOR error.\r\n");
		}
	}
	
	public void cwd()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(4);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				if(param.equals("..\\"))
				{
					if(dir.length() != 3)
					{
						dir = dir.substring(0, dir.length() - 1);
						while(dir.charAt(dir.length() - 1) != '\\')
							dir = dir.substring(0, dir.length() - 1);
						reply = "250 Requested file action okay, completed.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					else
					{
						reply = "550 Requested action not taken.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}				
				}
				else 
				{
					if(!param.endsWith(File.separator))
					{
				    	param = param + File.separator;
				    }
					File file = new File(dir + param);
					if(param.charAt(1) == ':')
						file = new File(param);
					if(file.exists() && file.isDirectory())
					{
						try{
							if(param.charAt(1) == ':')
								dir = param;
							else
								dir = dir + param;
							reply = "250 Requested file action okay, completed.\r\n";
							output.write(reply.getBytes());
							output.flush();
						}
						catch(IOException e)
						{
							System.out.println("CWD error.\r\n");
						}
					}
					else
					{
						reply = "550 Requested action not taken.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("CWD error.\r\n");
		}
	}

	public void cdup()
	{
		try{
			if(!st.hasMoreTokens())
			{
				if(dir.length() > 3)
				{
					dir = dir.substring(0, dir.length() - 1);
					while(dir.charAt(dir.length() - 1) != '\\')
						dir = dir.substring(0, dir.length() - 1);
					reply = "200 Command okay.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
				else
				{
					reply = "550 Requested action not taken.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("CDUP error.\r\n");
		}
	}
	
	public void dele()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				File file = new File(dir + param);
				if(file.exists() && file.isFile())
				{
					try{
						file.delete();
						reply = "250 Requested file action okay, completed.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					catch(IOException e)
					{
						System.out.println("450 Requested action not taken.\r\n");
					}
				}
				else
				{
					reply = "550 Requested action not taken.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("DELE error.\r\n");
		}
	}

	public void mkd()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(4);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				File file = new File(dir + param);
				if(!file.exists())
				{
					try{
						file.mkdir();
						reply = "257 \"" + dir + param + "\" created.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					catch(IOException e)
					{
						System.out.println("MKD error.\r\n");
					}
				}
				else
				{
					reply = "550 Requested action not taken.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("MKD error.\r\n");
		}
	}

	public void pwd()
	{
		try{
			if(!st.hasMoreTokens())
			{
				reply = "257 \"" + dir + "\" created.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("PWD error.\r\n");
		}
	}
	
	public void rmd()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(4);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				File file = new File(dir + param);
				if(file.exists() && file.isDirectory())
				{
					deleteDirectory(file);
					reply = "250 Requested file action okay, completed.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
				else
				{
					reply = "550 Requested action not taken.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("RMD error.\r\n");
		}
	}
	
	public void rnfr()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				File file = new File(dir + param);
				if(file.exists())
				{
					try{
						renameFile = dir + param;
						rename = true;
						reply = "250 Requested file action okay, completed.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					catch(IOException e)
					{
						System.out.println("RNFR error.\r\n");
					}
				}
				else
				{
					reply = "550 Requested action not taken.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("RNFR error.\r\n");
		}
	}
	
	public void rnto()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				File oldFile = new File(renameFile);
				File newFile = new File(dir + param);
				if(oldFile.exists() && !newFile.exists() && rename)
				{
					try{
						oldFile.renameTo(newFile);
						rename = false;
						reply = "250 Requested file action okay, completed.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
					catch(IOException e)
					{
						reply = "550 Requested action not taken.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else
				{
					reply = "550 Requested action not taken.\r\n";
					output.write(reply.getBytes());
					output.flush();
				}
			}
			else
			{
				reply = "501 Syntax error in parameters or arguments.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
		}
		catch(IOException e)
		{
			System.out.println("RNTO error.\r\n");
		}
	}
	
	public void list()
	{
		try{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param = param.substring(1);
				if(portReady)
				{
					portReady = false;
					try
					{
						fileSocket = new Socket(fileIP, filePort);
//						fileSocket = new Socket(fileIP, filePort, client.getLocalAddress(), 20);
						File file = new File(dir + param);
						if(param.charAt(1) == ':')
							file = new File(param);
						if(!file.exists() || !file.isDirectory())
						{
							reply = "550 Requested action not taken.\r\n";
							output.write(reply.getBytes());
							output.flush();
							fileSocket.close();
						}
						else
						{
							reply = "125 Data connection already open; transfer starting.\r\n";
							output.write(reply.getBytes());
							try
							{
								OutputStream fileOutput = fileSocket.getOutputStream();
								File[] files = file.listFiles();
								for(int i = 0; i < files.length; i++)
								{
									reply = files[i].getName();
									if(files[i].isDirectory())
								    	reply = "<DIR> " + reply;
									else
								    	reply = "      " + reply;
							    	reply = reply + "\r\n";
									fileOutput.write(reply.getBytes());
								}
								fileSocket.close();
								reply = "226 Closing data connection.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(FileNotFoundException e)
							{
								fileSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(IOException e)
							{
								fileSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
						}
					}
					catch(IOException e)
					{
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else if(pasvReady)
				{
					pasvReady = false;
					try
					{
						fileSocket = fileServerSocket.accept();
						File file = new File(dir + param);
						if(param.charAt(1) == ':')
							file = new File(param);
						if(!file.exists() || !file.isDirectory())
						{
							reply = "550 Requested action not taken.\r\n";
							output.write(reply.getBytes());
							output.flush();
							fileSocket.close();
							fileServerSocket.close();
						}
						else
						{
							reply = "125 Data connection already open; transfer starting.\r\n";
							output.write(reply.getBytes());
							try
							{
								OutputStream fileOutput = fileSocket.getOutputStream();
								File[] files = file.listFiles();
								for(int i = 0; i < files.length; i++)
								{
									reply = files[i].getName();
									if(files[i].isDirectory())
								    	reply = "<DIR> " + reply;
									else
								    	reply = "      " + reply;
							    	reply = reply + "\r\n";
									fileOutput.write(reply.getBytes());
								}
								fileSocket.close();
								fileServerSocket.close();
								reply = "226 Closing data connection.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(FileNotFoundException e)
							{
								fileSocket.close();
								fileServerSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(IOException e)
							{
								fileSocket.close();
								fileServerSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
						}
					}
					catch(IOException e)
					{
						fileServerSocket.close();
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else
				{
					reply = "425 Can't open data connection.\r\n";
					output.write(reply.getBytes());	
					output.flush();
				}					
			}
			else
			{
				if(portReady)
				{
					portReady = false;
					try
					{
						fileSocket = new Socket(fileIP, filePort);
//						fileSocket = new Socket(fileIP, filePort, client.getLocalAddress(), 20);
						File file = new File(dir);
						if(!file.exists() || !file.isDirectory())
						{
							reply = "550 Requested action not taken.\r\n";
							output.write(reply.getBytes());
							output.flush();
							fileSocket.close();
						}
						else
						{
							reply = "125 Data connection already open; transfer starting.\r\n";
							output.write(reply.getBytes());
							try
							{
								OutputStream fileOutput = fileSocket.getOutputStream();
								File[] files = file.listFiles();
								for(int i = 0; i < files.length; i++)
								{
									reply = files[i].getName();
									if(files[i].isDirectory())
								    	reply = "<DIR> " + reply;
									else
								    	reply = "      " + reply;
							    	reply = reply + "\r\n";
									fileOutput.write(reply.getBytes());
								}
								fileSocket.close();
								reply = "226 Closing data connection.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(FileNotFoundException e)
							{
								fileSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(IOException e)
							{
								fileSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
						}
					}
					catch(IOException e)
					{
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else if(pasvReady)
				{
					pasvReady = false;
					try
					{
						fileSocket = fileServerSocket.accept();
						File file = new File(dir);
						if(!file.exists() || !file.isDirectory())
						{
							reply = "550 Requested action not taken.\r\n";
							output.write(reply.getBytes());
							output.flush();
							fileSocket.close();
							fileServerSocket.close();
						}
						else
						{
							reply = "125 Data connection already open; transfer starting.\r\n";
							output.write(reply.getBytes());
							try
							{
								OutputStream fileOutput = fileSocket.getOutputStream();
								File[] files = file.listFiles();
								for(int i = 0; i < files.length; i++)
								{
									reply = files[i].getName();
									if(files[i].isDirectory())
								    	reply = "<DIR> " + reply;
									else
								    	reply = "      " + reply;
							    	reply = reply + "\r\n";
									fileOutput.write(reply.getBytes());
								}
								fileSocket.close();
								fileServerSocket.close();
								reply = "226 Closing data connection.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(FileNotFoundException e)
							{
								fileSocket.close();
								fileServerSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
							catch(IOException e)
							{
								fileSocket.close();
								fileServerSocket.close();
								reply = "426 Connection closed; transfer aborted.\r\n";
								output.write(reply.getBytes());
								output.flush();
							}
						}
					}
					catch(IOException e)
					{
						fileServerSocket.close();
						reply = "425 Can¡¯t open data connection.\r\n";
						output.write(reply.getBytes());
						output.flush();
					}
				}
				else
				{
					reply = "425 Can't open data connection.\r\n";
					output.write(reply.getBytes());	
					output.flush();
				}	
			}

		}
		catch(IOException e)
		{
			System.out.println("LIST error.\r\n");
		}
	}
	
	public void commandNotFound()
	{
		try{
			reply = "500 Syntax error, command unrecognized.\r\n";
			output.write(reply.getBytes());
			output.flush();
		}
		catch(IOException e)
		{
			System.out.println("Thread " + counter + " error.\r\n");
		}
	}
	
	public void deleteDirectory(File file)
	{
		try{
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++)
			{
				if(files[i].isFile())
					files[i].delete();
				else
					deleteDirectory(files[i]);
			}
			file.delete();
		}
		catch(Exception e)
		{
			try
			{
				reply = "450 Requested action not taken.\r\n";
				output.write(reply.getBytes());
				output.flush();
			}
			catch(IOException E)
			{
				System.out.println("RMD error.\r\n");
			}
		}
	}
}