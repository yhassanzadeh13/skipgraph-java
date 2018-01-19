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
    public static String lookup[][] = new String[4][2]; 
    public static int myPort = 0;
    public static Scanner input = new Scanner(System.in);
    public static String response;
    public static int size = 4;
    public static String introducer = "2224";
    public static void main(String args[]) throws IOException
    {
          ServerConnection SS = new ServerConnection();
          
          LookupInit();
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
      * Initializes the lookup table for the current node.
      */
     public static void LookupInit()
     {
         for(int i = 0 ; i < 4 ; i++)
             for(int j = 0 ; j < 2 ; j++)
                 lookup[i][j] = null;
     }
     /**
      * Prints the lookup table for the current node.
      */
     public static void PrintLookup()
     {
         System.out.println("\n");
         for(int i = 3 ; i >= 0 ; i--)
         {
             for(int j = 0 ; j<2 ; j++)
                 System.out.print(lookup[i][j]+"\t");
             System.out.println("\n");
         }
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
     
     public static void Insert()
     {
        try 
        {
           
            String Left = null;
            String Right= null;
            
//            System.out.println("Who is before?");
//            String yourPort = get();
              String yourPort = sendTo("search_"+Num_ID, introducer , "localhost");
              System.out.println("my before is " + yourPort);
//            Scanner input3 = new Scanner(System.in);
//            yourPort = input3.nextLine();
//           input3.close();
            
//            sendTo("set_0_R_"+myPort, yourPort, "localhost");
//            sendTo("set_0_L_221", yourPort, "localhost");
//            sendTo("lookup_0_R_", yourPort, "localhost");
//            sendTo("lookup_0_L_", yourPort, "localhost");
            
            Left = yourPort;
            Right = sendTo("lookup_0_R", yourPort, "localhost");
            
            lookup[0][0] = yourPort;
            sendTo("set_0_R_"+myPort, yourPort, "localhost");
            
            lookup[0][1] = Right;
            if(Right != null)
                sendTo("set_0_L_"+myPort, Right, "localhost");
            
            int level = 0;
            while(true)
            {
                
               while(true)
                {
                    if(Left != null)
                        if(commonBits(sendTo("name", Left, "localhost")) <= level)
                            Left  = sendTo("lookup_"+level+"_L", Left, "localhost");
                        else 
                            break;
                            
                    if(Right != null)
                        if(commonBits(sendTo("name", Right, "localhost")) <=level)
                         Right = sendTo("lookup_"+level+"_R", Right, "localhost");
                        else 
                            break;
                    if(Right == null && Left == null)
                        break;
                }
                
                if(Left!= null)
                {
                    if(commonBits(sendTo("name", Left, "localhost")) > level )
                    {
                        String RightNeighbor = null;
                        RightNeighbor = sendTo("lookup_"+(level+1)+"_R", Left, "localhost");
                        
                        sendTo("set_"+(level+1)+"_R_"+myPort, Left, "localhost");
                        if(RightNeighbor != null)
                            sendTo("set_"+(level+1)+"_L_"+myPort, RightNeighbor, "localhost");
                        
                        lookup[level+1][0] = Left;
                        lookup[level+1][1] = RightNeighbor;
                        Right = RightNeighbor;
                    }
                }
                
                else if(Right != null)
                {
                    if(commonBits(sendTo("name", Right, "localhost")) > level)
                    {
                        String LeftNeighbor = null;
                        LeftNeighbor = sendTo("lookup_"+(level+1)+"_L", Right, "localhost");
                        
                        sendTo("set_"+(level+1)+"_L_"+myPort, Right, "localhost");
                        if(LeftNeighbor != null)
                            sendTo("set_"+(level+1)+"_R_"+myPort, LeftNeighbor, "localhost");
                        
                        lookup[level+1][0] = LeftNeighbor;
                        lookup[level+1][1] = Right;
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
      * Searches the graph for a given name ID.
      * 
      * @param name name ID of the node to be searched in the graph
      * 
      * @return if found, returns the name of the node. else, returns the string "nothing found"
      */
     public static String SearchByNameID(String name)
     {
        try 
        {
           
            String Left = null;
            String Right= null;
            int level = 0;

                      
            Left  = lookup[0][0];
            Right = lookup[0][1];
            
            if(commonBits(Name_ID,name) > level)
            {
                 level = commonBits(Name_ID , name); 
                 Left  = lookup[level][0];
                 Right = lookup[level][1]; 
            }
            
               while(true)
                {
                    if(Left != null)
                    {
                        if(sendTo("name", Left, "localhost").contains(name))
                            return Left;
                        else if(commonBits(sendTo("name", Left, "localhost"),name) <= level)
                            Left  = sendTo("lookup_"+level+"_L", Left, "localhost");
                        else if(commonBits(sendTo("name", Left, "localhost"),name) > level)
                        {
                            level = commonBits(sendTo("name", Left, "localhost"),name);
                            Right = sendTo("lookup_"+level+"_R", Left, "localhost");
                            Left  = sendTo("lookup_"+level+"_L", Left, "localhost");
                            continue;
                        }
                    }        
                    else if(Right != null)
                    {
                        if(sendTo("name", Right, "localhost").contains(name))
                            return Right;
                        else if(commonBits(sendTo("name", Right, "localhost"),name) <=level)
                         Right = sendTo("lookup_"+level+"_R", Right, "localhost");
                        else if(commonBits(sendTo("name", Right, "localhost"),name) > level)
                        {
                            level = commonBits(sendTo("name", Right, "localhost"),name);
                            Right = sendTo("lookup_"+level+"_R", Right, "localhost");
                            Left  = sendTo("lookup_"+level+"_L", Right, "localhost");  
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
         
        return "nothing found"; 
         
    }     
     /**
      * Searches the graph for a given number ID.
      * 
      * @param num number ID of the node to be searched in the graph
      * 
      * @return if found, returns the node. else, returns null
      */
     public static String SearchByNumID(String num)
     {
        int level = 3; 
         
        if(lookup[0][0] == null && lookup[0][1] == null) // if only the introducer exists
        {
            return Integer.toString(myPort);
        }
         
        
        else if(Integer.parseInt(Num_ID) < Integer.parseInt(num))
        {
            
         
         
         String next = null;
         while(level > 0 && lookup[level][1] == null)
             level = level - 1;
         if(level >= 0)
         {
             next = lookup[level][1];
             while(level>=0)
             {
                 try
                 {
        
                     if(sendTo("lookup_"+level+"_R", next, "localhost") != null) 
                     {
                         if(Integer.parseInt(sendTo("num", sendTo("lookup_"+level+"_R", next, "localhost"), "localhost")) <= Integer.parseInt(num))
                         {
                             next = sendTo("lookup_"+level+"_R", next, "localhost");
                             if(Integer.parseInt(sendTo("num", next, "localhost")) == Integer.parseInt(num))
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
         while(level > 0 && lookup[level][0] == null)
             level = level - 1;
         if(level >= 0)
         {
             next = lookup[level][0];
             while(level>=0)
             {
                 try
                 {
        
                     if(sendTo("lookup_"+level+"_L", next, "localhost") != null) 
                     {
                         if(Integer.parseInt(sendTo("num", sendTo("lookup_"+level+"_L", next, "localhost"), "localhost")) >= Integer.parseInt(num))
                         {
                             next = sendTo("lookup_"+level+"_L", next, "localhost");
                             if(Integer.parseInt(sendTo("num", next, "localhost")) == Integer.parseInt(num))
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
                 PrintLookup();
                 break;
         }
         
     }
 }



