package general;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable
{
	private static final long				serialVersionUID	= -6430853516779346834L;
	
	private static final DateTimeFormatter	formatter			= DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
	private LocalDateTime					sent;
	private User							sender;
	private String							message;
	
	public Message(User usr, String msg)
	{
		sent = LocalDateTime.now();
		sender = usr;
		message = msg;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.writeObject(sent);
		stream.writeObject(sender);
		stream.writeObject(message);
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		sent = (LocalDateTime) stream.readObject();
		sender = (User) stream.readObject();
		message = (String) stream.readObject();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (!Message.class.isAssignableFrom(obj.getClass()))
		{
			return false;
		}
		final Message other = (Message) obj;
		return sent.equals(other.sent) && sender.equals(other.sender) && message.equals(other.message);
	}
	
	@Override
	public String toString()
	{
		return "[" + sent.format(formatter) + " " + sender.getName() + "]: " + message;
	}
}
