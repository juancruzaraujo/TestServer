package mainTestServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import mainConCon.ConCon;
import mainConCon.ConConEvents;
import mainConCon.IConConEvents;
import mainConCon.IServerClientHandler;
import mainConCon.ServerClientHandlerEvents;

public class Main 
{

	private static ConCon _objConCon;
	private static boolean  _leerCom;
	private static int _serverStatus;
	
	private static ConCon _objConConUDP;
	
	//public final static int C_MODO_SERVIDOR = 0;
	private final static int C_MAX_CON = 2;
	private final static int C_PORT = 1492;
	
	
	//cambiar en el server indice por ID, ver que no llegue al maximo de ID si no que vaya renovando los viejos
		//hacer el server para UDP
	
	private static IConConEvents _conConHandler = new IConConEvents() 
	{
		
		@Override
		public void NuevaConexion(ConConEvents ev, String IP, int id) 
		{
			Msg("[ " + id + " ] Nueva Conexión" + IP);
		}
		
		@Override
		public void FinConexion(ConConEvents ev, String Datos, int id) 
		{
			Msg("[ " + id + " ] fin conexión ");
			
		}
		
		@Override
		public void Error(ConConEvents ev, String mensaje, int id) 
		{
			Msg("ERROR en conexión " + id + " " + mensaje);
		}
		
		@Override
		public void DatosIn(ConConEvents ev, String datos, int id) 
		{
			//System.out.println(datos);
		}

		@Override
		public void DatosLineIn(ConConEvents ev, String datos, int id) 
		{
			Msg("[ " + id + " ] " + datos);
			
			if (datos.equals("fin\r\n"))
			{
				_objConCon.DesconectarCliente(id);
			}
			
			if (datos.equals("all\r\n"))
			{
				_objConCon.DesconectarTodos("desconecto a todos porque se me canta\r\n");
			}
			
			if (datos.equals("end\r\n"))
			{
				System.exit(0);
			}
		}

		@Override
		public void MaxConnections(ConConEvents ev, String datos, int id) 
		{
			Msg("máximo de conexiones");
			Msg("MATAR A TODOS!");
			
		}
	};
	
	public static void main(String[] args) 
	{
		_leerCom = true;
		BufferedReader br = null;
		
		try
        {
			
			Msg("TEST SERVER JAVA SOCKETS");
			
			Msg("Iniciado TCP");
			_objConCon = new ConCon(ConCon.C_MODO_SERVIDOR);
			_objConCon.AddEventListener(_conConHandler);
			_objConCon.StartServer(C_PORT, C_MAX_CON,ConCon.C_MODO_TCP);
			
			Msg("Iniciado UDP");
			_objConConUDP = new ConCon(ConCon.C_MODO_SERVIDOR);
			_objConConUDP.AddEventListener(_conConHandler);
			_objConConUDP.StartServer(C_PORT, C_MAX_CON,ConCon.C_MODO_UDP);
			
			Msg("en puerto " + C_PORT);
			br = new BufferedReader(new InputStreamReader(System.in));
			
			Msg("escribi algo");
			while(_leerCom)
			{
				System.out.print("=>");
				String texto = br.readLine();
				//objConCon.SendAll(texto);
				//objConCon.Send(0, texto);
				_objConCon.SendAll(texto);
				Msg(texto);
			}
			
        }
		catch(Exception err)
        {
        	//System.out.println(err.getMessage());
			Msg(err.getMessage());
        }
		
	}
	
	private static void Msg(String msg)
	{
		System.out.println(msg);
	}

}
