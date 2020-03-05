package mainConCon;

import java.util.EventListener;

public interface IConConEvents extends EventListener
{
	public abstract void DatosIn(ConConEvents ev,String Datos,int id);
	public abstract void DatosLineIn(ConConEvents ev,String Datos,int id);
	public abstract void NuevaConexion(ConConEvents ev,String IP,int id);
	public abstract void Error(ConConEvents ev, String mensaje,int id);
	public abstract void FinConexion(ConConEvents ev,String datos,int id);
	public abstract void MaxConnections(ConConEvents ev,String datos,int id);
	
	//faltan los del cliente
}
