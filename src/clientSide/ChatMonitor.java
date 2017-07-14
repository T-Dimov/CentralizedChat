package clientSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class ChatMonitor implements Runnable
{
	private Socket			incommingMessages;
	private BufferedReader	messagesInput;
	
	public ChatMonitor(Socket socket) throws IOException
	{
		incommingMessages = socket;
		messagesInput = new BufferedReader(new InputStreamReader(incommingMessages.getInputStream()));
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				String msg = messagesInput.readLine();
				if (msg == null)
				{
					break;
				}
				System.out.println(msg);
				
			}
			catch (SocketException e)
			{
				break;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			incommingMessages.close();
			messagesInput.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
