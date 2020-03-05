package mainConCon;

import java.util.EventListener;

public interface IServerClientHandler extends EventListener
{
	public abstract void DatosIn(ServerClientHandlerEvents ev, String datos,int id);
	public abstract void DatosLineIn(ServerClientHandlerEvents ev, String datos,int id);
	public abstract void Error(ServerClientHandlerEvents ev, String error,int id);
	public abstract void FinCon(ServerClientHandlerEvents ev,String aux,int id);
}
