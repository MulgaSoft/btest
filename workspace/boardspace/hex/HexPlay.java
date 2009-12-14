package hex;

import online.game.*;
import online.common.*;
import online.search.*;
import java.util.*;

/** 
 * the Robot player only has to implement the basic methods to generate and evaluate moves.
 * the actual search is handled by the search driver framework.
 * 
 * in general, the Robot has it's own thread and operates on a copy of the board, so the
 * main UI can continue unaffected by the processing of the robot.
 * @author ddyer
 *
 */
public class HexPlay extends commonRobot implements Runnable, HexConstants,
    RobotProtocol
    {
    static final double VALUE_OF_WIN = 10000.0;
    static final int VALUE_LARGE = 200;
    int VERBOSE = 0;						// 0 is normal, 1 is useful
    boolean SAVE_TREE = false;				// debug flag for the search driver.  Uses lots of memory
    int MAX_DEPTH = 4;						// search depth.
	static final boolean KILLER = false;	// if true, allow the killer heuristic in the search
	static final double GOOD_ENOUGH_VALUE = VALUE_OF_WIN;	// good enough to stop looking
				// this is appropriate for simple games like hex, but probably not too effective
				// until there is a much better evaluator.
    // this is an arbitrary value assigned to a winning position, so minmax
    // and alpha-beta will prefer wins to non-wins.  It's exact value is
    // unimportant, but it must at least double any non-winning score the
    // evaluator produces.  Integers are cheap, don't be stingy.  The main thing
    // is to have a convenient range of numbers to work with.
    /* strategies */
    static final int DUMBOT = 1;
    static final int SMARTBOT = 2;
    static final int BESTBOT = 3;
    int Strategy = DUMBOT;
    
    HexGameBoard GameBoard = null;			// this is the "real" game board we're starting from
    HexGameBoard board = null;				// this is our local copy
    int boardSearchLevel = 0;				// the current search depth

    /* constructor */
    public HexPlay(int strategy)
    {
    	Strategy = strategy;
    }
    /** 
     * this is a debugging hack, use this board when the "alternate board"
     * item is selected from the extraactions menu, so we can visualize the
     * state of the robot's search when debugging the robot thread
     */
    public BoardProtocol getBoard()
    {
    	return(board);
    }
    /** return true if the search should be depth limited at this point.
     * 
     */
    public boolean Depth_Limit(int current, int max)
    {	// for simple games where there is always one move per player per turn
    	// current>=max is good enough.  For more complex games where there could
    	// be several moves per turn, we have to keep track of the number of turn changes.
    	// it's also possible to implement quiescence search by carefully adjusting when
    	// this method returns true.
        return(current>=max);
    } 

    /** Called from the search driver to undo the effect of a previous Make_Move.  These
     * will always be done in reverse sequence
     */
    public void Unmake_Move(commonMove m)
    {	Hexmovespec mm = (Hexmovespec)m;
        board.UnExecute(mm);
        boardSearchLevel--;
    }
	/** Called from the search driver to make a move, saving information needed to 
	 * unmake the move later.
	 * 
	 */
    public void Make_Move(commonMove m)
    {   Hexmovespec mm = (Hexmovespec)m;
        board.RobotExecute(mm);
        boardSearchLevel++;
    }
	/** return true if the game is over.
	 * 
	 */
    public boolean Game_Over_P()
    {
        return (board.GameOver());
    }
	/** return an enumeration of moves to consider at this point.  It doesn't have to be
	 * the complete list, but that is the usual procedure. Moves in this list will
	 * be evaluated and sorted, then used as fodder for the depth limited search
	 * pruned with alpha-beta.
	 */
    public Vector List_Of_Legal_Moves()
    {
    	return(board.GetListOfMoves());

/*      We could try and pre-filter the moves here.  Code not yet working.
 *     	board.findBlobs();
    	
    	// Add up the 2-distance of each hex for each player to each side of the board.
    	calcTwoDist(board, OnlineConstants.FIRST_PLAYER_INDEX, 1, true);
    	calcTwoDist(board, OnlineConstants.FIRST_PLAYER_INDEX, board.ncols, false);
    	calcTwoDist(board, OnlineConstants.SECOND_PLAYER_INDEX, 1, false);
    	calcTwoDist(board, OnlineConstants.SECOND_PLAYER_INDEX, board.ncols, false);
    	
    	int best = 0;
    	for (hexCell cell = (hexCell)board.allCells; cell != null; cell = (hexCell)cell.next) 
    	{
    		if (cell.two_dist < best) best = cell.two_dist;
    	}
    	
    	Vector ret = new Vector();
     	if (board.board_state==PLAY_OR_SWAP_STATE)
     	{
     		ret.addElement(new Hexmovespec("swap",board.whoseTurn));
     	}
    	
    	for (hexCell cell = (hexCell)board.allCells; cell != null; cell = (hexCell)cell.next) 
    	{
    		if (cell.two_dist < best + 20)
    	    {
    			ret.add(new Hexmovespec("dropb "+board.getPlayerChip(board.whoseTurn).colorName+" "+cell.col+" "+cell.row,board.whoseTurn));
    	    }
    	}
    	return (ret);
*/    }
    
    // this is the static evaluation used by Dumbot.  When testing a better
    // strategy, leave this untouched as a reference point.  The real meat
    // of the evaluation is the "blobs" class which is constructed by the
    // board.  The blobs structure aggregates the stones into chains and
    // keeps some track of how close the chains are to each other and to
    // the edges of the board.
    //
    double dumbotEval(HexGameBoard evboard,int player,boolean print)
    {	// note we don't need "player" here because the blobs variable
    	// contains all the information, and was calculated for the player
    	double val = 0.0;
    	double ncols = evboard.ncols;
    	for(int i=0;i<evboard.blobs.size();i++)
    	{
    		hexblob blob = (hexblob)evboard.blobs.elementAt(i);
    		int span = blob.span();
    		double spanv = span/ncols;
    		if(print) 
    		{ System.out.println(blob + " span "+span + " = " + (spanv*spanv));
    		}
    		val += spanv*spanv;		// basic metric is the square of the span
    	}
    	OStack merged = hexblob.mergeBlobs(evboard.blobs);
    	for(int i=0;i<merged.size();i++)
    	{
    		hexblob blob = (hexblob)merged.elementAt(i);
    		int span = blob.span();
    		double spanv = span/ncols;
    		if(print) 
    		{ System.out.println(blob + " span "+span + " = " + (spanv*spanv));
    		}
   		val += spanv*spanv;		// basic metric is the square of the span
    	}	
    	return(val);
    }

/* Beginning of implementation of "SmartBot".  The evaluator used is "two-distance", as per
 * the hex playing program Queen-bee, and described in the paper "Search and evaluation in Hex"
 * by Jack van Rijswijck.
 * 
 * Implementor: Adam Shepherd.
 */
    
    // Called to indicate we have discovered a new route to a cell. 
    void tryDist(Vector addTo, hexCell cell, hexChip color)
    {
    	if (cell.chip == color)
    	{
    		// A blob of cells of our color are all equivalent.  Make sure we track the
    		// links on the first cell in the blob.
    		cell = cell.blob.cells;
    	}
    	else if (cell.chip != null)
    	{
    		// A cell of the opponents color no use. 
    		return;
    	}
    	
    	// Track the new link.
		cell.links++;
		
		if (cell.links == 2)
		{
			// We've found the second link, so the distance to this cell is now known.
			
	    	if (cell.chip == color)
	    	{
	    		// For a cell of our color, we don't add the cell itself, we add all blank cells adjacent
	    		// to the blob.  Effectively we're saying that a cell of our color has zero distance. 
	        	for (hexCell loop_cell = cell.blob.cells; loop_cell != null; loop_cell = loop_cell.nextInBlob)
	        	{
	        	   	for(int dir = 0; dir<6; dir++)
	        		{	
	        	   		hexCell lNeighbour = (hexCell)cell.exitToward(dir);
	        	   		if ((lNeighbour != null) && (lNeighbour.chip == null) && (lNeighbour.links < 2)) 
	        	   		{
	        	   			lNeighbour.links = 2;
	    	        		addTo.add(lNeighbour);
	        	   		}
	            	}
	        	}
	    	}
	    	else 
	    	{
	    		// Blank cell, so add the cell itself.
        		addTo.add(cell);
	    	}
		}
    }

    // Calculate the "2-distance" for each hex to a specified side of the board and player.
    //
    // Normal "1-distance" measures how many moves we'd need to connect if we can play turn
    // after turn without interruption.  Hence the distance of a given hex is the one more than
    // the lowest distance of all its neighbours.
    //
    // The "2-distance" of a hex is defined as one more than the _second_ lowest 2-distance 
    // of all its neighbours.  This could be thought of as saying that the opponent will take the 
    // best move, and we'll get the next best.  However don't get too hung up on that idea,
    // because it doesn't accurately model the opponent's efforts - it's just a heuristic that
    // happens to give a reasonable result.
    //
    // The algorithm used is the classic Dijkstra's algorithm, with two alterations.
    // - We need to ignore the best neighbour and only take the second best.  This is handled with the 
    //   "links" field which tracks how many times we have reached a cell.
    // - We can simplify because all 'edges' in the graph are length 1.
    void calcTwoDist(HexGameBoard evboard, int player, int target, boolean reset)
	{
    	hexChip color = evboard.getPlayerChip(player);
    	// In Dijkstra with lengths of all edges equal to one, we will build up the distances
    	// starting with those of distance 1, 2, 3, ...   As we are processing the cells of distance
    	// d, we examine their neighbours.  If any now has 2 links, then the cell has distance d+1.
    	// We add that neighbour cell to the working set for processing next time round the loop. 
    	Vector workingSet = new Vector();
    	int dist = 0;

    	// Reset state of each cell.
    	for (hexCell cell = (hexCell)evboard.allCells; cell != null; cell = (hexCell)cell.next)
    	{
    		cell.links = 0;
    		if (reset) cell.two_dist = 0;
    	}
    	
    	// Start by creating a working set from all the cells next to the edge in question.
    	for (hexCell cell = (hexCell)evboard.allCells; cell != null; cell = (hexCell)cell.next)
    	{
    		int relevant_val = -1;
        	if (color==hexChip.White) { relevant_val = cell.col - 'A' + 1; }
        	else if (color==hexChip.Black) { relevant_val = cell.row; }
        	else { G.Error("Not expecting this"); }

        	if (relevant_val == target)
        	{
        		// This is next to the specified edge.  It necessarily has two links to the 
        		// edge, so call the procedure twice.
        		tryDist(workingSet, cell, color);
        		tryDist(workingSet, cell, color);
        	}
    	}
    	
    	// This is the main work of the algorithm.  Save the working set as the save set.
    	// Mark each cell in the save set as having distance d.  Consider neighbours of this cell
    	// as candidates for the new working set (hence having distance d+1).
    	while (!workingSet.isEmpty())
    	{
    		Vector saveSet = workingSet;
    		dist++;
    		workingSet = new Vector();
    		Iterator ii = saveSet.iterator();
    		
    		while (ii.hasNext())
    		{
    			hexCell cell = (hexCell)ii.next();
    			cell.two_dist += dist - VALUE_LARGE;
    			
			   	for (int dir = 0; dir<6; dir++)
				{	
			   		hexCell lNeighbour = (hexCell)cell.exitToward(dir);
			   		if (lNeighbour != null)
			   		{
			   			tryDist(workingSet, lNeighbour, color);
			   		}
		    	}
    		}
    	}
	}

    // Top-level evaluator function for Smartbot.
    double betterEval(HexGameBoard evboard, int player, boolean print)
    {	
    	// Calculate the 2-distance of each hex for this player, first from one side of the 
    	// board then the other.  (Clear totals first time round, add them on second time.)
    	calcTwoDist(evboard, player, 1, true);
    	calcTwoDist(evboard, player, evboard.ncols, false);

    	// For each hex, we now have the sum of the the 2-distances from each side of the board.  
    	// The idea is that this gives an idea of how close to winning we are if we play this hex.
    	// If it's our turn, we'll take the best such hex, if it's not we likely have to settle 
    	// for second best as the opponent will take the best one.
    	boolean our_turn = (evboard.whoseTurn == player);
    	int best = VALUE_LARGE;
    	int discarded_one = VALUE_LARGE;
    	int freq = 0;
    	
    	for (hexCell cell = (hexCell)evboard.allCells;  cell != null; cell = (hexCell)cell.next)
    	{
    		int total = cell.two_dist;
    		
			if (!our_turn && total < discarded_one)
			{
				int temp = discarded_one; 
				discarded_one = total;
				total = temp;
			}
    		
    		if (total < best) 
    		{
    			best = total;
    			freq = 1;
    		}
    		else if (total == best)
    		{
    			freq++;
    		}
    	}
    	
		if (print) 
		{
			System.out.println("Best " + best + " freq " + freq);
		}
    	
    	return(- best*10 + freq/10.0);
    }
    
    /** return a value of the current board position for the specified player.
     * this should be greatest for a winning position.  The evaluations ought
     * to be stable and greater scores should indicate some degree of progress
     * toward winning.
     * @param player
     * @return
     */
    double ScoreForPlayer(HexGameBoard evboard,int player,boolean print)
    {
		double val = 0.0;
     	boolean win = evboard.WinForPlayerNow(player);
     	// make wins in fewer moves look slightly better. Nothing else matters.
     	if(win) 
     		{ val = VALUE_OF_WIN+(1.0/(1+boardSearchLevel));
     		  if(print) {System.out.println(" win = "+val); }
     		  return(val); 
     		}
     	// if the position is not a win, then estimate the value of the position
      	
    	switch(Strategy)
    	{	default: G.Error("Not expecting strategy "+Strategy);
    		case DUMBOT: 
   			  val = dumbotEval(evboard,player,print);
   			  break;
    		case SMARTBOT: 
    		case BESTBOT: 	// both the same for now
    		  val = betterEval(evboard,player,print);	
    		  break;
    	}
    	// we're going to subtract two values, and the result must be inside the
    	// bounds defined by +-WIN
    	G.Assert((val<(VALUE_OF_WIN/2))&&(val>=(VALUE_OF_WIN/-2)),"value out of range");
     	return(val);
    }
    
    /** this is called from the search driver to evaluate a particular position. The driver
     * calls List_of_Legal_Moves, then calls Make_Move/Static_Evaluate_Position/UnMake_Move
     *  for each and sorts the result to preorder the tree for further evaluation
     */
    public double Static_Evaluate_Position(	commonMove m)
    {	int playerindex = m.player;
        double val0 = ScoreForPlayer(board,playerindex,false);
        double val1 = ScoreForPlayer(board,nextPlayer[playerindex],false);
        // don't dilute the value of wins with the opponent's positional score.
        // this avoids the various problems such as the robot comitting suicide
        // because it's going to lose anyway, and the position looks better than
        // if the oppoenent makes the last move.  Technically, this isn't needed
        // for hex because there is no such thing as a suicide move, but the logic
        // is included here because this is supposed to be an example.
        if(val0>=VALUE_OF_WIN) { return(val0); }
        if(val1>=VALUE_OF_WIN) { return(-val1); }
        return(val0-val1);
    }
    /**
     * called as a robot debugging hack from the viewer.  Print debugging
     * information about the static analysis of the current position.
     * */
    public void StaticEval()
    {
        HexGameBoard evboard = new HexGameBoard(GameBoard.gametype);
        evboard.clone(GameBoard);
        double val0 = ScoreForPlayer(evboard,FIRST_PLAYER_INDEX,true);
        double val1 = ScoreForPlayer(evboard,SECOND_PLAYER_INDEX,true);
        System.out.println("Eval is "+ val0 +" "+val1+ " = " + (val0-val1));
    }


	/** prepare the robot, but don't start making moves.  G is the game object, gboard
	 * is the real game board.  The real board shouldn't be changed.  Evaluator and Strategy
	 * are parameters from the applet that can be interpreted as desired.  The debugging 
	 * menu items "set robotlevel(n)" set the value of "strategy".  Evaluator is not
	 * really used at this point, but was intended to be the class name of a plugin
	 * evaluator class
	 */
	public void InitRobot(exHashtable info, BoardProtocol gboard, String evaluator,
        int stragegy)
    {
        InitRobot(info);
        GameBoard = (HexGameBoard) gboard;
        board = new HexGameBoard(GameBoard.gametype);
        // strategy with be 0,1,2 for Dumbot, Smartbot, Bestbot
        switch(stragegy)
        {
        default: G.Error("Not expecting strategy "+stragegy);
        case 0: Strategy = DUMBOT; break;
        case 1: Strategy = SMARTBOT; break;
        case 2: Strategy = BESTBOT; break;
        }
    }
     
	/** PrepareToMove is called in the thread of the main game run loop at 
	 * a point where it is appropriate to start a move.  We must capture the
	 * board state at this point, so that when the robot runs it will not
	 * be affected by any subsequent changes in the real game board state.
	 * The canonical error here was the user using the < key before the robot
	 * had a chance to capture the board state.
	 */
	public void PrepareToMove(int playerIndex)
	{	//use this for an arms-length robot:  
		GameBoard.getStateString();
		//use this for a friendly robot that shares the board class
		board.clone(GameBoard);
	    board.sameboard(GameBoard);	// check that we got a good copy.  Not expensive to do this once per move
	
	}
	/** search for a move on behalf of player p and report the result
	 * to the game.  This is called in the robot process, so the normal
	 * game UI is not encumbered by the search.
	 */
	 public commonMove DoFullMove()
    {
        Hexmovespec move = null;
        try
        {
 
            if (board.DoneState())
            { // avoid problems with gameover by just supplying a done
                move = new Hexmovespec("Done", board.whoseTurn);
            }

            // it's important that the robot randomize the first few moves a little bit.
            int randomn = RANDOMIZE ? ((board.moveNumber <= 6) ? (14 - 2*board.moveNumber) : 0) : 0;
            boardSearchLevel = 0;

            int depth = MAX_DEPTH;	// search depth
            double dif = 0.0;		// stop randomizing if the value drops this much
            // if the "dif" and "randomn" arguments to Find_Static_Best_Move
            // are both > 0, then alpha-beta will be disabled to avoid randomly
            // picking moves whose value is uncertain due to cutoffs.  This makes
            // the search MUCH slower so depth ought to be limited
            // if ((randomn>0)&&(dif>0.0)) { depth--; }
            // for games such as hex, where there are no "fools mate" type situations
            // the best colution is to use dif=0.0;  For games with fools mates,
            // set dif so the really bad choices will be avoided
            Setup_For_Search(depth, false);
            search_state.save_all_variations = SAVE_TREE;
            search_state.good_enough_to_quit = GOOD_ENOUGH_VALUE;
            search_state.verbose = VERBOSE;
            search_state.allow_killer = KILLER;
            search_state.save_digest=false;	// debugging only
            search_state.check_duplicate_digests = false; 	// debugging only

            if (move == null)
            {	// randomn takes the a random element among the first N
            	// to provide variability.  The second parameter is how
            	// large a drop in the expectation to accept.  For hex this
            	// doesn't really matter, but some games have disasterous
            	// opening moves that we wouldn't want to choose randomly
                move = (Hexmovespec) search_state.Find_Static_Best_Move(randomn,dif);
            }
        }
        finally
        {
            Accumulate_Search_Summary();
            Abort_Search_In_Progress();
        }

        if (move != null)
        {
            if(debug && (move.op!=MOVE_DONE)) { move.showPV("exp final pv: "); }
            // normal exit with a move
            return (move);
        }

        continuous = false;
        // abnormal exit
        return (null);
    }
 }
