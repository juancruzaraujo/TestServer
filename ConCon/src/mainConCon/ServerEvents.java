package mainConCon;

import java.util.EventObject;

public class ServerEvents extends EventObject
{
	Server sv;
	
	public ServerEvents(Object source, Server server)
	{
		super(source);
		sv = server;
	}
}
