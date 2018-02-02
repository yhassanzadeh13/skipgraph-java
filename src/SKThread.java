import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SKThread extends Thread
{  

    String line=null;
    BufferedReader  is = null;
    PrintWriter os=null;
    Socket s=null;

    public SKThread(Socket s)
    {
        this.s=s;
    }

    public void run()
    {
        try
        {
            is= new BufferedReader(new InputStreamReader(s.getInputStream()));
            os=new PrintWriter(s.getOutputStream());

        }
        catch(IOException e)
        {
            System.out.println("IO error in server thread");
        }

        try 
        {
            int level = 0;
            line=is.readLine();
            String split[] = null;
            split = line.split("_"); //parses the incoming message and determines the required action by looking at the first part
            switch(split[0])
            {
                case"lookup": //lookup_level_direction
                    level = Integer.parseInt(split[1].toString());
                    if(split[2].contains("R"))
                        //os.println(SK_Node.lookup[level][1]);
                    	os.println(SK_Node.lookup.getLookupEntry(level, 1));
                    else
                        //os.println(SK_Node.lookup[level][0]);
                    	os.println(SK_Node.lookup.getLookupEntry(level, 0));
                    os.flush();
                    break;
                    
                case "search": //search for num ID search_number
                    os.println(SK_Node.SearchByNumID(split[1]));
                    os.flush();
                    System.out.println("A search by name ID was done for "+ split[1]);
                    break;
                    
                    
                case "set": //set_level_direction_address
                     level = Integer.parseInt(split[1].toString());
                    if(split[2].contains("R"))
                        //SK_Node.lookup[level][1] = split[3];
                    	SK_Node.lookup.LookupUpdate(level, 1, split[3]);
                    else
                        //SK_Node.lookup[level][0] = split[3];
                    	SK_Node.lookup.LookupUpdate(level, 0, split[3]);
                    System.out.println("Lookup table has been updated successfully");
                    SK_Node.lookup.PrintLookup();
                    os.println("Split[2] is:-"+split[2]+"-");
                    os.flush();
                    break;
                    
                case "name":
                    os.println(SK_Node.Name_ID);
                    os.flush();
                    break;
                    
                case "num":
                    os.println(SK_Node.Num_ID);
                    os.flush();
                    break;                   
                    
                    
                default:
                    os.println("Somthing is wrong");
                    os.flush();
                    break;
                    
            }
//            while(line.compareTo("QUIT")!=0)
//            {
//
//                os.println(line);
//                os.flush();
//                System.out.println("Response to Client  :  "+line);
//                line=is.readLine();
//            }   
        }
        catch (IOException e)
        {

            line=this.getName(); //reused String line for getting thread name
            System.out.println("IO Error/ Client "+line+" terminated abruptly");
        }
        catch(NullPointerException e)
        {
            line=this.getName(); //reused String line for getting thread name
            System.out.println("Client "+line+" Closed");
        }

      finally
        {    
            try
            {
                System.out.println("Connection Closing..");
                if (is!=null)
                {
                    is.close(); 
                    System.out.println(" Socket Input Stream Closed");
                }

                if(os!=null)
                {
                    os.close();
                    System.out.println("Socket Out Closed");
                }
                if (s!=null)
                {
                    s.close();
                    System.out.println("Socket Closed");
                }

            }
            catch(IOException ie)
            {
                System.out.println("Socket Close Error");
            }
        }//end finally 
    }//end of run
}//end of SKThread