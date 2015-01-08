package checkerboard;

import online.game.*;

import java.util.*;

import lib.*;

import static checkerboard.CheckerMovespec.*;
/**
 * CheckerBoard knows all about the game of Checkers.
 * It gets a lot of logistic support from game.rectBoard, 
 * which knows about the coordinate system.  
 * 
 * This class doesn't do any graphics or know about anything graphical, 
 * but it does know about states of the game that should be reflected 
 * in the graphics.
 * 
 *  The principle interface with the game viewer is the "Execute" method
 *  which processes moves.  Note that this
 *  
 *  In general, the state of the game is represented by the contents of the board,
 *  whose turn it is, and an explicit state variable.  All the transitions specified
 *  by moves are mediated by the state.  In general, my philosophy is to be extremely
 *  restrictive about what to allow in each state, and have a lot of trip wires to
 *  catch unexpected transitions.   We expect to be fed only legal moves, but mistakes
 *  will be made and it's good to have the maximum opportunity to catch the unexpected.
 *  
 * Note that none of this class shows through to the game controller.  It's purely
 * a private entity used by the viewer and the robot.
 * 
 * @author ddyer
 *
 */

class CheckerBoard extends rectBoard<CheckerCell> implements BoardProtocol,CheckerConstants
{	
    public int boardColumns;	// size of the board
    public int boardRows;
    public void SetDrawState() { setState(CheckerState.Draw); }
    public CheckerCell rack[] = null;
    public OStack<CheckerCell> animationStack = new OStack<CheckerCell>(CheckerCell.class);
    //
    // private variables
    //
    private CheckerState board_state = CheckerState.Play;	// the current board state
    private CheckerState unresign = null;					// remembers the previous state when "resign"
    Variation variation = Variation.Checkers_10;
    public CheckerState getState() { return(board_state); } 
	public void setState(CheckerState st) 
	{ 	unresign = (st==CheckerState.Resign)?board_state:null;
		board_state = st;
		if(!board_state.GameOver()) 
			{ G.setValue(win,false); 	// make sure "win" is cleared
			}
	}
    private CheckerId playerColor[]={CheckerId.White_Chip_Pool,CheckerId.Black_Chip_Pool};
 	public CheckerId getPlayerColor(int p) { return(playerColor[p]); }
	
   public int chips_on_board[] = new int[2];			// number of chips currently on the board
    
    // intermediate states in the process of an unconfirmed move should
    // be represented explicitly, so unwinding is easy and reliable.
    public CheckerChip pickedObject = null;
    private OStack<CheckerCell> pickedSourceStack = new OStack<CheckerCell>(CheckerCell.class);
    private OStack<CheckerCell> droppedDestStack = new OStack<CheckerCell>(CheckerCell.class);
    
	// factory method
	public CheckerCell newcell(char c,int r)
	{	return(new CheckerCell(c,r));
	}
    public CheckerBoard(String init,long rv,int np) // default constructor
    {   
        doInit(init,rv,np); // do the initialization 
     }


	public void sameboard(BoardProtocol f) { sameboard((CheckerBoard)f); }

    /**
     * Robots use this to verify a copy of a board.  If the copy method is
     * implemented correctly, there should never be a problem.  This is mainly
     * a bug trap to see if clone,digest and sameboard methods agree.
     * @param from_b
     */
    public void sameboard(CheckerBoard from_b)
    {
    	super.sameboard(from_b);	// calls sameCell for each cell, also for inherited class variables.
    	G.Assert(unresign==from_b.unresign,"unresign mismatch");
       	G.Assert(G.sameArrayContents(win,from_b.win),"win array contents match");
       	G.Assert(G.sameArrayContents(playerColor,from_b.playerColor),"playerColor contents match");
       	G.Assert(G.sameArrayContents(chips_on_board,from_b.chips_on_board),"chips_on_board contents match");
       	G.Assert(sameCells(pickedSourceStack,from_b.pickedSourceStack),"pickedSourceStack mismatch");
        G.Assert(sameCells(droppedDestStack,from_b.droppedDestStack),"droppedDestStack mismatch");
        G.Assert(pickedObject==from_b.pickedObject,"pickedObject doesn't match");
        // this is a good overall check that all the copy/check/digest methods
        // are in sync, although if this does fail you'll no doubt be at a loss
        // to explain why.
        G.Assert(Digest()==from_b.Digest(),"Digest matches");

    }

    /** 
     * Digest produces a 64 bit hash of the game state.  This is used in many different
     * ways to identify "same" board states.  Some are germaine to the ordinary operation
     * of the game, others are for system record keeping use; so it is important that the
     * game Digest be consistent both within a game and between games over a long period
     * of time which have the same moves. 
     * (1) Digest is used by the default implementation of EditHistory to remove moves
     * that have returned the game to a previous state; ie when you undo a move or
     * hit the reset button.  
     * (2) Digest is used after EditHistory to verify that replaying the history results
     * in the same game as the user is looking at.  This catches errors in implementing
     * undo, reset, and EditHistory
	 * (3) Digest is used by standard robot search to verify that move/unmove 
	 * returns to the same board state, also that move/move/unmove/unmove etc.
	 * (4) Digests are also used as the game is played to look for draw by repetition.  The state
     * after most moves is recorded in a hashtable, and duplicates/triplicates are noted.
     * (5) games where repetition is forbidden (like xiangqi/arimaa) can also use this
     * information to detect forbidden loops.
	 * (6) Digest is used in fraud detection to see if the same game is being played
     * over and over. Each game in the database contains a digest of the final
     * state of the game, and a midpoint state of the game. Other site machinery
     * looks for duplicate digests.  
     * (7) digests are also used in live play to detect "parroting" by running two games
     * simultaneously and playing one against the other.
     */
   public long Digest()
    {

        // the basic digestion technique is to xor a bunch of random numbers. The key
        // trick is to always generate exactly the same sequence of random numbers, and
        // xor some subset of them.  Note that if you tweak this, all the existing
        // digests are invalidated.
        //
        Random r = new Random(64 * 1000); // init the random number generator
        long v = super.Digest();

		v ^= chip.Digest(r,pickedObject);
		v ^= Digest(r,pickedSourceStack);
		v ^= Digest(r,droppedDestStack);
		v ^= (board_state.ordinal()*10+whoseTurn)*r.nextLong();
        return (v);
    }
   public BoardProtocol cloneBoard() 
	{ CheckerBoard copy = new CheckerBoard(gametype,randomKey,players_in_game);
	  copy.clone(this); 
	  return(copy);
	}
   public void copyFrom(BoardProtocol b) { clone((CheckerBoard)b); }


    /* make a copy of a board.  This is used by the robot to get a copy
     * of the board for it to manupulate and analyze without affecting 
     * the board that is being displayed.
     *  */
    public void clone(CheckerBoard from_b)
    {	
        super.clone(from_b);			// copies the standard game cells in allCells list
        pickedObject = from_b.pickedObject;	
        getCell(pickedSourceStack,from_b.pickedSourceStack);
        getCell(droppedDestStack,from_b.droppedDestStack);
        G.copy(win,from_b.win);
        G.copy(playerColor,from_b.playerColor);
        G.copy(chips_on_board,from_b.chips_on_board);
        board_state = from_b.board_state;
        unresign = from_b.unresign;
        sameboard(from_b);
    }
    public void doInit(String gtype,long rv)
    {
    	doInit(gtype,rv,players_in_game);
    }
    /* initialize a board back to initial empty state */
    public void doInit(String gtype,long rv,int np)
    {  	drawing_style = DrawingStyle.STYLE_NOTHING; // STYLE_CELL or STYLE_LINES
    	Grid_Style = GRIDSTYLE; //coordinates left and bottom
    	randomKey = rv;
    	players_in_game = np;
		rack = new CheckerCell[2];
    	Random r = new Random(67246765);
     	for(int i=0,pl=FIRST_PLAYER_INDEX;i<2; i++,pl=nextPlayer[pl])
    	{
       	CheckerCell cell = new CheckerCell(r);
       	cell.rackLocation=RackLocation[i];
       	cell.addChip(CheckerChip.getChip(i));
    	rack[i]=cell;
     	}    
     	
     	variation = Variation.findVariation(gtype);
     	switch(variation)
     	{
     	default:  G.Error("No init named "+gtype);
     	case Checkers_10:
     	case Checkers_8:
     	case Checkers_6:
     		boardColumns = variation.size;
     		boardRows = variation.size;
     		initBoard(boardColumns,boardRows);
     		gametype = gtype;
     		break;
     	}

        allCells.setDigestChain(r);
	    setState(CheckerState.Puzzle);
	    
	    // fill the board with the background tiles
	    for(CheckerCell c = allCells; c!=null; c=c.next)
	    {  int i = (c.row+c.col)%2;
	       c.addChip(CheckerChip.getTile(i));
	    }
	    
	    whoseTurn = FIRST_PLAYER_INDEX;
		playerColor[FIRST_PLAYER_INDEX]=CheckerId.White_Chip_Pool;
		playerColor[SECOND_PLAYER_INDEX]=CheckerId.Black_Chip_Pool;
		pickedSourceStack.clear();
		droppedDestStack.clear();
		pickedObject = null;
        G.setValue(win,false);
        G.setValue(chips_on_board,0);
        moveNumber = 1;

        // note that firstPlayer is NOT initialized here
    }


    //
    // change whose turn it is, increment the current move number
    //
    public void setNextPlayer()
    {
        switch (board_state)
        {
        default:
            G.Error("Move not complete, can't change the current player");
            break;
        case Puzzle:
            break;
        case Confirm:
        case Draw:
        case Resign:
            moveNumber++; //the move is complete in these states
            setWhoseTurn(nextPlayer[whoseTurn]);
            return;
        }
    }

    /** this is used to determine if the "Done" button in the UI is live
     *
     * @return
     */
    public boolean DoneState()
    {	
        switch (board_state)
        {case Resign:
         case Confirm:
         case Draw:
            return (true);

        default:
            return (false);
        }
    }


    void setGameOver(boolean winCurrent,boolean winNext)
    {	if(winCurrent && winNext) { winCurrent=false; } // simultaneous win is a win for player2
    	win[whoseTurn]=winCurrent;
    	win[nextPlayer[whoseTurn]]=winNext;
    	setState(CheckerState.Gameover);
    }
    
    public boolean WinForPlayerNow(int player)
    {	// return true if the conditions for a win exist for player right now
    	if(board_state==CheckerState.Gameover) { return(win[player]); }
    	G.Error("not implemented");
    	return(false);
    }
    // estimate the value of the board position.
    public double ScoreForPlayer(int player,boolean print,double cup_weight,double ml_weight,boolean dumbot)
    {  	double finalv=0.0;
    	G.Error("not implemented");
    	return(finalv);
    }


    //
    // finalize all the state changes for this move.
    //
    public void acceptPlacement()
    {	
        pickedObject = null;
        droppedDestStack.clear();
        pickedSourceStack.clear();
     }
    //
    // undo the drop, restore the moving object to moving status.
    //
    private void unDropObject()
    {
    G.Assert(pickedObject==null, "nothing should be moving");
    if(droppedDestStack.size()>0)
    	{
    	CheckerCell dr = droppedDestStack.pop();
    	switch(dr.rackLocation())
	    	{
	   		default: G.Error("Not expecting rackLocation %s",dr.rackLocation);
			case BoardLocation: 
				pickedObject = dr.removeTop(); 
				break;
			case White_Chip_Pool:	// treat the pools as infinite sources and sinks
			case Black_Chip_Pool:	
				pickedObject = dr.topChip();
				break;	// don't add back to the pool
	    	
	    	}
	    	}
    }
    // 
    // undo the pick, getting back to base state for the move
    //
    private void unPickObject()
    {	CheckerChip po = pickedObject;
    	if(po!=null)
    	{
    		CheckerCell ps = pickedSourceStack.pop();
    		switch(ps.rackLocation())
    		{
    		default: G.Error("Not expecting rackLocation %s",ps.rackLocation);
    		case BoardLocation: ps.addChip(po); break;
    		case White_Chip_Pool:
    		case Black_Chip_Pool:	break;	// don't add back to the pool
    		}
    		pickedObject = null;
     	}
     }

    // 
    // drop the floating object.
    //
    private void dropObject(CheckerCell c)
    {   G.Assert(pickedObject!=null,"pickedObject should not be null"); 	    		
    	switch(c.rackLocation())
		{
		default: G.Error("Not expecting rackLocation %s",c.rackLocation);
		case BoardLocation: c.addChip(pickedObject); break;
		case White_Chip_Pool:
		case Black_Chip_Pool:	break;	// don't add back to the pool
		}
       	droppedDestStack.push(c);
    }
    //
    // true if col,row is the place where something was dropped and not yet confirmed.
    // this is used to mark the one square where you can pick up a marker.
    //
    public boolean isDest(CheckerCell cell)
    {	return((droppedDestStack.size()>0) && (droppedDestStack.top()==cell));
    }
    
	//get the index in the image array corresponding to movingObjectChar 
    // or HitNoWhere if no moving object.  This is used to determine what
    // to draw when tracking the mouse.  
    // Caution! This method is called in the mouse process
    public int movingObjectIndex()
    {	CheckerChip ch = pickedObject;
    	if(ch!=null)
    		{ return(ch.chipNumber());
    		}
        return (NothingMoving);
    }
    
    public CheckerCell getCell(CheckerId source,char col,int row)
    {
        switch (source)
        {
        default:
            G.Error("Not expecting source " + source);
        case BoardLocation:
        	return(getCell(col,row));
        case White_Chip_Pool:
       		return(rack[White_Chip_Index]);
        case Black_Chip_Pool:
       		return(rack[Black_Chip_Index]);
        }
    }
    public CheckerCell getCell(CheckerCell c)
    {
    	return((c==null)?null:getCell(c.rackLocation(),c.col,c.row));
    }
	// pick something up.  Note that when the something is the board,
    // the board location really becomes empty, and we depend on unPickObject
    // to replace the original contents if the pick is cancelled.
    private void pickObject(CheckerCell c)
    {	G.Assert(pickedObject==null,"pickedObject should be null");
    	switch(c.rackLocation())
    	{
		default: G.Error("Not expecting rackLocation %s",c.rackLocation);
		case BoardLocation: 
			pickedObject = c.removeTop(); 
			break;
		case White_Chip_Pool:
		case Black_Chip_Pool:	
			pickedObject = c.topChip();
			break;	// don't add back to the pool
    	
    	}
    	pickedSourceStack.push(c);
   }

    //
    // in the actual game, picks are optional; allowed but redundant.
    //

    private void setNextStateAfterDrop()
    {
        switch (board_state)
        {
        default:
            G.Error("Not expecting drop in state " + board_state);
        case Confirm:
        case Draw:
        	setNextStateAfterDone(); 
        	break;
        case Play:
			setState(CheckerState.Confirm);
			break;

        case Puzzle:
			acceptPlacement();
            break;
        }
    }
    //	
    //true if col,row is the place where something was picked up.  This is used
    // by the board display to provide a visual marker where the floating chip came from.
    //
    public boolean isSource(CheckerCell c)
    {	return(getSource()==c);
    }
    public CheckerCell getSource()
    {
    	return((pickedSourceStack.size()>0) ?pickedSourceStack.top() : null);
    }
    //
    // in the actual game, picks are optional; allowed but redundant.
    //

    private void setNextStateAfterPick()
    {
        switch (board_state)
        {
        default:
            G.Error("Not expecting pick in state " + board_state);
            break;
        case Confirm:
        case Draw:
        	setState(CheckerState.Play);
        	break;
        case Play:
			break;
        case Puzzle:
            break;
        }
    }
    private void setNextStateAfterDone()
    {
       	switch(board_state)
    	{
    	default: G.Error("Not expecting state "+board_state);
    		break;
    	case Gameover: 
    		break;

        case Draw:
        	setGameOver(false,false);
        	break;
    	case Confirm:
    	case Puzzle:
    	case Play:
    		setState(CheckerState.Play);
    		break;
    	}

    }
   

    
    private void doDone()
    {	
        acceptPlacement();

        if (board_state==CheckerState.Resign)
        {	setGameOver(false,true);
        }
        else
        {	boolean win1 = WinForPlayerNow(whoseTurn);
        	boolean win2 = WinForPlayerNow(nextPlayer[whoseTurn]);
        	if(win1 || win2)  { setGameOver(win1,win2); }
        	else {setNextPlayer(); setNextStateAfterDone(); }
        }
    }
    public void unwindStack()
    {
    	while((pickedSourceStack.size()>0)||(droppedDestStack.size()>0))
    	{
    		if(pickedObject!=null) { unPickObject(); }
    		unDropObject();
    	}
    }
    public boolean Execute(commonMove mm,replayMode replay)
    {	CheckerMovespec m = (CheckerMovespec)mm;
        if(replay!=replayMode.Replay) { animationStack.clear(); }
        //System.out.println("E "+m+" for "+whoseTurn);
        switch (m.op)
        {
        case MOVE_DONE:

         	doDone();

            break;
        case MOVE_RACK_BOARD:
           	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        		case Play:
        			G.Assert(pickedObject==null,"something is moving");
                    pickObject(getCell(m.source, m.from_col, m.from_row));
                    dropObject(getCell(CheckerId.BoardLocation,m.to_col,m.to_row)); 
                    setNextStateAfterDrop();
                    break;
        	}
        	break;
        case MOVE_BOARD_BOARD:
        	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        		case Play:
        			G.Assert(pickedObject==null,"something is moving");
        			CheckerCell src = getCell(CheckerId.BoardLocation, m.from_col, m.from_row);
        			CheckerCell dest = getCell(CheckerId.BoardLocation,m.to_col,m.to_row);
        			pickObject(src);
        			dropObject(dest); 
        			if(replay!=replayMode.Replay)
        			{
        				animationStack.push(src);
        				animationStack.push(dest);
        			}
 				    setNextStateAfterDrop();
        			break;
        	}
        	break;
        case MOVE_DROPB:
			{
			CheckerCell c = getCell(CheckerId.BoardLocation, m.to_col, m.to_row);
        	G.Assert(pickedObject!=null,"something is moving");
			
            if(isSource(c)) 
            	{ 
            	  unPickObject(); 

            	} 
            	else
            		{
            		dropObject(c);
            		setNextStateAfterDrop();
            		}
			}
            break;

        case MOVE_PICKB:
        	// come here only where there's something to pick, which must
        	// be a temporary p
        	if(isDest(getCell(m.from_col,m.from_row)))
        		{ unDropObject(); 
        		  setState(CheckerState.Play);
        		}
        	else 
        		{ pickObject(getCell(CheckerId.BoardLocation, m.from_col, m.from_row));
        		  switch(board_state)
        		  {	default: G.Error("Not expecting pickb in state "+board_state);
        		  	case Play:
         		  	case Puzzle:
        		  		break;
        		  }
         		}
 
            break;

        case MOVE_DROP: // drop on chip pool;
            dropObject(getCell(m.source, m.to_col, m.to_row));
            setNextStateAfterDrop();

            break;

        case MOVE_PICK:
        	{
        	CheckerCell c = getCell(m.source, m.from_col, m.from_row);
            pickObject(c);
            setNextStateAfterPick();
        	}
            break;


        case MOVE_START:
            setWhoseTurn(m.player);
            acceptPlacement();
            unPickObject();
            // standardize the gameover state.  Particularly importing if the
            // sequence in a game is resign/start
            setState(CheckerState.Puzzle);
            {	boolean win1 = WinForPlayerNow(whoseTurn);
            	boolean win2 = WinForPlayerNow(nextPlayer[whoseTurn]);
            	if(win1 || win2) { setGameOver(win1,win2); }
            	else
            	{  setNextStateAfterDone(); 
            	}
            }
            break;

        case MOVE_RESIGN:
        	setState(unresign==null?CheckerState.Resign:unresign);
            break;
       case MOVE_RESET:
    	   	if(unresign!=null) { setState(unresign); }
        	switch(board_state)
        	{
        	case Puzzle: 
        		unPickObject();
        		break;
        	default:
       		  unwindStack();
   			  setState(CheckerState.Play); 
   			  break;
        	case Gameover:
        		break;
        	}
        	break;
        case MOVE_EDIT:
    		acceptPlacement();
            setWhoseTurn(FIRST_PLAYER_INDEX);
            // standardize "gameover" is not true
            setState(CheckerState.Puzzle);
 
            break;

        default:
            G.Error("Can't execute " + m);
        }

 
        return (true);
    }

    // legal to hit the chip storage area
    public boolean LegalToHitChips(int player)
    {
        switch (board_state)
        {
        default:
            G.Error("Not expecting state " + board_state);
         case Confirm:
         case Draw:
         case Play: 
        	return((pickedObject==null)
        			?(player==whoseTurn)
        			:((droppedDestStack.size()==0) 
        					&& (pickedSourceStack.top().onBoard==false)
        					&&(player==pickedObject.chipNumber())));


		case Gameover:
			return(false);
        case Puzzle:
        	return((pickedObject==null)?true:(player==pickedObject.chipNumber()));
        }
    }
  
    // true if it's legal to drop something  originating from fromCell on toCell
    public boolean LegalToDropOnBoard(CheckerCell fromCell,CheckerChip chip,CheckerCell toCell)
    {	
		return(false);

    }
    public boolean LegalToHitBoard(CheckerCell cell)
    {	
        switch (board_state)
        {
 		case Play:
			return(LegalToDropOnBoard(pickedSourceStack.top(),pickedObject,cell));

		case Gameover:
			return(false);
		case Confirm:
		case Draw:
			return(isDest(cell));
        default:
            G.Error("Not expecting state " + board_state);
        case Puzzle:
        	return(pickedObject==null?(cell.chipIndex>0):true);
        }
    }
  public boolean canDropOn(CheckerCell cell)
  {		CheckerCell top = (pickedObject!=null) ? pickedSourceStack.top() : null;
  		return((pickedObject!=null)				// something moving
  			&&(top.onBoard 			// on the main board
  					? (cell!=top)	// dropping on the board, must be to a different cell 
  					: (cell==top))	// dropping in the rack, must be to the same cell
  				);
  }
 
 /** assistance for the robot.  In addition to executing a move, the robot
    requires that you be able to undo the executetion.  The simplest way
    to do this is to record whatever other information is needed before
    you execute the move.  It's also convenient to automatically supply
    the "done" confirmation for any moves that are not completely self
    executing.
    */
    public void RobotExecute(CheckerMovespec m)
    {
        m.state = board_state; //record the starting state. The most reliable
        // to undo state transistions is to simple put the original state back.
        
        G.Assert(m.player == whoseTurn, "whoseturn doesn't agree");

        if (Execute(m,replayMode.Replay))
        {
            if (m.op == MOVE_DONE)
            {
            }
            else if (DoneState())
            {
                doDone();
            }
            else
            {
                G.Error("Robot move should be in a done state");
            }
        }
    }
 

   //
    // un-execute a move.  The move should only be unexecuted
    // in proper sequence.  This only needs to handle the moves
    // that the robot might actually make.
    //
    public void UnExecute(CheckerMovespec m)
    {
        //System.out.println("U "+m+" for "+whoseTurn);

        switch (m.op)
        {
        case MOVE_START:
        case MOVE_PICK:
        case MOVE_DROP:
        case MOVE_RESET:
        case MOVE_EDIT: // robot never does these
   	    default:
            G.Error("Can't unexecute " + m);

        case MOVE_DONE:
            break;
        case MOVE_RACK_BOARD:
           	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        		case Gameover:
        		case Play:
        			G.Assert(pickedObject==null,"something is moving");
        			pickObject(getCell(CheckerId.BoardLocation,m.to_col,m.to_row));
        			dropObject(getCell(m.source,m.from_col, m.from_row));
       			    acceptPlacement();
                    break;
        	}
        	break;
        case MOVE_BOARD_BOARD:
        	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        			break;
        		case Gameover:
        		case Play:
        			G.Assert(pickedObject==null,"something is moving");
        			pickObject(getCell(CheckerId.BoardLocation, m.to_col, m.to_row));
       			    dropObject(getCell(CheckerId.BoardLocation, m.from_col,m.from_row)); 
       			    acceptPlacement();
        			break;
        	}
        	break;
        case MOVE_RESIGN:
            break;
        }
        setState(m.state);
        if(whoseTurn!=m.player)
        {  	moveNumber--;
        	setWhoseTurn(m.player);
        }
 }

 OStack<commonMove> GetListOfMoves()
 {	OStack<commonMove> all = new OStack<commonMove>(commonMove.class);
 	G.Error("Not implemented");
  	return(all);
 }
 
}
