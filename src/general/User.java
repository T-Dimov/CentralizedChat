package general;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class User implements Serializable
{
	private static final long	serialVersionUID	= -8024407569306187613L;
	
	private String				name;
	private String				password;
	private transient Chat		currentChat;
	private transient Socket	messageSocket;
	
	public User(String username, String pass)
	{
		name = username;
		password = pass;
		currentChat = null;
		messageSocket = null;
	}
	
	public User(String username, String pass, Socket msgSock)
	{
		name = username;
		password = pass;
		currentChat = null;
		messageSocket = msgSock;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Socket getMessageSocket()
	{
		return messageSocket;
	}
	
	public boolean authenticate(User user)
	{
		return name.equals(user.name) && password.equals(user.password);
	}
	
	public void setChat(Chat chat)
	{
		currentChat = chat;
	}
	
	public Chat getChat()
	{
		return currentChat;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.writeObject(name);
		stream.writeObject(password);
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		name = (String) stream.readObject();
		password = (String) stream.readObject();
		currentChat = null;
		messageSocket = null;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (!User.class.isAssignableFrom(obj.getClass()))
		{
			return false;
		}
		final User other = (User) obj;
		return name.equals(other.name);
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
	@Override
	public String toString()
	{
		return name + " " + currentChat;
	}
}
