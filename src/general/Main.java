package general;

import java.util.Scanner;

import clientSide.Client;
import serverSide.Server;

public class Main
{
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);
		int input = -1;
		
		do
		{
			System.out.println("Choose role:\n0 - Server\n1 - Client");
			try
			{
				input = Integer.parseInt(scanner.nextLine());
			}
			catch (NumberFormatException e)
			{
				continue;
			}
		} while (input < 0 || input > 1);
		
		if (input == 0)
		{
			Thread.currentThread().setName("Server");
			new Server().start();
		}
		else
		{
			Thread.currentThread().setName("Client");
			new Client(scanner).start();
		}
		
		scanner.close();
	}
}
