package mainConCon;

import java.awt.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;



public class Server extends Thread
{
	
	final static int C_MODO_TCP = 0;
	final static int C_MODO_UDP = 1;
	final static int C_LEN_MSG_UDP = 65535;
	
	static int C_EV_SERVER_ERROR = 0;
    static int C_EV_SERVER_NUEVA_CONEXION = 1;
    static int C_EV_SERVER_DATOS_IN = 2;
    static int C_EV_JSOCKET_ERROR = 3;
    static int C_EV_SERVER_FIN_CONEXION = 4;
    static int C_EV_SERVER_DATOS_LINE_IN = 5;
    static int C_EV_SERVER_MAX_CON = 6;
    static int C_EV_SERVER_READY = 7;
	
    static int C_SERVER_STATUS_READY = 0;
    static int C_SERVER_STATUS_LISTENING = 1;
    static int C_SERVER_STATUS_CONNECTION_LIMIT = 2;
    static int C_SERVER_STATUS_ERROR = 3;
    
    private int _serverStatus = -1;
    private int _ServerStatusAnt;
    
    private int _port;
	
	private int _modo;
	
	private int _maxCon;	
	private int _cantCon;
	private boolean _seguirEscuchando;
	private Thread _th; //hilo del socket
	private int _indice; //va a tener el indice del thread, ojo, no es lo mismo que _cantCon
	
	private String _endOfLine;
	
	//private ServerClientHandler _thCliente[];
	private ArrayList<ServerClientHandler> _thClienteTCP;
	private ArrayList<UDPServerClientHandler> _thClienteUDP;
	
	private ArrayList<IServerEvents> _listeners;
    
	
	private ServerSocket _serverSocket;
	private DatagramSocket _udpServerSocket;
	
	//private byte[] _recUDP; 
	//private DatagramPacket DpReceive = null;
	
	//AGREGAR UN INT DE ESTADO PARA PONER
	//ESCUCHANDO, LIMITE DE CONEXIONES, ETC
	
    private void TriggerDatosIn(String datos,int id)
    {
    	ManejadorEventos(Server.C_EV_SERVER_DATOS_IN, datos, id);
    }
    
    private void TriggerDatosLineIn(String datos, int id)
    {
    	ManejadorEventos(Server.C_EV_SERVER_DATOS_LINE_IN,datos,id);
    }
    
    private void TriggerError(String mensaje,int id)
    {
    	ManejadorEventos(Server.C_EV_JSOCKET_ERROR, mensaje, id);
    }
    
    private void TriggerFinConexion(String mensaje,int id)
    {
    	ManejadorEventos(Server.C_EV_SERVER_FIN_CONEXION, mensaje, id);
    }
    
    private void TriggerNuevaConexion(String mensaje,int id)
    {
    	ManejadorEventos(Server.C_EV_SERVER_NUEVA_CONEXION, mensaje, id);
    }
    
    //acá se ejecuta lo que llega de serverclienthandler
	private IServerClientHandler _clientHandlerEventListener = new IServerClientHandler() 
	{
		
		@Override
		public void FinCon(ServerClientHandlerEvents ev, String aux,int id) 
		{
			_cantCon--;
			for (int i=0;i<_thClienteTCP.size();i++)
			{
				if (_thClienteTCP.get(i).GetId() == id)
				{
					_thClienteTCP.remove(i); //remuevo de la lista
				}
			}

			TriggerFinConexion(aux, id);
		}
		
		@Override
		public void Error(ServerClientHandlerEvents ev, String error,int id) 
		{
			TriggerError(error, id);
		}
		
		@Override
		public void DatosIn(ServerClientHandlerEvents ev, String datos,int id) 
		{
			TriggerDatosIn(datos, id);
		}

		@Override
		public void DatosLineIn(ServerClientHandlerEvents ev, String datos, int id) 
		{
			ManejadorEventos(Server.C_EV_SERVER_DATOS_LINE_IN, datos, id);
		}
	};
	
	private void ManejadorEventos(Integer nEvento,String datos,int id)
    {

		ListIterator li = _listeners.listIterator();
    	
    	while (li.hasNext()) 
        {
    		IServerEvents listener = (IServerEvents) li.next();
    		ServerEvents readerEvObj = new ServerEvents(this, this);
            
            if (nEvento == Server.C_EV_SERVER_ERROR)
            {
            	listener.Error(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_DATOS_IN)
            {
            	listener.DatosIn(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_ERROR)
            {
            	listener.Error(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_NUEVA_CONEXION)
            {
            	listener.NuevaConexion(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_DATOS_LINE_IN)
            {
            	listener.DatosLineIn(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_FIN_CONEXION)
            {
            	listener.FinConexion(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_MAX_CON)
            {
            	listener.MaxConnections(readerEvObj, datos, id);
            }
        }
    	
    }
	
	
	void AddEventListener(IServerEvents listener)
    {
    	_listeners.add(listener);
    }
	
	Server(int port,int maxCon,int modo)
	{
		_port = port;
		_maxCon = maxCon;
		_seguirEscuchando = true;
		_modo = modo;
		_endOfLine = "\r\n";
		_listeners = new ArrayList();
		
		if (_th ==null)
		{
			_th = new Thread(this,"thrServer" + _port + _modo);
			//System.out.println("thread CREADO");
		}
		
		_serverStatus = C_SERVER_STATUS_READY;
		
	}
	
	void SetEndOfLine(String endOfLine)
	{
		_endOfLine = endOfLine;
	}
	
	int GetCantCon()
	{
		return _cantCon;
	}
	
	void StartServer()
	{
		_th.start(); //ejecuta run
		_cantCon =0;
		_indice = 0;
		
	}
	
	public void run()
	{
		if (_modo==ConCon.C_MODO_TCP)
		{
			StartServerTCP();
		}
		else
		{
			
			StartServerUDP();
		}
	}
	
	private void StartServerUDP()
	{
		try
		{	
			
			DatagramPacket dpReceive=null;
			byte[] recUDP; 
			recUDP = new byte[C_LEN_MSG_UDP];
			
			boolean conOk=false;
			_udpServerSocket = new DatagramSocket(_port);
			
			_thClienteUDP = new ArrayList<>();
			
			
			
			while(true)
			{
				dpReceive = new DatagramPacket(recUDP, recUDP.length);
				_udpServerSocket.receive(dpReceive); //leo la peticion (conexion nueva?)
				
				recUDP = dpReceive.getData();
				
				String s = new String(recUDP);
				TriggerDatosIn("[ udp ]" + s, _indice);
				TriggerDatosLineIn(s, _indice);
				
				recUDP = new byte[C_LEN_MSG_UDP];
						
				
			}
			
			
		}
		catch (Exception e) 
		{
			TriggerError(e.getMessage(), _indice);
		}
		
	}
	
	private void StartServerTCP()
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(_port);
			//InstanciarSvSocket();
			//_thCliente = new ServerClientHandler[_maxCon];
			_thClienteTCP = new ArrayList<>();
			
			
			
			while(true)
			{	
				Socket socket = null;
				socket = serverSocket.accept();
				
				if (SeguirEscuchando())
				{
					////Socket socket = null;
					
					try
					{
						////socket = serverSocket.accept();
						TriggerNuevaConexion(socket.toString(), _indice); //evento de nueva conexión
						
						DataInputStream datosIn = new DataInputStream(socket.getInputStream()); 					
		                DataOutputStream datosOut = new DataOutputStream(socket.getOutputStream()); 
		                
		                ServerClientHandler aux = new ServerClientHandler(socket, datosIn, datosOut, _indice);	                
		                
		                aux.setName(Integer.toString(_indice));
		                aux.AddEventListener(_clientHandlerEventListener);
		                aux.start();
		                _thClienteTCP.add(aux);
		                
		                _cantCon++;
		                _indice++;
						
					}
					catch (Exception e) 
					{
						socket.close();
						//System.out.println(e.getMessage());
						TriggerError(e.getMessage(), _indice);
					}
				}
				else
				{
					socket.close();//con esto evitamos que haya conexiones nuevas
				}
				
			}
			
		}
		catch(Exception e)
		{
			//System.out.println(e.getMessage());
			TriggerError(e.getMessage(), _indice);
			_serverStatus = C_SERVER_STATUS_ERROR;
		}
		
	}
	
	private boolean SeguirEscuchando()
	{
		
		boolean res=false;
		
		if (_maxCon>0)
		{
			if (_cantCon < _maxCon)
			{
				res = true;
				//_ServerStatusAnt = _serverStatus;
				_serverStatus = C_SERVER_STATUS_LISTENING;
				//InstanciarSvSocket();
			}
			else
			{
				res = false;
				_ServerStatusAnt = _serverStatus;
				_serverStatus = C_SERVER_STATUS_CONNECTION_LIMIT;
				
				if (_ServerStatusAnt != _serverStatus)
				{
					ManejadorEventos(Server.C_EV_SERVER_MAX_CON, "", _maxCon);
				}
				
			}
		}
		else
		{
			res = true; //se puso para conexiones ilimitadas
			_serverStatus = C_SERVER_STATUS_LISTENING;
		}
		
		return res;
	}
		
	void EnviarDatos(int id,String datos)
	{
		try
		{
			if (_modo == ConCon.C_MODO_TCP)
			{
				//datosOut.writeUTF(datos);
				//_thCliente[indice].Enviar(datos);
				_thClienteTCP.get(id).Enviar(datos);
			}	
			if (_modo == ConCon.C_MODO_UDP)
			{
				//implementar
			}
			
		}
		catch(Exception e)
		{
			//System.out.println(e.getMessage());
			TriggerError(e.getMessage(), _indice);
			_serverStatus = C_SERVER_STATUS_ERROR;
		}
		
	}
	
	void EnviarDatosATodos(String datos)
	{
		
		for (int i=0;i<_cantCon;i++)
		{
			EnviarDatos(i,datos);
		}
	}
	
	void DesconectarCliente(int id)
	{
		//_thCliente[indice].Desconectar();
		for (int i=0;i<_thClienteTCP.size();i++)
		{
			if (_thClienteTCP.get(i).GetId() ==  id)
			{
				_thClienteTCP.get(i).Desconectar();
			}
		}
		//_thCliente.get(id).Desconectar();
		
	}
	
	void DesconectarTodos(String mensaje)
	{
		for (int i=0;i<_cantCon;i++)
		{
			EnviarDatos(i,mensaje);
			DesconectarCliente(i);
		}
	}
	
	int GetStatus()
	{
		return _serverStatus;
	}
	
	private void SetIndice()
	{
		for (int i=0;i<_thClienteTCP.size();i++)
		{
			_thClienteTCP.get(i).SetId(i);
		}
	}
}
