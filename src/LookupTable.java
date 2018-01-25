
public class LookupTable {
	
	public static String table[][];
	public static int rowNum; 
	public static int colNum;
	
	public LookupTable(int r, int c){
		table = new String[r][c];
		rowNum = r;
		colNum = c;
		LookupInit();
	}
	
	/**
     * Initializes the lookup table for the current node.
     */
    public static void LookupInit()
    {
        for(int i = 0 ; i < rowNum ; i++)
            for(int j = 0 ; j < colNum ; j++)
                table[i][j] = null;
    }
    /**
     * Prints the lookup table for the current node.
     */
    public static void PrintLookup()
    {
        System.out.println("\n");
        for(int i = (rowNum-1) ; i >= 0 ; i--)
        {
            for(int j = 0 ; j<colNum ; j++)
                System.out.print(table[i][j]+"\t");
            System.out.println("\n");
        }
    }
    
    public void LookupUpdate(int row, int col, String val){
    	table[row][col] = val;
    }
    
    public String getLookupEntry(int row, int col){
    	return table[row][col];
    }
    

}
