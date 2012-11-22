package checkerboard;

import online.common.*;

public interface CheckerConstants extends OnlineConstants
{	
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
     /* states of the board/game.  Because several gestures are needed to complete a move, and
    there are several types of move, we use a state machine to determine what is legal */
    //static final int PUZZLE_STATE = 0; // no game, just plopping balls and removing rings at will.
    //static final int RESIGN_STATE = 1; // pending resignation, ready to confirm
    //static final int GAMEOVER_STATE = 2; // game is over (someone won or resigned)
    static final int CONFIRM_STATE = 3; // move and remove completed, ready to commit to it.
    static final int DRAW_STATE = 4;	// game is a draw, click to confirm
    static final int PLAY_STATE = 5; 	// place a marker on the board
 
    
    /* these strings correspoind to the move states */
    static String[] boardStates = 
        {
            "Rearrange things any way you want to", // puzzle state
            "Click on Done to confirm your resignation", 
            "Game Over", 
            "Click Done to confirm this move", 
            "Click on Done to end the game as a Draw",
            "Place a gobblet on the board, or move a gobblet",
            "You must move the gobblet you have picked up"
          };
    
	
    static final String Checker_SGF = "Checker"; // sgf game number allocated for hex
    static final String[] CHECKERGRIDSTYLE = { "1", null, "A" }; // left and bottom numbers

 
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