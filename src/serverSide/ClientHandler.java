package serverSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import general.Chat;
import general.Commands;
import general.User;

public class ClientHandler implements Runnable
{
	private User			client;
	private Socket			clientCommandsSocket;
	private Socket			clientMessagesSocket;
	private Server			mainServer;
	private PrintWriter		toClient;
	private BufferedReader	fromClient;
	
	public ClientHandler(Socket commandSocket, Socket messageSocket, Server server)
	{
		clientCommandsSocket = commandSocket;
		clientMessagesSocket = messageSocket;
		mainServer = server;
	}
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("Client handler for " + clientCommandsSocket.getLocalSocketAddress());
		while (toClient == null || fromClient == null)
		{
			try
			{
				toClient = new PrintWriter(clientCommandsSocket.getOutputStream(), true);
				fromClient = new BufferedReader(new InputStreamReader(clientCommandsSocket.getInputStream()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		String input;
		String command = "";
		String argument = "";
		while (!command.equals(Commands.quit))
		{
			try
			{
				input = fromClient.readLine();
				if (input == null)
				{
					System.err.println("Command is null");
					command = Commands.quit;
					// clientCommandsSocket.close();
					// break;
				}
				else
				{
					command = input.substring(0, Commands.commandLength);
					argument = input.length() > Commands.commandLength ? input.substring(Commands.commandLength + 1).trim() : "";
				}
				
				System.out.println("Recieved command from " + client + ": " + input);
				
				boolean responce;
				String[] nameAndPassword = {};
				switch (command)
				{
				case Commands.register:
					nameAndPassword = argument.split(" ");
					client = new User(nameAndPassword[0], nameAndPassword[1], clientMessagesSocket);
					responce = mainServer.registerUser(client);
					toClient.println(responce);
					break;
				case Commands.login:
					nameAndPassword = argument.split(" ");
					client = new User(nameAndPassword[0], nameAndPassword[1], clientMessagesSocket);
					responce = mainServer.loginUser(client);
					toClient.println(responce);
					break;
				case Commands.active:
					synchronized (mainServer.activeUsers)
					{
						mainServer.activeUsers.forEach(user -> toClient.println(user.getName()));
						toClient.println("end");
					}
					break;
				case Commands.whisper:
					int space = argument.indexOf((int) ' ');
					String userName = argument.substring(0, space);
					String message = argument.substring(space + 1);
					responce = mainServer.whisperUser(new User(userName, "irrelevant"), message);
					toClient.println(responce);
					break;
				case Commands.create:
					responce = mainServer.createChat(client, argument);
					toClient.println(responce);
					break;
				case Commands.delete:
					responce = mainServer.deleteChat(client, argument);
					toClient.println(responce);
					break;
				case Commands.chats:
					synchronized (mainServer.chats)
					{
						mainServer.chats.forEach(chat -> toClient.println(chat.getTitle()));
					}
					toClient.println("end");
					break;
				case Commands.join:
					responce = mainServer.joinChat(client, argument);
					toClient.println(responce);
					break;
				case Commands.members:
					mainServer.chatMembers(argument).forEach(user -> toClient.println(user.getName()));
					toClient.println("end");
					break;
				case Commands.exit:
					responce = mainServer.exitChat(client);
					toClient.println(responce);
					break;
				case Commands.quit:
					mainServer.disconnectUser(client);
					break;
				case Commands.message:
					Chat chat = client.getChat();
					if (chat != null)
					{
						synchronized (mainServer.chats)
						{
							chat.postMessage(client, argument);
							mainServer.saveChat(chat);
						}
						toClient.println("true");
					}
					else
					{
						toClient.println("false");
					}
					break;
				default:
					toClient.println("false");
				}
			}
			catch (IOException e)
			{
				System.err.println("Problem with communication with client " + client + ", terminating connection.");
				mainServer.disconnectUser(client);
				break;
			}
		}
		try
		{
			toClient.close();
			fromClient.close();
			clientCommandsSocket.close();
			clientMessagesSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
