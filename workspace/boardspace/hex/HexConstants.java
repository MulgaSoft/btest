package hex;

import online.common.*;


public interface HexConstants extends OnlineConstants
{
    //	these next must be unique integers in the Hexmovespec dictionary
	//  they represent places you can click to pick up or drop a stone
    static final int Black_Chip_Pool = 100; // positive numbers are trackable
    static final int White_Chip_Pool = 101;
    static final int BoardLocation = 102;
    static final int EmptyBoard = 103;

    // init strings for variations of the game.
    static final String Hex_5_INIT = "hex-5"; // can help debugging
    static final String Hex_9_INIT = "hex-9";
    static final String Hex_INIT = "hex"; //init for standard game
    static final String Hex_15_INIT = "hex-15";
    static final String Hex_19_INIT = "hex-19";

    /* the "external representation for the board is A1 B2 etc.  This internal representation is X,Y
       where adjacent X's are separated by 2.  This gives the board nice mathematical properties for
       calculating adjacency and connectivity. */
    static int[] ZfirstInCol5 = { 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
    static int[] ZnInCol5 = { 5, 5, 5, 5, 5 }; // depth of columns, ie A has 4, B 5 etc.
    static int[] ZfirstInCol9 = { 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
    static int[] ZnInCol9 = { 9, 9, 9, 9, 9, 9, 9, 9, 9 }; // depth of columns, ie A has 4, B 5 etc.
    static int[] ZfirstInCol = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
    static int[] ZnInCol = { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11 }; // depth of columns, ie A has 4, B 5 etc.
    //
    // rhombix hex board 15 per side
    static int[] ZfirstInCol15 = { 14,13,12,11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
    static int[] ZnInCol15 = { 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15 }; // depth of columns, ie A has 4, B 5 etc.
    // rhombix hex board, 19 per side
    static int[] ZfirstInCol19 = { 18, 17, 16, 15, 14,13,12,11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }; // these are indexes into the first ball in a column, ie B1 has index 2
    static int[] ZnInCol19 =     { 19,19,19,19,19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19 }; // depth of columns, ie A has 4, B 5 etc.
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

    
    /* states of the board/game.  Because several gestures are needed to complete a move, and
    there are several types of move, we use a state machine to determine what is legal */
    //static final int PUZZLE_STATE = 0; // no game, just plopping balls and removing rings at will.
    //static final int RESIGN_STATE = 1; // pending resignation, ready to confirm
    //static final int GAMEOVER_STATE = 2; // game is over (someone won or resigned)
    static final int CONFIRM_STATE = 3; // move and remove completed, ready to commit to it.
    static final int FIRST_PLAY_STATE = 4;	// the first move
    static final int PLAY_STATE = 5; // place a marker on the board
	static final int PLAY_OR_SWAP_STATE = 6; // place a marker or swap colors 
	static final int CONFIRM_SWAP_STATE = 7; // swapped, confirm swap move
    /* these strings correspond to the move states */
    static String[] boardStates = 
        {
            "Rearrange things any way you want to", // puzzle state
            "Click on Done to confirm your resignation", 
            "Game Over", 
            "Click Done to confirm this move", 
            "Place a marker on any empty cell",	// first play state
            "Place a marker on any empty cell",	// play state
            "Place a marker on any empty cell, or Swap Colors",
            "Click Done to confirm swapping colors with your opponent"
         };

	
	// move commands, actions encoded by movespecs.  Values chosen so these
    // integers won't look quite like all the other integers
     static final int MOVE_PICK = 204; // pick a chip from a pool
    static final int MOVE_DROP = 205; // drop a chip
    static final int MOVE_PICKB = 206; // pick from the board
    static final int MOVE_DROPB = 207; // drop on the board
 	static final int MOVE_SWAP = HitSwapButton;	// swap colors (as a move) share the code for the parser's concenience
	
    static final String Hex_SGF = "11"; // sgf game number allocated for hex
    static final String[] HEXGRIDSTYLE = { "1", null, "A" }; // left and bottom numbers

 
    // file names for jpeg images and masks
    static final String ImageDir = "hex/images/";
    static final int HEXTILE_INDEX = 0;
    static final int HEXTILE_NR_INDEX = 1;
    //
    // basic image strategy is to use jpg format because it is compact.
    // .. but since jpg doesn't support transparency, we have to create
    // composite images wiht transparency from two matching images.
    // the masks are used to give images soft edges and shadows
    //
    static final String[] TileFileNames = 
        {   "hextile",
            "hextile-nr"
        };
    // the artwork for these is derived directly from the tile artwork, so 
    // they can use the same offset and scale information
    static final int HEXTILE_BORDER_INDEX = 0;
    static final int HEXTILE_BORDER_NR_INDEX = 4;
    int BorderPairIndex[]={0,1,1,3,3,2};	// this table matches the artwork below to the rotations defined by exitToward(x)
    static final String BorderFileNames[] = 
    {	"border-sw",
    	"border-nw",
    	"border-se",
    	"border-ne",
    	
       	"border-w",
    	"border-n",
     	"border-s",
       	"border-e"

    };
    static final double BORDERSCALES[][] = 
    {	{0.50,0.50,1.76},
    	{0.50,0.50,1.76},
    	{0.50,0.50,1.76},
    	{0.50,0.50,1.76},
    	
    	{0.50,0.50,1.76},
    	{0.50,0.50,1.76},
    	{0.50,0.50,1.76},
    	{0.50,0.50,1.76}
    
    };
    //to keep the artwork aquisition problem as simple as possible, images
    //are recentered and scaled on the fly before presentation.  These arrays
    //are X,Y,SCALE factors to standardize the size and center of the images
    //in the development environment, use "show aux sliders" to pop up sliders
    //which adjust the position and scale of displayed elements.  Copy the numbers
    //back into these arrays and save.  Due to flakeyness in eclipse, it's not very
    //reliable to save and have the result be replaced in the running applet, but
    //this is only a one time thing in development.
    //
    //ad hoc scale factors to fit the stones to the board
    static final double[][] TILESCALES=
    {   {0.50,0.50,1.76},	// hex tile and border artworks
    	{0.50,0.50,1.6}};	// unrotated hex tile and border artwork

    
    static final int BACKGROUND_TILE_INDEX = 0;
    static final int BACKGROUND_REVIEW_INDEX = 1;
    static final String TextureNames[] = { "background-tile" ,"background-review-tile"};


}