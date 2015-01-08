package hex;

import lib.G;
import online.common.CellId;
import online.game.Play2Constants;
import online.game.BaseBoard.BoardState;


public interface HexConstants extends Play2Constants
{
	static String HexVictoryCondition = "connect opposite sides with a chain of markers";
	static String HexPlayState = "Place a marker on any empty cell";
	static String HexPlayOrSwapState = "Place a marker on any empty cell, or Swap Colors";
	static String HexConfirmSwapState = "Click Done to confirm swapping colors with your opponent";
	static String HexStrings[] = 
	{  "Hex","Hex-15","Hex-19",
       HexPlayState,
       HexPlayOrSwapState,
       HexConfirmSwapState,
	   HexVictoryCondition
		
	};
	static String HexStringPairs[][] = 
	{   {"Hex_family","Hex"},
		{"Hex_variation","11x11 Hex"},
		{"Hex-15_variation","15x15 Hex"},
		{"Hex-19_variation","19x19 Hex"}
	};
    //
    // states of the game
    //
	public enum HexState implements BoardState
	{
	Puzzle(PuzzleStateDescription,false,false),
	Resign(ResignStateDescription,true,false),
	Gameover(GameOverStateDescription,false,false),
	Confirm(ConfirmStateDescription,true,true),
	ConfirmSwap(HexConfirmSwapState,true,false),
	PlayOrSwap(HexPlayOrSwapState,false,false),
	Play(HexPlayState,false,false);
	HexState(String des,boolean done,boolean digest)
	{
		description = des;
		digestState = digest;
		doneState = done;
	}
	boolean doneState;
	boolean digestState;
	String description;
	public boolean GameOver() { return(this==Gameover); }
	public String description() { return(description); }
	public boolean doneState() { return(doneState); }
	public boolean digestState() { return(digestState); }
	public boolean Puzzle() { return(this==Puzzle); }
	};
	
    //	these next must be unique integers in the Hexmovespec dictionary
	//  they represent places you can click to pick up or drop a stone
	enum HexId implements CellId
	{
    	Black_Chip_Pool("B"), // positive numbers are trackable
    	White_Chip_Pool("W"),
    	BoardLocation(null),
    	EmptyBoard(null),;
    	String shortName = name();
    	HexId(String sn) { if(sn!=null) { shortName = sn; }}
    	static public HexId find(String s)
    	{	String sl = s.toLowerCase();
    		for(HexId v : values()) { if(sl.equals(v.shortName.toLowerCase())) { return(v); }}
    		return(null);
    	}
    	static public HexId get(String s)
    	{	HexId v = find(s);
    		G.Assert(v!=null,"Id %s not found",s);
    		return(v);
    	}

	}
    
    /* the "external representation for the board is A1 B2 etc.  This internal representation is X,Y
    where adjacent X's are separated by 2.  This gives the board nice mathematical properties for
    calculating adjacency and connectivity. */
 static int[] ZfirstInCol = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
 static int[] ZnInCol = { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11 }; // depth of columns, ie A has 4, B 5 etc.
 //
 // rhombix hex board 15 per side
 static int[] ZfirstInCol15 = { 14,13,12,11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
 static int[] ZnInCol15 = { 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15 }; // depth of columns, ie A has 4, B 5 etc.
 // rhombix hex board, 19 per side
 static int[] ZfirstInCol19 = { 18, 17, 16, 15, 14,13,12,11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
 static int[] ZnInCol19 =     { 19,19,19,19,19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19 }; // depth of columns, ie A has 4, B 5 etc.

 enum HexVariation
    {
    	hex("hex",ZfirstInCol,ZnInCol),
    	hex_15("hex-15",ZfirstInCol15,ZnInCol15),
    	hex_19("hex-19",ZfirstInCol19,ZnInCol19);
    	String name ;
    	int [] firstInCol;
    	int [] ZinCol;
    	// constructor
    	HexVariation(String n,int []fin,int []zin) 
    	{ name = n; 
    	  firstInCol = fin;
    	  ZinCol = zin;
    	}
    	// match the variation from an input string
    	static HexVariation findVariation(String n)
    	{
    		if(n!=null)
    		{	String nl = n.toLowerCase();
    			for(HexVariation s : values()) { if(s.name.equals(nl)) { return(s); }}
    		}
    		return(null);
    	}
     	
    }

//
// this would be a standard six sided 5-per-side hex board
//    static int[] ZfirstInCol = { 4, 3, 2, 1, 0, 1, 2, 3, 4 };
//    static int[] ZnInCol =     {5, 6, 7, 8, 9, 8, 7, 6, 5 }; // depth of columns, ie A has 4, B 5 etc.
//
// this would be a standard six sided 4-per-side hex board
//    static int[] ZfirstInCol = { 3, 2, 1, 0, 1, 2, 3 }; // these are indexes into the first ball in a column, ie B1 has index 2
//    static int[] ZnInCol = { 4, 5, 6, 7, 6, 5, 4 }; // depth of columns, ie A has 4, B 5 etc.
//
// this would be a standard yinsh board, 5-per side with the corners missing
//    static int[] ZfirstInCol = { 6, 3, 2, 1, 0, 1, 0, 1, 2, 3, 6 }; // these are indexes into the first ball in a column, ie B1 has index 2
//    static int[] ZnInCol = { 4, 7, 8, 9, 10, 9, 10, 9, 8, 7, 4 }; // depth of columns, ie A has 4, B 5 etc.
//    static int[] ZfirstCol = { 1, 0, 0, 0, 0, 1, 1, 2, 3, 4, 6 }; // number of the first visible column in this row, 
//	 standard "volo" board, 6 per side with missing corners
//    static int[] ZfirstInCol = { 8, 5, 4, 3, 2, 1, 2, 1,  2, 3, 4, 5, 8 }; // these are indexes into the first ball in a column, ie B1 has index 2
//    static int[] ZnInCol =   { 5, 8, 9, 10, 11, 12, 11, 12, 11, 10, 9, 8, 5 }; // depth of columns, ie A has 4, B 5 etc.
//    static int[] ZfirstCol = { 1, 0, 0,  0,  0,  0,  1,  0,  0,  0, 0, 0, 1 };

//  "snowflake" hex board with crinkly edges, 5 per side.  Used for "crossfire"
//    static int[] ZfirstInCol = { 6, 3, 0, 1, 0, 1, 0, 3, 6 };
//    static int[] ZnInCol =     {1, 4, 7, 6, 7, 6, 7, 4, 1 }; // depth of columns, ie A has 4, B 5 etc.


	
	// move commands, actions encoded by movespecs.  Values chosen so these
    // integers won't look quite like all the other integers
 	
    static final String Hex_SGF = "11"; // sgf game number allocated for hex
    static final String[] HEXGRIDSTYLE = { "1", null, "A" }; // left and bottom numbers

    // file names for jpeg images and masks
    static final String ImageDir = "hex/images/";

}