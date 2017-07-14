package general;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;

public class UserTest
{
	User user;
	
	@Before
	public void initializeUser()
	{
		user = new User("Toshko", "password");
	}
	
	@Test
	public void testEquals()
	{
		assertEquals(true, user.equals(new User("Toshko", "parola")));
	}
	
	@Test
	public void testHashCode()
	{
		assertEquals(user.hashCode(), new User("Toshko", "parola").hashCode());
	}
	
	@Test
	public void testAuthenticate()
	{
		assertEquals(true, user.authenticate(new User("Toshko", "password")));
		assertEquals(false, user.authenticate(new User("Toshko", "parola")));
	}
	
	@Test
	public void testToString()
	{
		assertEquals("Toshko null", user.toString());
	}
	
	@Test
	public void testReadWriteObject()
	{
		User usr = null;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(user);
			oos.close();
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream input = new ObjectInputStream(bais);
			usr = (User) input.readObject();
			input.close();
		}
		catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		assertEquals(true, user.authenticate(usr));
	}
}
