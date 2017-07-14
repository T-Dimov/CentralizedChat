package general;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Chat implements Serializable
{
	private static final long						serialVersionUID	= -581295915884436335L;
	
	private User									owner;
	private String									title;
	private transient HashMap<User, PrintWriter>	members;
	private ArrayList<Message>						messages;
	
	public Chat(String ttl, User usr, Socket clientMessagesSocket) throws IOException
	{
		title = ttl;
		owner = usr;
		members = new HashMap<>();
		messages = new ArrayList<>();
		
		PrintWriter toClient = new PrintWriter(clientMessagesSocket.getOutputStream(), true);
		members.put(usr, toClient);
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String ttl)
	{
		title = ttl;
	}
	
	public boolean isMember(User user)
	{
		return members.containsKey(user);
	}
	
	public void addMember(User usr, Socket clientMessagesSocket)
	{
		PrintWriter toClient = null;
		while (toClient == null)
		{
			try
			{
				toClient = new PrintWriter(clientMessagesSocket.getOutputStream(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		members.put(usr, toClient);
	}
	
	public void removeMember(User user)
	{
		user.setChat(null);
		members.remove(user);
	}
	
	public void removeAllMembers()
	{
		members.forEach((user, pw) -> user.setChat(null));
		members.clear();
	}
	
	public Set<User> getMembers()
	{
		return members.keySet();
	}
	
	public int memberCount()
	{
		return members.size();
	}
	
	public boolean canDelete(User user)
	{
		return owner.authenticate(user);
	}
	
	public void postMessage(User usr, String msg)
	{
		Message message = new Message(usr, msg);
		messages.add(message);
		// for (User user : members.keySet())
		// {
		// if (!user.equals(usr))
		// members.get(user).println(message);
		// }
		for (PrintWriter pr : members.values())
		{
			pr.println(message);
		}
	}
	
	public void updateUser(User user)
	{
		synchronized (members)
		{
			PrintWriter toUser = members.get(user);
			synchronized (messages)
			{
				messages.forEach(toUser::println);
			}
		}
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.writeObject(owner);
		stream.writeObject(title);
		stream.writeObject(messages);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		owner = (User) stream.readObject();
		title = (String) stream.readObject();
		messages = (ArrayList<Message>) stream.readObject();
		members = new HashMap<>();
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
		final Chat other = (Chat) obj;
		return title.equals(other.title);
	}
	
	@Override
	public String toString()
	{
		return title;
	}
}
