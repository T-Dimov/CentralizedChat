package serverSide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import general.Chat;
import general.User;

public class Server
{
	private ServerSocket					commandServerSocket;
	private ServerSocket					messageServerSocket;
	
	private ConcurrentHashMap<User, User>	clients;
	HashSet<Chat>							chats;
	HashSet<User>							activeUsers;
	private volatile boolean				accepting;
	private ExecutorService					executor;
	
	//////////////////////////////////////////////////// Main thread
	
	public Server()
	{
		loadRegistrations();
		loadChats();
		
		activeUsers = new HashSet<>();
		accepting = true;
		executor = Executors.newCachedThreadPool();
		
		openCommandSocket();
		openMessageSocket();
		
		System.out.println("Created server at " + commandServerSocket.getInetAddress());
	}
	
	public void start()
	{
		final Thread serverThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				accepting = false;
				try
				{
					System.out.println("Server shutdown?");
					serverThread.join();
					System.out.println("Server shutdown!");
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		});
		
		System.out.println("Started server at " + commandServerSocket.getInetAddress());
		while (accepting)
		{
			try
			{
				Socket commandSocket = commandServerSocket.accept();
				Socket messageSocket = messageServerSocket.accept();
				while (!commandSocket.getInetAddress().equals(messageSocket.getInetAddress()))
				{
					messageSocket.close();
					messageSocket = messageServerSocket.accept();
				}
				
				System.out.println("Recieved connection from " + commandSocket.getInetAddress());
				executor.submit(new ClientHandler(commandSocket, messageSocket, this));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			executor.shutdown();
			saveRegistrations();
			saveChats();
			commandServerSocket.close();
			messageServerSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Server shutdown.");
	}
	
	@SuppressWarnings("unchecked")
	private void loadRegistrations()
	{
		try
		{
			ObjectInputStream input = new ObjectInputStream(new FileInputStream("users.dat"));
			clients = (ConcurrentHashMap<User, User>) input.readObject();
			input.close();
		}
		catch (FileNotFoundException e)
		{
			clients = new ConcurrentHashMap<>();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveRegistrations()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"));
			oos.writeObject(clients);
			oos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadChats()
	{
		chats = new HashSet<>();
		try
		{
			Files.list(Paths.get("chats")).map(path -> path.toFile()).filter(file -> file.isFile() && file.getName().endsWith(".cht")).forEach(file -> loadChat(file));
		}
		catch (NoSuchFileException e)
		{
			try
			{
				Files.createDirectory(Paths.get("chats"));
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadChat(File file)
	{
		try
		{
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			Chat newChat = (Chat) input.readObject();
			input.close();
			chats.add(newChat);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveChats()
	{
		chats.forEach(chat -> saveChat(chat));
	}
	
	private void openCommandSocket()
	{
		while (commandServerSocket == null)
		{
			try
			{
				commandServerSocket = new ServerSocket(5839);
			}
			catch (BindException e)
			{
				System.err.println("Server already started.");
				commandServerSocket = null;
				return;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void openMessageSocket()
	{
		while (messageServerSocket == null)
		{
			try
			{
				messageServerSocket = new ServerSocket(5840);
			}
			catch (BindException e)
			{
				System.err.println("Server already started.");
				messageServerSocket = null;
				return;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//////////////////////////////////////////////////// Handler threads
	
	public void saveChat(Chat chat)
	{
		try
		{
			Path path = Paths.get("chats", chat.getTitle() + ".cht");
			Files.deleteIfExists(path);
			Files.createFile(path);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));
			oos.writeObject(chat);
			oos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean registerUser(User client)
	{
		boolean result = clients.putIfAbsent(client, client) == null;
		if (result)
		{
			synchronized (activeUsers)
			{
				activeUsers.add(client);
			}
			
			saveRegistrations();
		}
		return result;
	}
	
	public boolean loginUser(User client)
	{
		try
		{
			boolean result = clients.get(client).authenticate(client);
			if (result)
			{
				synchronized (activeUsers)
				{
					activeUsers.add(client);
				}
			}
			return result;
		}
		catch (NullPointerException e)
		{
			return false;
		}
	}
	
	public boolean whisperUser(User usr, String message) throws IOException
	{
		synchronized (activeUsers)
		{
			for (User user : activeUsers)
			{
				if (user.equals(usr))
				{
					PrintWriter pr = new PrintWriter(user.getMessageSocket().getOutputStream(), true);
					pr.println(message);
					return true;
				}
			}
			return false;
		}
	}
	
	public boolean createChat(User user, String chatName) throws IOException
	{
		synchronized (chats)
		{
			for (Chat chat : chats)
			{
				if (chat.getTitle().equals(chatName))
				{
					return false;
				}
			}
			Chat chat = new Chat(chatName, user, user.getMessageSocket());
			chats.add(chat);
			user.setChat(chat);
			saveChat(chat);
			return true;
		}
	}
	
	public boolean deleteChat(User user, String chatName)
	{
		synchronized (chats)
		{
			for (Chat chat : chats)
			{
				if (chat.getTitle().equals(chatName))
				{
					if (chat.canDelete(user))
					{
						chat.removeAllMembers();
						chats.remove(chat);
						Path path = Paths.get("chats", chat.getTitle() + ".cht");
						try
						{
							Files.deleteIfExists(path);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						return true;
					}
					return false;
				}
			}
			return false;
		}
	}
	
	public boolean joinChat(User user, String chatName)
	{
		synchronized (chats)
		{
			for (Chat chat : chats)
			{
				if (chat.getTitle().equals(chatName))
				{
					if (chat.isMember(user))
					{
						return false;
					}
					else
					{
						if (user.getChat() != null)
						{
							exitChat(user);
						}
						chat.addMember(user, user.getMessageSocket());
						user.setChat(chat);
						chat.updateUser(user);
						saveChat(chat);
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public Set<User> chatMembers(String chatName)
	{
		synchronized (chats)
		{
			for (Chat chat : chats)
			{
				if (chat.getTitle().equals(chatName))
				{
					return chat.getMembers();
				}
			}
			return new HashSet<User>();
		}
	}
	
	public boolean exitChat(User user)
	{
		synchronized (chats)
		{
			Chat chat = user.getChat();
			if (chat != null)
			{
				chat.removeMember(user);
				saveChat(chat);
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	public void disconnectUser(User user)
	{
		if (user != null)
		{
			exitChat(user);
			synchronized (activeUsers)
			{
				activeUsers.remove(user);
			}
		}
	}
}
