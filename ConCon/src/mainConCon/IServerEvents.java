package mainConCon;

import java.util.EventListener;


public interface IServerEvents extends EventListener
{
	public abstract void DatosIn(ServerEvents ev,String Datos,int id);
	public abstract void DatosLineIn(ServerEvents ev,String Datos,int id);
	public abstract void NuevaConexion(ServerEvents ev,String IP,int id);
	public abstract void Error(ServerEvents ev, String mensaje,int id);
	public abstract void FinConexion(ServerEvents ev,String datos,int id);
	public abstract void MaxConnections(ServerEvents ev,String datos,int id);
	//crear evento de maximo de conexiones
}
