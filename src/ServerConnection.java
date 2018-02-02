import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection extends Thread
{
    Socket s=null;
    ServerSocket ss2=null;
    public ServerConnection()
    {
        System.out.println("What is my address?");
        //Scanner input2 = new Scanner(System.in);
        //SK_Node.myPort = input2.nextInt();
        //SK_Node.myPort = Integer.parseInt(SK_Node.get());
        SK_Node.myAddress = SK_Node.get();
        String[] address = SK_Node.myAddress.split(":");
        SK_Node.myIP = address[0];
        SK_Node.myPort = Integer.parseInt(address[1]);
        
        try
        {
            ss2 = new ServerSocket(SK_Node.myPort); // can also use static final PORT_NUM , when defined
            System.out.println("Server socket opened at port number: "+SK_Node.myPort);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("Server error");
        }
    }

    public void run() 
    {
       while(true) 
       {    
            try 
            {
                s = ss2.accept();
                System.out.println("Connection established");
                SKThread st = new SKThread(s);
                st.start();
            } 
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println("Error on accepting a connection");
            }
       }
    }
}