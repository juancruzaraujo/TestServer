package mainConCon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;


public class ServerClientHandler extends Thread 
{
 
    final DataInputStream dis; //cambiar a privado
    final DataOutputStream dos; //cambiar a privado
    
    Socket s;
    private int _Id;
    private ArrayList<IServerClientHandler> _listeners;
    private String _datos;
    private String _salto;
    
    public ServerClientHandler(Socket s, DataInputStream dis, DataOutputStream dos,int indice)
    //public ServerClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
    	this.s = s; 
        this.dis = dis; 
        this.dos = dos; 
        _Id = indice;
        //_indice = -1;
        
        _salto = "\r\n";
        _datos = "";
        
    	_listeners = new ArrayList();
    }
        
    void AddEventListener(IServerClientHandler listener)
    {
    	_listeners.add(listener);
    }
    
    private void TriggerDatosIn(String datos,int id)
    {
    	ManejadorEventos(Server.C_EV_SERVER_DATOS_IN, datos, id);
    }
    
    private void TriggerError(String mensaje,int id)
    {
    	ManejadorEventos(Server.C_EV_JSOCKET_ERROR, mensaje, id);
    }
    
    private void TriggerFinConexion(String mensaje,int id)
    {
    	ManejadorEventos(Server.C_EV_SERVER_FIN_CONEXION, mensaje, id);
    }
    
    void SetSaltoLinea(String salto)
    {
    	_salto = salto;
    }
    
    private void ManejadorEventos(Integer nEvento,String datos,int id)
    {
    	ListIterator li = _listeners.listIterator();
    	
    	while (li.hasNext()) 
        {
    		IServerClientHandler listener = (IServerClientHandler) li.next();
    		ServerClientHandlerEvents readerEvObj = new ServerClientHandlerEvents(this, this);
            
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
            
            if (nEvento == Server.C_EV_SERVER_DATOS_LINE_IN)
            {
            	listener.DatosLineIn(readerEvObj, datos, id);
            }
            
            if (nEvento == Server.C_EV_SERVER_FIN_CONEXION)
            {
            	listener.FinCon(readerEvObj, datos, id);
            }
        }
    	
    }
    
    void SetId(int id)
    {
    	_Id = id;
    }
    
    int GetId()
    {
    	return _Id;
    }
    
    void Enviar(String mensaje)
    {
    	try
    	{
    		dos.writeUTF(mensaje);
    	}
    	catch (IOException e) 
        { 
            //e.printStackTrace();
    		TriggerError(e.getMessage(), _Id);
        } 
    }
    
    void Desconectar()
    {
    	try
    	{
    		this.dis.close(); 
    		this.dos.close(); 
    		TriggerFinConexion("", _Id);
    	}
    	catch (IOException e) 
        { 
            //e.printStackTrace();
        	TriggerError(e.getMessage(), _Id);
        } 
    	
    }
    
	@Override
    public void run()  
    { 
        String received=""; 
        String toreturn; 
        //String aux="";
        
        while (true)  
        { 
            try 
            { 
            	//dos.writeUTF("test");
                //received = dis.readUTF();
                
                //TriggerDatosIn(received, _indice);
        		//received = "";
                
                
                int byteRead;
                while ((byteRead = dis.read()) != -1) 
	            {
	            	char res =(char)byteRead;
	            	String str = String.valueOf(res); 
	            	received = received + str;
	            	
            		TriggerDatosIn(received, _Id);
            		DatosLineIn(received);
            		received = "";
	            	
	            }
                
                this.dis.close(); 
                this.dos.close(); 
                TriggerFinConexion("", _Id);
                
                
            }
            catch (IOException e) 
            { 
                //e.printStackTrace();
            	TriggerError(e.getMessage(), _Id);
            } 
        } 
        
    }
	
	private void DatosLineIn(String datos)
	{
		_datos = _datos + datos;
		
		
		if (_datos.contains(_salto))
		{
			ManejadorEventos(Server.C_EV_SERVER_DATOS_LINE_IN, _datos, _Id);
			_datos = "";
		}
	}
}
