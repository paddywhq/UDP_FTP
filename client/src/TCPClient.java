import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class TCPClient
{
	static final String IP = "127.0.0.1";
	private static int Port = 21;
	
	static InputStream inputStream;
	static BufferedReader in;
	static OutputStream output;
	static PrintWriter out;
	static BufferedReader stdin;
	static String response, input, cmd, param;
	static byte[] buffer = new byte[4096];
	static int temp;
	
	static boolean portReady, pasvReady;
	static int filePort;
	static String fileIP;
	static ServerSocket fileServerSocket;
	static Socket fileSocket;
	
	static Socket tcpClient;
	static StringTokenizer st;
	
	public static void main(String args[])
	{
		try
		{
			tcpClient = new Socket(IP, Port);
			boolean login = false;
			output = tcpClient.getOutputStream();
			out = new PrintWriter(output, true);
			inputStream = tcpClient.getInputStream();
			in = new BufferedReader(new InputStreamReader(inputStream));
			stdin = new BufferedReader(new InputStreamReader(System.in));
			
			portReady = false;
			pasvReady = false;

			
			response = in.readLine();
			System.out.println(response);
			
			while(!login && !tcpClient.isClosed())
			{
				if(response.startsWith("230"))
					break;
				input = stdin.readLine();
				st = new StringTokenizer(input);
				if(!st.hasMoreTokens())
					continue;
				cmd = st.nextToken();
				switch (cmd)
				{
					case "QUIT":
						quit();
						break;
					case "ABOR":
						abor();
						break;
					default:
						otherCommands();
				}
			}

			while(!tcpClient.isClosed())
			{
				try
				{
					input = stdin.readLine();
					st = new StringTokenizer(input);
					if(!st.hasMoreTokens())
						continue;
					cmd = st.nextToken();					
					switch (cmd)
					{
						case "QUIT":
							quit();
							break;
						case "ABOR":
							abor();
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
						case "LIST":
							list();
							break;
						default:
							otherCommands();
					}
				}
				catch(IOException e)
				{
					System.out.print("Unknown error.\r\n");
					break;
				}
			}
		}
		catch(Exception e)
		{
			System.out.print("Unknown error.\r\n");
		}
	}
	
	public static void quit()
	{
		try
		{
			output.write((input + "\r\n").getBytes());			
			response = in.readLine();
			System.out.println(response);
			if(response.startsWith("2")){
				tcpClient.close();
			}
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("QUIT error.\r\n");			
			}
		}
	}
	
	public static void abor()
	{
		try
		{
			output.write((input + "\r\n").getBytes());			
			response = in.readLine();
			System.out.println(response);
			if(response.startsWith("2")){
				tcpClient.close();
			}
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("ABOR error.\r\n");			
			}
		}
	}
	
	public static void port()
	{
		try
		{
			output.write((input + "\r\n").getBytes());			
			response = in.readLine();
			System.out.println(response);
			if(response.startsWith("2")){
				portReady = true;
				pasvReady = false;
				param = st.nextToken();
				String[] nums = param.split(",");
				fileIP = nums[0] + "." + nums[1] + "." + nums[2] + "." + nums[3];
				filePort = Integer.parseInt(nums[4]) * 256 + Integer.parseInt(nums[5]);
			}
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("PORT error.\r\n");		
			}
		}
	}

	public static void pasv()
	{
		try
		{
			output.write(("PASV" + "\r\n").getBytes());			
			response = in.readLine();
			System.out.println(response);
			if(response.startsWith("2")){
				portReady = false;
				pasvReady = true;
				String[] nums = response.split(",");
				nums[0] = nums[0].substring(nums[0].length() - 3);
				while(nums[0].charAt(0) < 48 || nums[0].charAt(0) > 57)
					nums[0] = nums[0].substring(1);
				nums[5] = nums[5].substring(0, 3);
				while(nums[5].charAt(nums[5].length() - 1) < 48 || nums[5].charAt(nums[5].length() - 1) > 57)
					nums[5] = nums[5].substring(0, nums[5].length() - 1);
				fileIP = nums[0] + "." + nums[1] + "." + nums[2] + "." + nums[3];
				filePort = Integer.parseInt(nums[4]) * 256 + Integer.parseInt(nums[5]);
			}
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("PASV error.\r\n");		
			}
		}
	}
	
	public static void retr()
	{
		try
		{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param.substring(1);
				if(portReady)
				{
					portReady = false;
					output.write((input + "\r\n").getBytes());
					fileServerSocket = new ServerSocket(filePort);
					fileSocket = fileServerSocket.accept();			
					response = in.readLine();
					System.out.println(response);
					if(response.startsWith("1"))
					{
						try{
							File localFile = new File(param);
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
							response = in.readLine();
							System.out.println(response);
						}
						catch(FileNotFoundException e)
						{
							response = in.readLine();
							System.out.println(response);
						}
						catch(IOException e)
						{
							response = in.readLine();
							System.out.println(response);
						}
					}
					fileSocket.close();
					fileServerSocket.close();
				}
				else if(pasvReady)
				{
					pasvReady = false;
					output.write((input + "\r\n").getBytes());
					fileSocket = new Socket(fileIP, filePort, tcpClient.getLocalAddress(), 20);				
					response = in.readLine();
					System.out.println(response);
					if(response.startsWith("1"))
					{
						try{
							File localFile = new File(param);
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
							response = in.readLine();
							System.out.println(response);
						}
						catch(FileNotFoundException e)
						{
							response = in.readLine();
							System.out.println(response);
						}
						catch(IOException e)
						{
							response = in.readLine();
							System.out.println(response);
						}
					}
					fileSocket.close();
				}
				else
				{
					output.write((input + "\r\n").getBytes());					
					response = in.readLine();
					System.out.println(response);
				}
			}
			else
			{
				output.write((input + "\r\n").getBytes());				
				response = in.readLine();
				System.out.println(response);
			}
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("RETR error.\r\n");		
			}
		}
	}
	
	public static void stor()
	{
		try
		{
			if(st.hasMoreTokens())
			{
				param = input.substring(5);
				while(param.charAt(0) == ' ')
					param.substring(1);
				if(portReady)
				{
					portReady = false;
					File localFile = new File(param);
					if(!localFile.exists())
					{
						System.out.print("File not Found.\r\n");
					}
					else
					{
						output.write((input + "\r\n").getBytes());
						fileServerSocket = new ServerSocket(filePort);
						fileSocket = fileServerSocket.accept();						
						response = in.readLine();
						System.out.println(response);
						if(response.startsWith("1"))
						{
							try{
								FileInputStream fis = new FileInputStream(param);
								DataOutputStream download = new DataOutputStream(fileSocket.getOutputStream());
								byte[] buffer= new byte[4096];
								int bytes;
								while((bytes = fis.read(buffer)) != -1)
								{	
									download.write(buffer, 0, bytes);			
								}
								fis.close();
								download.close();								
								response = in.readLine();
								System.out.println(response);
							}
							catch(FileNotFoundException e)
							{
								response = in.readLine();
								System.out.println(response);
							}
							catch(IOException e)
							{
								response = in.readLine();
								System.out.println(response);
							}
						}
						fileSocket.close();
						fileServerSocket.close();
					}
				}
				else if(pasvReady)
				{
					pasvReady = false;
					File localFile = new File(param);
					if(!localFile.exists())
					{
						System.out.print("File not Found.\r\n");
					}
					else
					{
						output.write((input + "\r\n").getBytes());
						fileSocket = new Socket(fileIP, filePort, tcpClient.getLocalAddress(), 20);						
						response = in.readLine();
						System.out.println(response);
						if(response.startsWith("1"))
						{
							try{
								FileInputStream fis = new FileInputStream(param);
								DataOutputStream download = new DataOutputStream(fileSocket.getOutputStream());
								byte[] buffer= new byte[4096];
								int bytes;
								while((bytes = fis.read(buffer)) != -1)
								{	
									download.write(buffer, 0, bytes);			
								}
								fis.close();
								download.close();								
								response = in.readLine();
								System.out.println(response);
							}
							catch(FileNotFoundException e)
							{
								response = in.readLine();
								System.out.println(response);
							}
							catch(IOException e)
							{
								response = in.readLine();
								System.out.println(response);
							}
						}
						fileSocket.close();
					}
				}
				else
				{
					output.write((input + "\r\n").getBytes());					
					response = in.readLine();
					System.out.println(response);
				}
			}
			else
			{
				output.write((input + "\r\n").getBytes());
				response = in.readLine();
				System.out.println(response);
			}
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("STOR error.\r\n");			
			}
		}
	}

	public static void list()
	{
		try
		{
			if(portReady)
			{
				portReady = false;
				output.write((input + "\r\n").getBytes());
				fileServerSocket = new ServerSocket(filePort);
				fileSocket = fileServerSocket.accept();			
				response = in.readLine();
				System.out.println(response);
				if(response.startsWith("1"))
				{
					try{
						InputStream fileInput = fileSocket.getInputStream();
						BufferedReader fileInfo = new BufferedReader(new InputStreamReader(fileInput));
						while(true)
						{
							String fileName = fileInfo.readLine();
							if(fileName == null)
								break;
							System.out.println(fileName);
						}							
						response = in.readLine();
						System.out.println(response);
					}
					catch(FileNotFoundException e)
					{
						response = in.readLine();
						System.out.println(response);
					}
					catch(IOException e)
					{
						response = in.readLine();
						System.out.println(response);
					}
				}
				fileSocket.close();
				fileServerSocket.close();
			}
			else if(pasvReady)
			{
				pasvReady = false;
				output.write((input + "\r\n").getBytes());
				fileSocket = new Socket(fileIP, filePort, tcpClient.getLocalAddress(), 20);							
				response = in.readLine();
				System.out.println(response);
				if(response.startsWith("1"))
				{
					try{
						InputStream fileInput = fileSocket.getInputStream();
						BufferedReader fileInfo = new BufferedReader(new InputStreamReader(fileInput));
						while(true)
						{
							String fileName = fileInfo.readLine();
							if(fileName == null)
								break;
							System.out.println(fileName);
						}								
						response = in.readLine();
						System.out.println(response);
					}
					catch(FileNotFoundException e)
					{
						response = in.readLine();
						System.out.println(response);
					}
					catch(IOException e)
					{
						response = in.readLine();
						System.out.println(response);
					}
				}
				fileSocket.close();
			}
			else
			{
				output.write((input + "\r\n").getBytes());					
				response = in.readLine();
				System.out.println(response);
			}
			output.flush();		
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("LIST error.\r\n");		
			}
		}
	}
	
	public static void otherCommands()
	{
		try
		{
			output.write((input + "\r\n").getBytes());
			response = in.readLine();
			System.out.println(response);
			output.flush();
		}
		catch(Exception e)
		{
			try
			{
				response = in.readLine();
				System.out.println(response);
			}
			catch(Exception E)
			{
				System.out.print("Unknown error.\r\n");	
			}
		}
	}
}