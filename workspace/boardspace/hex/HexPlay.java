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
    static final double VALUE_OF_WIN = 1000.0;
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
    public HexPlay()
    {
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
    }
    // this is the static evaluation used by Dumbot.  When testing a better
    // strategy, leave this untouched as a reference point.  The real meat
    // of the evaluation is the "blobs" class which is constructed by the
    // board.  The blobs structure aggregates the stones into chains and
    // keeps some track of how close the chains are to each other and to
    // the edges of the board.
    //
    double dumbotEval(HexGameBoard evboard,Vector blobs,int player,boolean print)
    {	// note we don't need "player" here because the blobs variable
    	// contains all the information, and was calculated for the player
    	double val = 0.0;
    	double ncols = evboard.ncols;
    	for(int i=0;i<blobs.size();i++)
    	{
    		hexblob blob = (hexblob)blobs.elementAt(i);
    		int span = blob.span();
    		double spanv = span/ncols;
    		if(print) 
    		{ System.out.println(blob + " span "+span + " = " + (spanv*spanv));
    		}
    		val += spanv*spanv;		// basic metric is the square of the span
    	}
    	OStack merged = hexblob.mergeBlobs(blobs);
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
    /** return a value of the current board position for the specified player.
     * this should be greatest for a winning position.  The evaluations ought
     * to be stable and greater scores should indicate some degree of progress
     * toward winning.
     * @param player
     * @return
     */
    double ScoreForPlayer(HexGameBoard evboard,int player,boolean print)
    {	Vector blobs = new Vector();
		double val = 0.0;
     	boolean win = evboard.WinForPlayerNow(player,blobs);
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
    		case SMARTBOT: 
    		case BESTBOT: 	// all the same for now
   			val = dumbotEval(evboard,blobs,player,print);
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
            int randomn = RANDOMIZE ? ((board.moveNumber <= 6) ? (40 - board.moveNumber) : 0) : 0;
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
