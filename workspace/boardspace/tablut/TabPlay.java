package tablut;

import lib.*;
import online.common.*;
import online.game.*;
import online.search.*;


/** 
 * TODO: make robot run a test eval and swap when playing silver.  Specifically against open ranks.
 * the Robot player only has to implement the basic methods to generate and evaluate moves.
 * the actual search is handled by the search driver framework.
 * 
 * in general, the Robot has it's own thread and operates on a copy of the board, so the
 * main UI can continue unaffected by the processing of the robot.
 * @author ddyer
 *
 */
public class TabPlay extends commonRobot<TabGameBoard> implements Runnable, TabConstants,
    RobotProtocol
    {
    static final double VALUE_OF_WIN = 100000.0;
    int VERBOSE = 0;						// 0 is normal, 1 is useful
    boolean SAVE_TREE = false;				// debug flag for the search driver.  Uses lots of memory
    int MAX_DEPTH = 5;						// search depth.
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
    
    int boardSearchLevel = 0;				// the current search depth

    /* constructor */
    public TabPlay()
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
    {	Tabmovespec mm = (Tabmovespec)m;
        board.UnExecute(mm);
        boardSearchLevel--;
    }
/** Called from the search driver to make a move, saving information needed to 
 * unmake the move later.
 * 
 */
    public void Make_Move(commonMove m)
    {   Tabmovespec mm = (Tabmovespec)m;
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
    double SWEEP_WEIGHT = 10.0;
    double TOTAL_SWEEP_WEIGHT = -1.0;
    double WOOD_WEIGHT = 10.0;
    double OPEN_RANK_WEIGHT = 10.0;	// good for gold, lines are counted twice
    double dumbotEval(TabGameBoard evboard,int player,boolean print)
    {	double val = 0.0;
    	if(evboard.playerChip[player]==TabChip.GoldShip)
    	{
    	String msg = "";
     	evboard.classify();
    	if(evboard.flagShipLocation!=null)
    	{	double lv = (evboard.ncols-evboard.flagShipLocation.sweep_score)*SWEEP_WEIGHT;
    		val += lv;
    		if(print) { msg += "sweep "+lv; }
    	}
    	{
    	double lv = ((double)evboard.totalSweepScore/(evboard.ncols*evboard.nrows))*TOTAL_SWEEP_WEIGHT;
    	val += lv;
    	if(print) { msg += " total sweep "+lv; }
    	}
    	{
        	double lv = evboard.open_ranks*OPEN_RANK_WEIGHT;
        	val += lv;
        	if(print) { msg += " open ranks "+lv; }
        	}

    	{
    	double lv = (evboard.gold_ships*2 - evboard.silver_ships)*WOOD_WEIGHT;
    	val += lv;
    	if(print) { msg += " wood "+lv; System.out.println(msg);}
    	}
    	}
    	//G.Error("Not implemented");
     	return(val);
    }
    /** return a value of the current board position for the specified player.
     * this should be greatest for a winning position.  The evaluations ought
     * to be stable and greater scores should indicate some degree of progress
     * toward winning.
     * @param player
     * @return
     */
    double ScoreForPlayer(TabGameBoard evboard,int player,boolean print)
    {	
		double val = 0.0;
     	boolean win = evboard.WinForPlayerNow(player);
     	// make wins in fewer moves look slightly better. Nothing else matters.
     	if(win) 
     		{ evboard.WinForPlayerNow(player);
     		  val = VALUE_OF_WIN+(1.0/(1+boardSearchLevel));
     		  if(print) {System.out.println(" win = "+val); }
     		  return(val); 
     		}
     	// if the position is not a win, then estimate the value of the position
      	
    	switch(Strategy)
    	{	default: G.Error("Not expecting strategy "+Strategy);
    		case DUMBOT: 
    		case SMARTBOT: 
    		case BESTBOT: 	// all the same for now
   			val = dumbotEval(evboard,player,print);
    	}
    	// we're going to subtract two values, and the result must be inside the
    	// bounds defined by +-WIN
    	G.Assert((val<(VALUE_OF_WIN/2))&&(val>=(VALUE_OF_WIN/-2)),"value out of range");
     	return(val);
    }
    
    /**
     * this is it! just tell me that the position is worth.  
     */
    public double Static_Evaluate_Position(commonMove m)
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
            TabGameBoard evboard = new TabGameBoard(GameBoard.gameType());
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
 public void InitRobot(ViewerProtocol newParam, exHashtable info, BoardProtocol gboard,
        String evaluator, int stragegy)
    {
        InitRobot(info);
        GameBoard = (TabGameBoard) gboard;
        board = new TabGameBoard(GameBoard.gameType());
        // strategy with be 0,1,2 for Dumbot, Smartbot, Bestbot
        switch(stragegy)
        {
        default: G.Error("Not expecting strategy "+stragegy);
        case DUMBOT_LEVEL: Strategy = DUMBOT; break;
        case SMARTBOT_LEVEL: Strategy = SMARTBOT; break;
        case BESTBOT_LEVEL: Strategy = BESTBOT; break;
        }
    }
    /** copy the game board, in preparation for a search */
    public void InitBoardFromGame()
    {
        board.clone(GameBoard);
        board.sameboard(GameBoard);
    }
    
/** PrepareToMove is called in the thread of the main game run loop at 
 * a point where it is appropriate to start a move.  We must capture the
 * board state at this point, so that when the robot runs it will not
 * be affected by any subsequent changes in the real game board state.
 * The canonical error here was the user using the < key before the robot
 * had a chance to capture the board state.
 */
public void PrepareToMove(int playerIndex)
{
    InitBoardFromGame();

}
/** search for a move on behalf of player p and report the result
 * to the game.  This is called in the robot process, so the normal
 * game UI is not encumbered by the search.
 */
 public commonMove DoFullMove()
    {
        Tabmovespec move = null;
        try
        {
 
            if (board.DoneState())
            { // avoid problems with gameover by just supplying a done
                move = new Tabmovespec("Done", board.whoseTurn);
            }

            // it's important that the robot randomize the first few moves a little bit.
            int randomn = RANDOMIZE 
            				? ((board.moveNumber <= 6) ? (20 - board.moveNumber) : 0)
            				: 0;
            boardSearchLevel = 0;

            int depth = MAX_DEPTH;
            if(randomn>0) { depth--; // because no alpha-beta if random and threshold
            }
            Search_Driver search_state = Setup_For_Search(depth, false);
            search_state.save_all_variations = SAVE_TREE;
            search_state.good_enough_to_quit = GOOD_ENOUGH_VALUE;
            search_state.verbose = VERBOSE;
            search_state.allow_killer = KILLER;
            search_state.save_digest = false;	// debug only
            if (move == null)
            {
                move = (Tabmovespec) search_state.Find_Static_Best_Move(randomn,VALUE_OF_WIN/4);
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
