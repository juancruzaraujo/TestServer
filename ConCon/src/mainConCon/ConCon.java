package mainConCon;

import java.util.ArrayList;
import java.util.ListIterator;

public class ConCon 
{
	public final static int C_MODO_SERVIDOR = 0;
	public final static int C_MODO_CLIENTE = 1;
	
	public final static int C_MODO_TCP = 0;
	public final static int C_MODO_UDP = 1;
	
	private int _modo;
	private int _modoCon;
	private Server sv;
	
	private int _maxCon;
	private int _cantCon;
	
	private ArrayList<IConConEvents> _conconListeners;
	
	private void ManejadorEventos(Integer nEvento,String datos,int indice)
    {
		
    	ListIterator li = _conconListeners.listIterator();
    	
    	while (li.hasNext()) 
        {
    		IConConEvents listener = (IConConEvents) li.next();
    		ConConEvents readerEvObj = new ConConEvents(this, this);
    		
            
            if (nEvento == Server.C_EV_SERVER_ERROR)
            {
            	listener.Error(readerEvObj, datos, indice);
            }
            
            if (nEvento == Server.C_EV_SERVER_DATOS_IN)
            {
            	
            	listener.DatosIn(readerEvObj, datos, indice);
            }
            
            if (nEvento == Server.C_EV_SERVER_ERROR)
            {
            	listener.Error(readerEvObj, datos, indice);
            }
            
            if (nEvento == Server.C_EV_SERVER_NUEVA_CONEXION)
            {
            	listener.NuevaConexion(readerEvObj, datos, indice);
            }
            
            if (nEvento == Server.C_EV_SERVER_DATOS_LINE_IN)
            {
            	listener.DatosLineIn(readerEvObj, datos, indice);
            }
            
            if (nEvento == Server.C_EV_SERVER_FIN_CONEXION)
            {
            	listener.FinConexion(readerEvObj, datos, indice);
            }	
            
            if (nEvento == Server.C_EV_SERVER_MAX_CON)
            {
            	listener.MaxConnections(readerEvObj, datos, indice);
            }
        }
    	
    }
	
	private IServerEvents _iServerEventHandler = new IServerEvents() 
	{
		
		@Override
		public void NuevaConexion(ServerEvents ev, String IP, int indice) 
		{
			//TriggerServerNuevaConexion(IP, indice);
			ManejadorEventos(Server.C_EV_SERVER_NUEVA_CONEXION,IP, indice);
		}
		
		@Override
		public void FinConexion(ServerEvents ev, String datos, int indice) 
		{
			//TriggerSeverFinConexion(datos, indice);
			ManejadorEventos(Server.C_EV_SERVER_FIN_CONEXION, datos, indice);
		}
		
		@Override
		public void Error(ServerEvents ev, String mensaje, int indice) 
		{
			//TriggerServerError(mensaje, indice);
			ManejadorEventos(Server.C_EV_JSOCKET_ERROR,mensaje, indice);
		}
		
		@Override
		public void DatosIn(ServerEvents ev, String datos, int indice) 
		{
			//TriggerServerDatosIn(datos, indice);
			ManejadorEventos(Server.C_EV_SERVER_DATOS_IN, datos, indice);
		}

		@Override
		public void DatosLineIn(ServerEvents ev, String datos, int indice) 
		{
			//TriggerServerDatosLineIn(datos,indice);
			ManejadorEventos(Server.C_EV_SERVER_DATOS_LINE_IN, datos, indice);
			
		}

		@Override
		public void MaxConnections(ServerEvents ev, String datos, int indice) 
		{
			ManejadorEventos(Server.C_EV_SERVER_MAX_CON, datos, indice);
		}
	};
	
	public ConCon(int modo)
	{
		_modo = modo;
		_conconListeners = new ArrayList<>();
	}
	
	public void AddEventListener(IConConEvents listener)
    {
		_conconListeners.add(listener);
    }
	
	public void StartServer(int port,int maxCon,int modoCon)
	{
		if (_modo != C_MODO_SERVIDOR)
		{
			return;
		}
		_modoCon = modoCon;
		sv = new Server(port, maxCon,_modoCon);
		sv.AddEventListener(_iServerEventHandler);
		sv.StartServer();
	}
	
	public void SendAll(String mensaje)
	{
		sv.EnviarDatosATodos(mensaje);
	}
	
	public void Send(int index, String mensaje)
	{
		_cantCon = sv.GetCantCon();
		
		sv.EnviarDatos(index, mensaje);
	}
	
	public void SetEndOfLineChars(String endOfLine)
	{
		if (_modo == C_MODO_SERVIDOR)
		{
			sv.SetEndOfLine(endOfLine);
		}
		else
		{
			//implemetar
		}
	}
	
	public void DesconectarCliente(int id)
	{
		//System.out.println("desconecamos a " + indice);
		sv.DesconectarCliente(id);
	}
	
	public void DesconectarTodos(String mensaje)
	{
		sv.DesconectarTodos(mensaje);
	}
	
	public int GetStatus()
	{
		int status=-1;
		
		if (_modo == C_MODO_SERVIDOR)
		{
			status =sv.GetStatus();
		}
		else
		{
			//agregar acá el cliente
			status = -1;
		}
		
		return status;
	}
}
