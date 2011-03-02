package checkerboard;

import online.game.*;
import online.common.*;
import online.search.*;

/** 
 * the Robot player only has to implement the basic methods to generate and evaluate moves.
 * the actual search is handled by the search driver framework.
 * 
 * in general, the Robot has it's own thread and operates on a copy of the board, so the
 * main UI can continue unaffected by the processing of the robot.
 * @author ddyer
 *
 */
public class CheckerPlay extends commonRobot implements Runnable, CheckerConstants,
    RobotProtocol
{   boolean SAVE_TREE = false;				// debug flag for the search driver
    int VERBOSE = 1;						// how much to print from search
    boolean KILLER = false;					// probably ok for all games with a 1-part move
    static final int DUMBOT_DEPTH = 3;
    static final int GOODBOT_DEPTH = 4;
    static final int BESTBOT_DEPTH = 6;
    int MAX_DEPTH = BESTBOT_DEPTH;
     /* strategies */
    double CUP_WEIGHT = 1.0;
    double MULTILINE_WEIGHT=1.0;
    boolean DUMBOT = false;
    CheckerBoard GameBoard = null;			// this is the "real" game board we're starting from
    CheckerBoard board = null;				// this is our local copy
    int boardSearchLevel = 0;				// the current search depth

    /* constructor */
    public CheckerPlay()
    {
    }
    /** 
     * this is a debugging hack, use this board when the "alternate board"
     * item is selected from the extraactions menu
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
/** undo the effect of a previous Make_Move.  These
 * will always be done in reverse sequence
 */
    public void Unmake_Move(commonMove m)
    {	CheckerMovespec mm = (CheckerMovespec)m;
    	boardSearchLevel--;
    	board.UnExecute(mm);
     }
/** make a move, saving information needed to unmake the move later.
 * 
 */
    public void Make_Move(commonMove m)
    {   CheckerMovespec mm = (CheckerMovespec)m;
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
    public OStack<commonMove> List_Of_Legal_Moves()
    {   return(board.GetListOfMoves());
    }
    
    

    
    /** return a value of the current board position for the specified player.
     * this should be greatest for a winning position.  The evaluations ought
     * to be stable and greater scores should indicate some degree of progress
     * toward winning.
     * @param player
     * @return
     */
    double ScoreForPlayer(CheckerBoard evboard,int player,boolean print)
    {	
     	boolean win = evboard.WinForPlayerNow(player);
    	if(win) { return(VALUE_OF_WIN+(1.0/(1+boardSearchLevel))); }
    	return(evboard.ScoreForPlayer(player,print,CUP_WEIGHT,MULTILINE_WEIGHT,DUMBOT));

    }
    
    /**
     * this is it! just tell me that the position is worth.  
     */
    public double Static_Evaluate_Position(commonMove m)
    {	int playerindex = m.player;
        double val0 = ScoreForPlayer(board,playerindex,false);
        double val1 = ScoreForPlayer(board,nextPlayer[playerindex],false);
        // don't dilute the value of wins with the opponent's positional score.
        // this avoids the various problems such as the robot committing suicide
        // because it's going to lose anyway, and the position looks better than
        // if the opponent makes the last move.  Technically, this isn't needed
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
    	CheckerBoard evboard = (CheckerBoard)GameBoard.cloneBoard();
        double val0 = ScoreForPlayer(evboard,FIRST_PLAYER_INDEX,true);
        double val1 = ScoreForPlayer(evboard,SECOND_PLAYER_INDEX,true);
        if(val1>=VALUE_OF_WIN) { val0=0.0; }
        System.out.println("Eval is "+ val0 +" "+val1+ " = " + (val0-val1));
    }


/** prepare the robot, but don't start making moves.  G is the game object, gboard
 * is the real game board.  The real board shouldn't be changed.  Evaluator and Strategy
 * are parameters from the applet that can be interpreted as desired.
 */
 public void InitRobot(exHashtable info, BoardProtocol gboard, String evaluator,
        int strategy)
    {
        InitRobot(info);
        GameBoard = (CheckerBoard) gboard;
        board = (CheckerBoard)GameBoard.cloneBoard();
        switch(strategy)
        {case 0:
        	MAX_DEPTH = DUMBOT_DEPTH;
         	DUMBOT=true;
        	break;
        case 1:
        	MAX_DEPTH = GOODBOT_DEPTH;
        	DUMBOT=false;
        	break;
        case 2:
         	MAX_DEPTH = BESTBOT_DEPTH;
        	DUMBOT=false;
        	break;
        default: G.Error("Not expecting strategy "+strategy);
        }
    }
    /** copy the game board, in preparation for a search */
    public void InitBoardFromGame()
    {
        board.clone(GameBoard);
    }
/** search for a move on behalf onf player p and report the result
 * to the game.  This is called in the robot process, so the normal
 * game UI is not encumbered by the search.
 */
 public void PrepareToMove(int playerIndex)
 {	InitBoardFromGame();
 }
 /**
  * breakpoint or otherwise override this method to intercept search events.
  * This is a low level way of getting control in the middle of a search for
  * debugging purposes.
  */
public void Search_Break(String msg)
{	super.Search_Break(msg);
}
 public commonMove DoFullMove()
    {
	 CheckerMovespec move = null;

        try
        {

            if (board.DoneState())
            { // avoid problems with gameover by just supplying a done
                move = new CheckerMovespec("Done", board.whoseTurn);
            }

            // it's important that the robot randomize the first few moves a little bit.
            int randomn = RANDOMIZE 
            				? ((board.moveNumber <= 4) ? (20 - board.moveNumber) : 0)
            				: 0;
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
            boardSearchLevel = 0;

            Setup_For_Search(depth, false);
            search_state.save_all_variations = SAVE_TREE;
            search_state.allow_killer = KILLER;
            search_state.verbose=VERBOSE;			// debugging
            search_state.save_top_digest = true;	// always on as a background check
            search_state.save_digest=true;	// debugging only
            search_state.check_duplicate_digests = true; 	// debugging only

            if (move == null)
            {
                move = (CheckerMovespec) search_state.Find_Static_Best_Move(randomn,dif);
            }
        }
        finally
        {
            Accumulate_Search_Summary();
            Finish_Search_In_Progress();
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