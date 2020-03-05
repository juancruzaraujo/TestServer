package mainConCon;

import java.util.EventObject;

public class ConConEvents extends EventObject 
{
	ConCon _cn;
	
	public ConConEvents(Object source, ConCon cn)
	{
		super(source);
		_cn = cn;
	}
}
