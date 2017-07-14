package clientSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import general.Commands;
import general.Message;

public class ServerMediator
{
	private Socket			commandSocket;
	private Socket			messageSocket;
	private PrintWriter		toServer;
	private BufferedReader	fromServer;
	
	public ServerMediator(String address) throws UnknownHostException, IOException
	{
		commandSocket = new Socket(InetAddress.getByName(address), 5839);
		messageSocket = new Socket(InetAddress.getByName(address), 5840);
		fromServer = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
		toServer = new PrintWriter(commandSocket.getOutputStream(), true);
	}
	
	public Socket getMessageSocket()
	{
		return messageSocket;
	}
	
	public boolean registerUser(String username, String password) throws IOException
	{
		toServer.println(Commands.register + " " + username + " " + password);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public boolean login(String username, String password) throws IOException
	{
		toServer.println(Commands.login + " " + username + " " + password);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public ArrayList<String> activeUsers() throws IOException
	{
		toServer.println(Commands.active);
		String userName;
		ArrayList<String> result = new ArrayList<>();
		while (!(userName = fromServer.readLine()).equals("end"))
		{
			result.add(userName);
		}
		return result;
	}
	
	public boolean whisper(String username, Message message) throws IOException
	{
		toServer.println(Commands.whisper + " " + username + " " + message);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public boolean sendFile(String username, String path) throws IOException
	{
		// TODO implement
		return false;
	}
	
	public boolean createChat(String chat) throws IOException
	{
		toServer.println(Commands.create + " " + chat);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public boolean delteChat(String chat) throws IOException
	{
		toServer.println(Commands.delete + " " + chat);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public boolean joinChat(String chat) throws IOException
	{
		toServer.println(Commands.join + " " + chat);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public ArrayList<String> listChats() throws IOException
	{
		toServer.println(Commands.chats);
		String chatName;
		ArrayList<String> result = new ArrayList<>();
		while (!(chatName = fromServer.readLine()).equals("end"))
		{
			result.add(chatName);
		}
		return result;
	}
	
	public boolean exitChat() throws IOException
	{
		toServer.println(Commands.exit);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public ArrayList<String> listMembers(String argument) throws IOException
	{
		toServer.println(Commands.members + " " + argument);
		String userName;
		ArrayList<String> result = new ArrayList<>();
		while (!(userName = fromServer.readLine()).equals("end"))
		{
			result.add(userName);
		}
		return result;
	}
	
	public void quit() throws IOException
	{
		toServer.println(Commands.quit);
		terminate();
	}
	
	public boolean message(String msg) throws IOException
	{
		toServer.println(Commands.message + " " + msg);
		String result = fromServer.readLine();
		return Boolean.valueOf(result);
	}
	
	public void terminate()
	{
		try
		{
			fromServer.close();
			toServer.close();
			commandSocket.close();
			messageSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
