package checkerboard;

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
	static final int DEFAULT_COLUMNS = 8;	// 8x6 board
	static final int DEFAULT_ROWS = 8;
	static final String Checker_INIT = "checkers";	//init for standard game

    //	these next must be unique integers in the dictionary
    static final int Black_Chip_Pool = 100; // positive numbers are trackable
    static final int White_Chip_Pool = 101;
    static final int White_Chip_Index = 0;
    static final int Black_Chip_Index = 1;
    static final int RackLocation[] = { White_Chip_Pool,Black_Chip_Pool};
    
    static final int BoardLocation = 102;
    static final int LiftRect = 103;
    static final int ReverseViewButton = 104;
    
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