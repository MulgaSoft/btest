package checkerboard;

import lib.G;
import online.common.CellId;
import online.game.BaseBoard.BoardState;
import online.game.Play2Constants;

public interface CheckerConstants extends Play2Constants
{	static String VictoryCondition = "do what it takes to win";
	static String CheckerStrings[] =
	{	"Checkers",
		VictoryCondition
	};
	static String CheckerStringPairs[][] = 
	{   {"Checkers_family","Checkers"},
		{"Checkers_variation","Standard Checkers"},
	};
	
	static enum Variation
	{
		Checkers_10("checkers-10",10),
		Checkers_8("checkers-8",8),
		Checkers_6("checkers-6",6);
		int size;
		String name;
		Variation(String n,int sz) {name = n;  size = sz; }
		static Variation findVariation(String n)
    	{
    		if(n!=null)
    		{	String nl = n.toLowerCase();
    			for(Variation s : values()) { if(s.name.equals(nl)) { return(s); }}
    		}
    		return(null);
    	}
	};
	
	enum CheckerId implements CellId
	{
    //	these next must be unique integers in the dictionary
    	Black_Chip_Pool("B"), // positive numbers are trackable
    	White_Chip_Pool("W"),
        BoardLocation(null),
        LiftRect(null),
        ReverseViewButton(null),
  	;
    	String shortName = name();
    	CheckerId(String sn) { if(sn!=null) { shortName = sn; }}
    	static public CheckerId find(String s)
    	{	String sl = s.toLowerCase();
    		for(CheckerId v : values()) { if(sl.equals(v.shortName.toLowerCase())) { return(v); }}
    		return(null);
    	}
    	static public CheckerId get(String s)
    	{	CheckerId v = find(s);
    		G.Assert(v!=null,"Id %s not found",s);
    		return(v);
    	}
     	
	}
    static final int White_Chip_Index = 0;
    static final int Black_Chip_Index = 1;
    static final CheckerId RackLocation[] = { CheckerId.White_Chip_Pool,CheckerId.Black_Chip_Pool};
    
    
    public enum CheckerState implements BoardState
    {	Puzzle(PuzzleStateDescription),
    	Draw(DrawStateDescription),
    	Resign( ResignStateDescription),
    	Gameover(GameOverStateDescription),
    	Confirm(ConfirmStateDescription),
    	Play("Place a checker on the board, or move a checker");
    	
    	String description;
    	CheckerState(String des)
    	{	description = des;
    	}
    	public String getDescription() { return(description); }
    	public boolean GameOver() { return(this==Gameover); }
    	public boolean Puzzle() { return(this==Puzzle); }
    }


	
    static final String Checker_SGF = "Checker"; // sgf game number allocated for hex
    static final String[] GRIDSTYLE = { "1", null, "A" }; // left and bottom numbers

 
    // file names for jpeg images and masks
    static final String ImageDir = "checkerboard/images/";
	// sounds
    static final int BACKGROUND_TILE_INDEX = 0;
    static final int BACKGROUND_REVIEW_INDEX = 1;
    static final int LIFT_ICON_INDEX = 2;
    static final String TextureNames[] = 
    	{ "background-tile" ,
    	  "background-review-tile",
    	  "lift-icon"};

}