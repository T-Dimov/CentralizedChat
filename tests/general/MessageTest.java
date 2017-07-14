package general;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;

public class MessageTest
{
	Message message;
	
	@Before
	public void initializeUser()
	{
		message = new Message(new User("Toshko", "password"), "test message");
	}
	
	@Test
	public void testReadWriteObject()
	{
		Message msg = null;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(message);
			oos.close();
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream input = new ObjectInputStream(bais);
			msg = (Message) input.readObject();
			input.close();
		}
		catch (IOException | ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(true, message.equals(msg));
	}
	
}
