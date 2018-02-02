// echo server
//AP
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SK_Node
{
    //Parameters of Node
    public static String Name_ID = null;
    public static String Num_ID = null; //Number ID is assumed to be greater than 0 
    //public static String lookup[][] = new String[4][2]; 
    public static LookupTable lookup = new LookupTable(4,2);
    public static String myAddress = "";
    public static Scanner input = new Scanner(System.in);
    public static String response;
    public static int size = 4;
    public static String introducer = "1850";
    public static int myPort; 
    public static String myIP;
    public static void main(String args[]) throws IOException
    {
          ServerConnection SS = new ServerConnection();
          
          //LookupInit();
          setName();
          SS.start();
          
         while(true)
         {
            printMenu();
            query();
         }

     }
    /**
     * A shortcut to read user input in other methods.
     * 
     * @return input of the user
     */
     public static String get()
     {
            response = input.nextLine();
            return response;
     }
    
     /**
      * Initializes the name ID and the number ID for the incoming node.
      */
     public static void setName()
     {
        System.out.println("Please enter name ID and number Id and your inviter using comma\n example 1100,4 name id = 1100 number id = 4");
        //Scanner input1 = new Scanner(System.in);
        //Name_ID = input1.nextLine();
        String split[] = null;
        split = get().split(",");
        Name_ID = split[0];
        Num_ID  = split[1];
        System.out.println("Ok! the name"+ Name_ID + " and Number " + Num_ID +" Got accepted!");
        //input.close();
     }
     
     /**
      * Prints the main menu for the user to choose the next action.
      * 1: Insertion of a new node.
      * 2: Search the graph by a name ID
      * 3: Search the graph by a number ID
      * 4: Print current node's lookup table
      */
     public static void printMenu() throws IOException
     {
         InetAddress address=InetAddress.getLocalHost();
         System.out.println("SkipGraph Implementation Project\n Yahya Hassanzadeh \t Ata Aydin Uslu \t Pinar Topcam");
         System.out.println("Node at the address: "+ address.toString());
         System.out.println("Name ID: "+Name_ID +" Number ID: " + Num_ID);
         System.out.println("Choose a query by entering it's code and then press Enter:\n"
                 + "1-Insert\n2-Search By Name ID\n3-Search By Number ID\n4-Print the Lookup Table\n"); 
     }
     
     
     
     /**
      * Sends a message to a node.
      * 
      * @param message the message that is wanted to be sent
      * @param port port of the receiver node
      * @param Address address of the receiver node
      * 
      * @return the response of the receiver node
      */
     public static String sendTo(String message, String port, String Address) throws IOException
     {
        Socket s1=null;
        //String line=null;
        BufferedReader br=null;
        BufferedReader is=null;
        PrintWriter os=null;
        try
        {
            s1=new Socket(Address, Integer.parseInt(port.toString())); // You can use static final constant PORT_NUM
            br= new BufferedReader(new InputStreamReader(System.in));
            is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
            os= new PrintWriter(s1.getOutputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.print("IO Exception");
        }

        System.out.println("Connection Stablishet to : "+port);
        
        String response=null;
        if(message!=null)
        {
            os.println(message);
            os.flush();
            System.out.println("Message "+ message + " was sent to" + port + Address);
            response=is.readLine();
            if(response.contains("null"))
                response = null;
            System.out.println("Node "+port +"+response: "+response);
            //line=br.readLine();
        }
        
        return response;
     }
     
     /**
      * Counts the number of common bits between the input node and the current node, starting with the most significant bit.
      * 
      * @param name name of the node to be compared with the current node's name
      * 
      * @return the number of common bits between the input node and the current node
      */
     public static int commonBits(String name)
     {
         if(name.length() != Name_ID.length())
             return -1;
         else 
         {
            int i = 0;
            for( i = 0 ; i < Name_ID.length() && Name_ID.charAt(i) == name.charAt(i) ; i++);
            System.out.println("Commonbits "+name+" and "+Name_ID+" is equal to "+ i);
            return i;
         }
           
     }
     /**
      * Counts the number of common bits between two input nodes, starting with the most significant bit.
      * 
      * @param name1 first node to be used in the comparison
      * @param name2 second node to be used in the comparison
      * 
      * @return the number of common bits between the input nodes
      */
     public static int commonBits(String name1,String name2)
     {
         if(name1.length() != name2.length())
             return -1;
         else 
         {
            int i = 0;
            for( i = 0 ; i < name1.length() && name1.charAt(i) == name2.charAt(i) ; i++);
            System.out.println("Commonbits "+name1+" and "+name2+" is equal to "+ i);
            return i;
         }
           
     }   
     
     
     /**
      * Inserts the current node to the network. In other words,
      * updates the lookup tables of the current node and of its neighbors.
      */
     
     public static void Insert()
     {
        try 
        {
           
            String Left = null;
            String Right= null;
            
//            System.out.println("Who is before?");
//            String yourPort = get();
              String yourAddress = sendTo("search_"+Num_ID, introducer , "192.168.2.162");
              System.out.println(yourAddress);
              String[] address = yourAddress.split(":");
              String yourPort = address[1];
              String yourIP = address[0];
              System.out.println("my before is " + yourPort);
//            Scanner input3 = new Scanner(System.in);
//            yourPort = input3.nextLine();
//           input3.close();
            
//            sendTo("set_0_R_"+myPort, yourPort, "192.168.2.162");
//            sendTo("set_0_L_221", yourPort, "192.168.2.162");
//            sendTo("lookup_0_R_", yourPort, "192.168.2.162");
//            sendTo("lookup_0_L_", yourPort, "192.168.2.162");
            
            //Left = yourPort;
              Left = yourAddress;
            Right = sendTo("lookup_0_R", yourPort, yourIP);
            
            //lookup[0][0] = yourPort;
            lookup.LookupUpdate(0,0,yourAddress);
            sendTo("set_0_R_"+myAddress, yourPort, yourIP);
            
            //lookup[0][1] = Right;
            lookup.LookupUpdate(0,1,Right);
            if(Right != null)
                sendTo("set_0_L_"+myAddress, Right.split(":")[1] , Right.split(":")[0]);
            
            int level = 0;
            while(true)
            {
                
               while(true)
                {
                    if(Left != null)
                        if(commonBits(sendTo("name", Left.split(":")[1], Left.split(":")[0])) <= level)
                            Left  = sendTo("lookup_"+level+"_L", Left.split(":")[1], Left.split(":")[0]);
                        else 
                            break;
                            
                    if(Right != null)
                        if(commonBits(sendTo("name", Right.split(":")[1], Right.split(":")[0])) <=level)
                         Right = sendTo("lookup_"+level+"_R", Right.split(":")[1], Right.split(":")[0]);
                        else 
                            break;
                    if(Right == null && Left == null)
                        break;
                }
                
                if(Left!= null)
                {
                    if(commonBits(sendTo("name", Left.split(":")[1], Left.split(":")[0])) > level )
                    {
                        String RightNeighbor = null;
                        RightNeighbor = sendTo("lookup_"+(level+1)+"_R", Left.split(":")[1], Left.split(":")[0]);
                        
                        sendTo("set_"+(level+1)+"_R_"+myAddress, Left.split(":")[1], Left.split(":")[0]);
                        if(RightNeighbor != null)
                            sendTo("set_"+(level+1)+"_L_"+myAddress, RightNeighbor.split(":")[1], RightNeighbor.split(":")[0]);
                        
                       // lookup[level+1][0] = Left;
                        lookup.LookupUpdate((level+1), 0, Left);
                       // lookup[level+1][1] = RightNeighbor;
                        lookup.LookupUpdate((level+1), 1, RightNeighbor);
                        Right = RightNeighbor;
                    }
                }
                
                else if(Right != null)
                {
                    if(commonBits(sendTo("name", Right.split(":")[1], Right.split(":")[0])) > level)
                    {
                        String LeftNeighbor = null;
                        LeftNeighbor = sendTo("lookup_"+(level+1)+"_L", Right.split(":")[1], Right.split(":")[0]);
                        
                        sendTo("set_"+(level+1)+"_L_"+myAddress, Right.split(":")[1], Right.split(":")[0]);
                        if(LeftNeighbor != null)
                            sendTo("set_"+(level+1)+"_R_"+myAddress, LeftNeighbor.split(":")[1], LeftNeighbor.split(":")[0]);
                        
                        //lookup[level+1][0] = LeftNeighbor;
                        lookup.LookupUpdate((level+1), 0, LeftNeighbor);
                        //lookup[level+1][1] = Right;
                        lookup.LookupUpdate((level+1), 1, Right);
                        Left = LeftNeighbor;
                    }
                }
                
          
                
                    
               level = level + 1;    
               if(level > 3)
                   break;
               if(Left == null && Right == null)
                   break;
            }
            
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(SK_Node.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
     /**
      * Searches the graph for the input name ID.
      * 
      * @param name name ID of the node to be searched in the graph
      * 
      * @return if found, returns the (port of the) node. 
      * Else, randomly returns a node in the level that the search has ended i.e. returns a node having maximum number of common bits with the search target, starting with the most significant (leftmost) digit.
      * If only one node exists in the graph (e.g. the node did not inserted itself into the network), current node will be returned.
      */
     public static String SearchByNameID(String name)
     {
        try 
        {
           
            String Left = null;
            String Right= null;
            int level = 0;

                      
            //Left  = lookup[0][0];
            Left = lookup.getLookupEntry(0, 0);
            //Right = lookup[0][1];
            Right = lookup.getLookupEntry(0, 1);
            
            if(commonBits(Name_ID,name) > level)
            {
                 level = commonBits(Name_ID , name); 
                 //Left  = lookup[level][0];
                 Left = lookup.getLookupEntry(level, 0);
                 //Right = lookup[level][1]; 
                 Right = lookup.getLookupEntry(level, 1);
            }
            
               while(true)
                {
                    if(Left != null)
                    {
                        if(sendTo("name", Left.split(":")[1], Left.split(":")[0]).contains(name))
                            return Left;
                        else if(commonBits(sendTo("name", Left.split(":")[1], Left.split(":")[0]),name) <= level)
                            Left  = sendTo("lookup_"+level+"_L", Left.split(":")[1], Left.split(":")[0]);
                        else if(commonBits(sendTo("name", Left.split(":")[1], Left.split(":")[0]),name) > level)
                        {
                            level = commonBits(sendTo("name", Left.split(":")[1], Left.split(":")[0]),name);
                            Right = sendTo("lookup_"+level+"_R", Left.split(":")[1], Left.split(":")[0]);
                            Left  = sendTo("lookup_"+level+"_L", Left.split(":")[1], Left.split(":")[0]);
                            continue;
                        }
                    }        
                    else if(Right != null)
                    {
                        if(sendTo("name", Right.split(":")[1], Right.split(":")[0]).contains(name))
                            return Right;
                        else if(commonBits(sendTo("name", Right.split(":")[1], Right.split(":")[0]),name) <=level)
                         Right = sendTo("lookup_"+level+"_R", Right.split(":")[1], Right.split(":")[0]);
                        else if(commonBits(sendTo("name", Right.split(":")[1], Right.split(":")[0]),name) > level)
                        {
                            level = commonBits(sendTo("name", Right.split(":")[1], Right.split(":")[0]),name);
                            Right = sendTo("lookup_"+level+"_R", Right.split(":")[1], Right.split(":")[0]);
                            Left  = sendTo("lookup_"+level+"_L", Right.split(":")[1], Right.split(":")[0]);  
                            continue;
                        }
                    }
                    if(Right == null && Left == null)
                        break;
                }
        } 
        
        catch (IOException ex) 
        {
            Logger.getLogger(SK_Node.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        //return "nothing found"; 
        return Name_ID;
         
    }     
     /**
      * Searches the graph for the input number ID.
      * 
      * @param num number ID of the node to be searched in the graph
      * 
      * @return if found, returns the (port of the) node. 
      * Else, returns the node with number ID which is the greatest number that is less than the search target.
      * If only one node exists in the graph (e.g. the node did not inserted itself into the network), current node will be returned.
      */
     public static String SearchByNumID(String num)
     {
        int level = 3; 
         
        //if(lookup[0][0] == null && lookup[0][1] == null) // if only the introducer exists
        if(lookup.getLookupEntry(0, 0) == null && lookup.getLookupEntry(0, 1) == null)
        {
            //return Integer.toString(myPort);
        	return myAddress;
        }
         
        
        else if(Integer.parseInt(Num_ID) < Integer.parseInt(num))
        {
            
         
         
         String next = null;
         //while(level > 0 && lookup[level][1] == null)
         while(level > 0 && lookup.getLookupEntry(level, 1) == null)
             level = level - 1;
         if(level >= 0)
         {
             //next = lookup[level][1];
        	 next = lookup.getLookupEntry(level, 1);
             while(level>=0)
             {
                 try
                 {
        
                     if(sendTo("lookup_"+level+"_R", next.split(":")[1], next.split(":")[0]) != null) 
                     {
                         if(Integer.parseInt(sendTo("num", sendTo("lookup_"+level+"_R", next.split(":")[1], next.split(":")[0]).split(":")[1], sendTo("lookup_"+level+"_R", next.split(":")[1], next.split(":")[0]).split(":")[0])) <= Integer.parseInt(num))
                         {
                             next = sendTo("lookup_"+level+"_R", next.split(":")[1], next.split(":")[0]);
                             if(Integer.parseInt(sendTo("num", next.split(":")[1], next.split(":")[0])) == Integer.parseInt(num))
                                                                                                return next;
                         }
                         else
                            level = level - 1;   
                     }
                     else 
                         level = level - 1;
                                 
                 } 
                 catch (IOException ex)
                 {
                     Logger.getLogger(SK_Node.class.getName()).log(Level.SEVERE, null, ex);
                 }


             }
         }
         
         return next;
        }
        
        else 
        {
         
         String next = null;
         //while(level > 0 && lookup[level][0] == null)
         while(level > 0 && lookup.getLookupEntry(level, 0) == null)
             level = level - 1;
         if(level >= 0)
         {
             //next = lookup[level][0];
        	 next = lookup.getLookupEntry(level, 0);
             while(level>=0)
             {
                 try
                 {
        
                     if(sendTo("lookup_"+level+"_L", next.split(":")[1], next.split(":")[0]) != null) 
                     {
                         if(Integer.parseInt(sendTo("num", sendTo("lookup_"+level+"_L", next.split(":")[1], next.split(":")[0]).split(":")[1], sendTo("lookup_"+level+"_L", next.split(":")[1], next.split(":")[0]).split(":")[0])) >= Integer.parseInt(num))
                         {
                             next = sendTo("lookup_"+level+"_L", next.split(":")[1], next.split(":")[0]);
                             if(Integer.parseInt(sendTo("num", next.split(":")[1], next.split(":")[0])) == Integer.parseInt(num))
                                                                                                return next;
                         }
                         else
                            level = level - 1;   
                     }
                     else 
                         level = level - 1;
                                 
                 } 
                 catch (IOException ex)
                 {
                     Logger.getLogger(SK_Node.class.getName()).log(Level.SEVERE, null, ex);
                 }


             }
         }
         
         return next;
        }
        
     }
     
             
     /**
      * Triggers the desired action of the user.
      */
     
     public static void query()
     {
        
       //Scanner input4 = new Scanner(System.in); 
       
         
         switch(get())
         {
             case"1":
               Insert();
                 break;
                 
             case"2":
                 System.out.println("Please enter the search name ID");
                 String name = get();
                 String result1 = SearchByNameID(name);
                 System.out.println("The Result of Search by Name ID for "+name+" is: "+result1);
                 break;
                 
             case"3":
                 System.out.println("Please enter the search num ID");
                 String num = get();
                 String result2 = SearchByNumID(num);
                 System.out.println("The Result of Search by number ID for "+num+" is: "+result2);
                 break;
             case"4":
                 lookup.PrintLookup();
                 break;
         }
         
     }
 }



