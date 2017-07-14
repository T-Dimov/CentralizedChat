package general;

public class Commands
{
	public static final int		commandLength		= 2;
	
	public static final String	register			= "!r";
	public static final String	registerDescription	= " - start registration";
	public static final String	login				= "!l";
	public static final String	loginDescription	= " - login to server";
	
	public static final String	active				= "!a";
	public static final String	activerDescription	= " - list active users";
	public static final String	whisper				= "!w";
	public static final String	whisperDescription	= " <user> <message> - whisper <message> to <user>";
	public static final String	send				= "!s";
	public static final String	sendDescription		= " <user> <path> - send <user> the file at <path>";
	
	public static final String	create				= "!n";
	public static final String	createDescription	= " <name> - create a chat room <name>";
	public static final String	delete				= "!d";
	public static final String	deleteDescription	= " <name> - delete chat room <name>";
	public static final String	chats				= "!c";
	public static final String	chatsDescription	= " - list active chat rooms";
	public static final String	join				= "!j";
	public static final String	joinDescription		= " <name> - join chat room <name>";
	public static final String	members				= "!u";
	public static final String	membersDescription	= " <name> - list members of chat room <name>";
	public static final String	exit				= "!e";
	public static final String	exitDescription		= " - exit current chat room";
	
	public static final String	quit				= "!q";
	public static final String	quitDescription		= " - quit chat client";
	
	public static final String	help				= "!h";
	public static final String	helpDescription		= " - list commands";
	
	public static final String	message				= "!m";
}
