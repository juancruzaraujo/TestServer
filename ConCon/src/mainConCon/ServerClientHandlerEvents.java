package mainConCon;

import java.util.EventObject;

public class ServerClientHandlerEvents extends EventObject
{
	ServerClientHandler _sch;
	
	public ServerClientHandlerEvents(Object source, ServerClientHandler sch)
	{
		super(source);
		_sch = sch;
	}
}
