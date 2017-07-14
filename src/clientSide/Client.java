package clientSide;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import general.Commands;
import general.Message;
import general.User;

public class Client
{
	Scanner			scanner;
	ServerMediator	mediator;
	User			user;
	
	public Client(Scanner scnr)
	{
		scanner = scnr;
	}
	
	public void start()
	{
		connect();
		
		try
		{
			Thread monitorThread = new Thread(new ChatMonitor(mediator.getMessageSocket()));
			monitorThread.setDaemon(true);
			monitorThread.setName("Chat monitor daemon");
			monitorThread.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("Problem with communication with server, terminating connection.");
			mediator.terminate();
			return;
		}
		
		printCommands();
		interpretCommands();
	}
	
	private void connect()
	{
		while (mediator == null)
		{
			System.out.print("Server IP: ");
			String line = scanner.nextLine();
			try
			{
				mediator = new ServerMediator(line);
			}
			catch (UnknownHostException e)
			{
				System.err.println("Invalid server address.");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Connceted to server.\n");
	}
	
	private void register() throws IOException
	{
		String name;
		String password;
		if (user != null)
		{
			System.err.println("You are already connected to the server.");
			return;
		}
		else
		{
			System.out.print("Enter username: ");
			name = scanner.nextLine();
			if (name.equals("true") || name.equals("false") || name.equals("end") || name.equals("") || name.contains(" "))
			{
				System.err.println("Invalid username, please choose another one.");
				return;
			}
			
			System.out.print("Enter password: ");
			password = scanner.nextLine();
			if (password.equals("") || password.contains(" "))
			{
				System.err.println("Password must not be an empty string and must not contain space.");
				return;
			}
			
			// TODO encrypt password
			if (mediator.registerUser(name, password))
			{
				user = new User(name, password);
				System.out.println("Registered succesfully.\n");
			}
			else
			{
				System.err.println("Username already exists, please choose another one.");
			}
		}
	}
	
	private void login() throws IOException
	{
		String name;
		String password;
		if (user != null)
		{
			System.err.println("You are already connected to the server.");
			return;
		}
		else
		{
			System.out.print("Enter username: ");
			name = scanner.nextLine();
			if (name.equals("true") || name.equals("false") || name.equals("end") || name.equals("") || name.contains(" "))
			{
				System.err.println("Invalid username.");
				return;
			}
			
			System.out.print("Enter password: ");
			password = scanner.nextLine();
			if (password.equals("") || password.contains(" "))
			{
				System.err.println("Password must not be an empty string and must not contain space.");
				return;
			}
			
			// TODO encrypt password
			if (mediator.login(name, password))
			{
				user = new User(name, password);
				System.out.println("Login succesful.\n");
			}
			else
			{
				System.err.println("Incorrect username or password.");
			}
		}
	}
	
	private void printCommands()
	{
		System.out.println("Chat commands:");
		System.out.println(Commands.register + Commands.registerDescription);
		System.out.println(Commands.login + Commands.loginDescription);
		System.out.println(Commands.quit + Commands.quitDescription);
		System.out.println(Commands.active + Commands.activerDescription);
		System.out.println(Commands.whisper + Commands.whisperDescription);
		System.out.println(Commands.send + Commands.sendDescription);
		System.out.println(Commands.create + Commands.createDescription);
		System.out.println(Commands.delete + Commands.deleteDescription);
		System.out.println(Commands.join + Commands.joinDescription);
		System.out.println(Commands.exit + Commands.exitDescription);
		System.out.println(Commands.chats + Commands.chatsDescription);
		System.out.println(Commands.members + Commands.membersDescription);
		System.out.println(Commands.help + Commands.helpDescription);
		System.out.println();
	}
	
	private void interpretCommands()
	{
		String input;
		String command = "";
		String argument = "";
		while (!command.equals(Commands.quit))
		{
			input = scanner.nextLine();
			input = input.trim();
			if (input.charAt(0) == '!')
			{
				command = input.substring(0, Commands.commandLength);
				argument = input.length() > Commands.commandLength ? input.substring(Commands.commandLength + 1).trim() : "";
			}
			else
			{
				command = "";
			}
			
			if (!command.equals(Commands.register) && !command.equals(Commands.login) && !command.equals(Commands.help) && !command.equals(Commands.quit) && user == null)
			{
				System.err.println("You must login first.");
				continue;
			}
			
			try
			{
				switch (command)
				{
				case Commands.register:
					register();
					break;
				case Commands.login:
					login();
					break;
				case Commands.active:
					ArrayList<String> users = mediator.activeUsers();
					if (users.size() != 0)
					{
						for (String chatName : users)
						{
							System.out.println(chatName);
						}
					}
					else
					{
						System.out.println("The are no active users at the moment.");
					}
					break;
				case Commands.whisper:
					int space = argument.indexOf((int) ' ');
					if (!mediator.whisper(argument.substring(0, space), new Message(user, argument.substring(space + 1))))
					{
						System.err.println("User offline.");
					}
					break;
				case Commands.send:
					// TODO: Implement
					break;
				case Commands.create:
					if (argument.equals("true") || argument.equals("false") || argument.equals("end") || argument.equals("") || argument.contains(" "))
					{
						System.err.println("Invalid chat room name, please choose another one.");
						continue;
					}
					if (!mediator.createChat(argument))
					{
						System.err.println("Chat already exists.");
					}
					break;
				case Commands.delete:
					if (!mediator.delteChat(argument))
					{
						System.err.println("Chat doesn't exist, or you don't have permision to delete it.");
					}
					break;
				case Commands.chats:
					ArrayList<String> chatNames = mediator.listChats();
					if (chatNames.size() != 0)
					{
						for (String chatName : chatNames)
						{
							System.out.println(chatName);
						}
					}
					else
					{
						System.out.println("There are currently no chat rooms.");
					}
					break;
				case Commands.join:
					if (argument.equals("true") || argument.equals("false") || argument.equals("end") || argument.equals("") || argument.contains(" "))
					{
						System.err.println("Invalid chat room name, please choose another one.");
						continue;
					}
					if (!mediator.joinChat(argument))
					{
						System.err.println("You're alredy member of the chat room, or it doesn't exist.");
					}
					break;
				case Commands.members:
					ArrayList<String> members = mediator.listMembers(argument);
					if (members.size() != 0)
					{
						for (String chatName : members)
						{
							System.out.println(chatName);
						}
					}
					else
					{
						System.out.println("The chat room is empty or it doesn't exist.");
					}
					break;
				case Commands.exit:
					if (!mediator.exitChat())
					{
						System.err.println("You're not a member of a chat room.");
					}
					break;
				case Commands.quit:
					mediator.quit();
					break;
				case Commands.help:
					printCommands();
					break;
				default:
					if (!mediator.message(input))
					{
						System.err.println("Unknown command.");
					}
				}
			}
			catch (IOException e)
			{
				System.err.println("Problem with communication with server, terminating connection.");
				mediator.terminate();
				break;
			}
		}
	}
}
