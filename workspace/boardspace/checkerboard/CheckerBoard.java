package checkerboard;

import online.common.*;
import online.game.*;

import java.util.*;


/**
 * CheckerBoard knows all about the game of Truchet, which is played
 * on a 7x7 board. It gets a lot of logistic support from 
 * common.rectBoard, which knows about the coordinate system.  
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
 *  restrictive about what to allow in each state, and have a lot of tripwires to
 *  catch unexpected transitions.   We expect to be fed only legal moves, but mistakes
 *  will be made and it's good to have the maximum opportunity to catch the unexpected.
 *  
 * Note that none of this class shows through to the game controller.  It's purely
 * a private entity used by the viewer and the robot.
 * 
 * @author ddyer
 *
 */

class CheckerBoard extends rectBoard implements BoardProtocol,CheckerConstants
{
    public int boardColumns = DEFAULT_COLUMNS;	// size of the board
    public int boardRows = DEFAULT_ROWS;
    public void SetDrawState() { setBoardState(DRAW_STATE); }
    public CheckerCell rack[] = null;
    //
    // private variables
    //
    private int playerColor[]={White_Chip_Pool,Black_Chip_Pool};
 	public int getPlayerColor(int p) { return(playerColor[p]); }
	
   public int chips_on_board[] = new int[2];			// number of chips currently on the board
    
    // intermediate states in the process of an unconfirmed move should
    // be represented explicitly, so unwinding is easy and reliable.
    public CheckerChip pickedObject = null;
    private CheckerCell pickedSource = null;
    private CheckerCell droppedDest = null;
    
    public CheckerCell getCell(char col,int row)
    {	return((CheckerCell)GetBoardCell(col,row));
    }

	// factory method
	public cell newcell(char c,int r)
	{	return(new CheckerCell(c,r));
	}
    public CheckerBoard(String init,int key) // default constructor
    {   drawing_style = STYLE_NOTHING; // STYLE_CELL or STYLE_LINES
    	Grid_Style = CHECKERGRIDSTYLE; //coordinates left and bottom
        doInit(init,key); // do the initialization 
     }



    // standared init for Hex.  Presumably there could be different
    // initializations for variation games.
    private void Init_Standard(String game)
    { 	if(Checker_INIT.equals(game)) 
    		{ boardColumns=DEFAULT_COLUMNS; 
    		  boardRows = DEFAULT_ROWS;
    		}
    	else { G.Error("No init named "+game); }
        gametype = game;
        setBoardState(PUZZLE_STATE);
        initBoard(boardColumns,boardRows); //this sets up the board and cross links
        
        // fill the board with the background tiles
        for(CheckerCell c = (CheckerCell)allCells; c!=null; c=(CheckerCell)c.next)
        {  int i = (c.row+c.col)%2;
           c.addChip(CheckerChip.getTile(i));
        }
        
        whoseTurn = FIRST_PLAYER_INDEX;
		playerColor[FIRST_PLAYER_INDEX]=White_Chip_Pool;
		playerColor[SECOND_PLAYER_INDEX]=Black_Chip_Pool;
        for(int i=FIRST_PLAYER_INDEX;i<=SECOND_PLAYER_INDEX; i++)
        {
        chips_on_board[i] = 0;
        }

    }
	public void sameboard(BoardProtocol f) { sameboard((CheckerBoard)f); }

    /**
     * Robots use this to verify a copy of a board.  If the copy method is
     * implemented correctly, there should never be a problem.  This is mainly
     * a bug trap to see if BOTH the copy and sameboard methods agree.
     * @param from_b
     */
    public void sameboard(CheckerBoard from_b)
    {

        for (int i = 0; i < win.length; i++)
        {
            G.Assert(win[i] == from_b.win[i], "Win[] matches");
			G.Assert(playerColor[i]==from_b.playerColor[i],"Player colors match");
			G.Assert(chips_on_board[i]==from_b.chips_on_board[i],"chip count matches");
        }
        for(CheckerCell c = (CheckerCell)allCells,d=(CheckerCell)from_b.allCells;
        	c!=null;
        	c=(CheckerCell)c.next,d=(CheckerCell)d.next)
        {	G.Assert(c.sameCell(d),"cells match");
        }
 
        // here, check any other state of the board to see if
        G.Assert((whoseTurn == from_b.whoseTurn) &&
            (board_state == from_b.board_state) &&
            (moveNumber == from_b.moveNumber) &&
            (resign_planned == from_b.resign_planned), "Boards not the same");
        
        // this is a good overall check that all the copy/check/digest methods
        // are in sync, although if this does fail you'll no doubt be at a loss
        // to explain why.
        G.Assert(Digest()==from_b.Digest(),"Digest matches");

    }

    /** 
     * Digest produces a 32 bit hash of the game state.  This is used 3 different
     * ways in the system.
     * (1) This is used in fraud detection to see if the same game is being played
     * over and over. Each game in the database contains a digest of the final
     * state of the game, and a midpoint state of the game. Other site machinery
     *  looks for duplicate digests.  
     * (2) Digests are also used as the game is played to look for draw by repetition.  The state
     * after most moves is recorded in a hashtable, and duplicates/triplicates are noted.
     * (3) Digests are used by the search machinery as a check on the robot's winding/unwinding
     * of the board position, this is mainly a debug/development function, but a very useful one.
     * @return
     */
   public int Digest()
    {
        int v = 0;

        // the basic digestion technique is to xor a bunch of random numbers. The key
        // trick is to always generate exactly the same sequence of random numbers, and
        // xor some subset of them.  Note that if you tweak this, all the existing
        // digests are invalidated.
        //
        Random r = new Random(64 * 1000); // init the random number generator
        
		int c1 = r.nextInt();
		int c2 = r.nextInt();
		switch(playerColor[0])
		{
		default: G.Error("Not expecting playerColor[0]="+playerColor[0]);
			break;
		case Black_Chip_Pool: v^= c1; break;
		case White_Chip_Pool: v^= c2; break;
		}
		
		for(CheckerCell c = (CheckerCell)allCells; c!=null; c=(CheckerCell)c.next)
		{	v ^= c.Digest(r);
		}
        // for most games, we should also digest whose turn it is
		int v0 = r.nextInt();
		int v1 = r.nextInt();
		v ^= whoseTurn==0 ? v0 : v1;	// player to move

        return (v);
    }
   public BoardProtocol cloneBoard() 
	{ CheckerBoard copy = new CheckerBoard(gametype,randomKey);
	  copy.clone(this); 
	  return(copy);
	}

    /* make a copy of a board.  This is used by the robot to get a copy
     * of the board for it to manupulate and analyze without affecting 
     * the board that is being displayed.
     *  */
    public void clone(CheckerBoard from_board)
    {
        CheckerBoard from_b = from_board;
        G.Assert(from_b != this, "can clone from myself");
        doInit(from_b.gametype,from_b.randomKey);
        
        whoseTurn = from_b.whoseTurn;
        board_state = from_b.board_state;
        moveNumber = from_b.moveNumber;
		for(int i=0;i<2;i++) 
		{  win[i] = from_b.win[i];
		   playerColor[i]=from_b.playerColor[i];
		   chips_on_board[i]=from_b.chips_on_board[i];
		}
		for(CheckerCell dest=(CheckerCell)allCells,src=(CheckerCell)from_board.allCells;
			dest!=null;
			dest=(CheckerCell)dest.next,src=(CheckerCell)src.next)
		{ dest.copyFrom(src);
		}
        resign_planned = from_b.resign_planned;
        
        
        sameboard(from_b);
    }

    /* initialize a board back to initial empty state */
    public void doInit(String gtype,int key)
    {	randomKey = key;	// not used, but for reference in this demo game
    	rack = new CheckerCell[CheckerChip.N_STANDARD_CHIPS*2];
     	for(int i=0,pl=FIRST_PLAYER_INDEX;i<CheckerChip.N_STANDARD_CHIPS*2; i++,pl=nextPlayer[pl])
    	{
       	CheckerCell cell = new CheckerCell();
       	cell.rackLocation=RackLocation[i];
       	cell.addChip(CheckerChip.getChip(i));
    	rack[i]=cell;
     	}    
       Init_Standard(gtype.toLowerCase());
 
        win[0] = win[1] = false;
        resign_planned = false;
        moveNumber = 1;

        // note that firstPlayer is NOT initialized here
    }

    public void setWhoseTurn(int who)
    {	
        whoseTurn = (who<0)?FIRST_PLAYER_INDEX:who;
    }

    public void togglePlayer()
    {
        setWhoseTurn(nextPlayer[whoseTurn]);
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
        case PUZZLE_STATE:
            break;
        case CONFIRM_STATE:
        case DRAW_STATE:
        case RESIGN_STATE:
            moveNumber++; //the move is complete in these states
			togglePlayer();
            return;
        }
    }

    /** this is used to determine if the "Done" button in the UI is live
     *
     * @return
     */
    public boolean DoneState()
    {	if(resign_planned) { return(true); }
        switch (board_state)
        {
         case CONFIRM_STATE:
         case DRAW_STATE:
            return (true);

        default:
            return (false);
        }
    }


   public boolean uniformRow(int player,CheckerCell cell,int dir)
    {	for(CheckerCell c=(CheckerCell)cell.exitToward(dir); c!=null; c=(CheckerCell)c.exitToward(dir))
    	{    CheckerChip cup = c.topChip();
    	     if(cup==null) { return(false); }	// empty cell
    	     if(cup.chipNumber()!=player) { return(false); }	// cell covered by the other player
    	}
    	return(true);
    }
    void setGameOver(boolean winCurrent,boolean winNext)
    {	if(winCurrent && winNext) { winCurrent=false; } // simultaneous win is a win for player2
    	win[whoseTurn]=winCurrent;
    	win[nextPlayer[whoseTurn]]=winNext;
    	setBoardState(GAMEOVER_STATE);
    }
    
    public boolean WinForPlayerNow(int player)
    {	// return true if the conditions for a win exist for player right now
    	if(board_state==GAMEOVER_STATE) { return(win[player]); }
    	G.Error("not implemented");
    	return(false);
    }
    // look for a win for player.  This algorithm should work for Gobblet Jr too.
    public double ScoreForPlayer(int player,boolean print,double cup_weight,double ml_weight,boolean dumbot)
    {  	double finalv=0.0;
    	G.Error("not implemented");
    	return(finalv);
    }


    //
    // return true if balls[rack][ball] should be selectable, meaning
    // we can pick up a ball or drop a ball there.  movingBallColor is 
    // the ball we would drop, or -1 if we want to pick up
    //
    public void acceptPlacement()
    {	
        pickedObject = null;
        droppedDest = null;
        pickedSource = null;
     }
    //
    // undo the drop, restore the moving object to moving status.
    //
    private void unDropObject()
    {
    CheckerCell dr = droppedDest;
    if(dr!=null)
    	{
    	droppedDest = null;
    	dr.removeChip(pickedObject);
    	}
    }
    // 
    // undo the pick, getting back to base state for the move
    //
    private void unPickObject()
    {	CheckerChip po = pickedObject;
    	if(po!=null)
    	{
    	CheckerCell ps = pickedSource;
    	pickedSource=null;
    	pickedObject = null;
    	ps.addChip(po);
    	}
     }
    // 
    // drop the floating object.
    //
    private void dropObject(int dest, char col, int row)
    {
       G.Assert((pickedObject!=null)&&(droppedDest==null),"ready to drop");
       switch (dest)
        {
        default: G.Error("Not expecting dest "+dest);
        	break;
        case BoardLocation: // an already filled board slot.
        	droppedDest = getCell(col,row);
        	droppedDest.addChip(pickedObject);
        	break;
        case Black_Chip_Pool:		// back in the pool
        case White_Chip_Pool:		// back in the pool
        	acceptPlacement();
            break;
        }
    }
    //
    // true if col,row is the place where something was dropped and not yet confirmed.
    // this is used to mark the one square where you can pick up a marker.
    //
    public boolean isDest(CheckerCell cell)
    {	return(droppedDest==cell);
    }
    
	//get the index in the image array corresponding to movingObjectChar 
    // or HitNoWhere if no moving object.  This is used to determine what
    // to draw when tracking the mouse.  
    // Caution! This method is called in the mouse process
    public int movingObjectIndex()
    {	CheckerChip ch = pickedObject;
    	if((ch!=null)&&(droppedDest==null))
    		{ return(ch.chipNumber());
    		}
        return (HitNoWhere);
    }
    
	// pick something up.  Note that when the something is the board,
    // the board location really becomes empty, and we depend on unPickObject
    // to replace the original contents if the pick is cancelled.
    private void pickObject(int source, char col, int row)
    {	G.Assert((pickedObject==null)&&(pickedSource==null),"ready to pick");
    
        switch (source)
        {
        default:
            G.Error("Not expecting source " + source);
            break;
        case BoardLocation:
         	{
        	CheckerCell c = pickedSource = getCell(col,row);
        	pickedObject = c.removeChip();
         	break;
         	}
        case White_Chip_Pool:
        	{
       		CheckerCell c = pickedSource = rack[White_Chip_Index];
       		pickedObject = c.topChip();
        	break;
        	}
        case Black_Chip_Pool:
        	{
       		CheckerCell c = pickedSource = rack[Black_Chip_Index];
       		pickedObject = c.topChip();
        	break;
        	}
        }
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
        case CONFIRM_STATE:
        case DRAW_STATE:
        	if(droppedDest!=null) { setNextStateAfterDone(); }
        	break;
        case PLAY_STATE:
			setBoardState(CONFIRM_STATE);
			break;

        case PUZZLE_STATE:
			acceptPlacement();
            break;
        }
    }
    //	
    //true if col,row is the place where something was picked up.  This is used
    // by the board display to provide a visual marker where the floating chip came from.
    //
    public boolean isSource(char col, int row)
    {
        return ((pickedSource!=null) 
        		&& (pickedSource.col == col) 
        		&& (pickedSource.row == row));
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
        case CONFIRM_STATE:
        case DRAW_STATE:
        	setBoardState(PLAY_STATE);
        	break;
        case PLAY_STATE:
			break;
        case PUZZLE_STATE:
            break;
        }
    }
    private void setNextStateAfterDone()
    {
       	switch(board_state)
    	{
    	default: G.Error("Not expecting state "+board_state);
    		break;
    	case GAMEOVER_STATE: 
    		break;

        case DRAW_STATE:
        	setGameOver(false,false);
        	break;
    	case CONFIRM_STATE:
    	case PUZZLE_STATE:
    	case PLAY_STATE:
    		setBoardState(PLAY_STATE);
    		break;
    	}

    }
   

    
    private void doDone()
    {	
        acceptPlacement();

        if (resign_planned)
        {	setGameOver(false,true);
        }
        else
        {	boolean win1 = WinForPlayerNow(whoseTurn);
        	boolean win2 = WinForPlayerNow(nextPlayer[whoseTurn]);
        	if(win1 || win2)  { setGameOver(win1,win2); }
        	else {setNextPlayer(); setNextStateAfterDone(); }
        }
    }

    public boolean Execute(commonMove mm)
    {	CheckerMovespec m = (CheckerMovespec)mm;
        boolean next_rp = false;

        //System.out.println("E "+m+" for "+whoseTurn);
        switch (m.op)
        {
        case MOVE_DONE:

         	doDone();

            break;
        case MOVE_RACK_BOARD:
           	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        		case PLAY_STATE:
        			G.Assert((pickedObject==null)&&(droppedDest==null),"something is moving");
                    pickObject(m.source, m.from_col, m.from_row);
                    dropObject(BoardLocation,m.to_col,m.to_row); 
                    setNextStateAfterDrop();
                    break;
        	}
        	break;
        case MOVE_BOARD_BOARD:
        	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        		case PLAY_STATE:
        			G.Assert((pickedObject==null)&&(droppedDest==null),"something is moving");
        			pickObject(BoardLocation, m.from_col, m.from_row);
        			dropObject(BoardLocation,m.to_col,m.to_row); 
 				    setNextStateAfterDrop();
        			break;
        	}
        	break;
        case MOVE_DROPB:
        	G.Assert(pickedObject!=null,"something is moving");
			switch(board_state)
			{ default: G.Error("Not expecting drop in state "+board_state);
			  case PUZZLE_STATE:  break;
		      case DRAW_STATE:
			  case CONFIRM_STATE: unDropObject(); unPickObject(); break;
			  case PLAY_STATE:

				  break;
			}
            dropObject(BoardLocation, m.to_col, m.to_row);
            if(pickedSource==droppedDest) 
            	{ unDropObject(); 
            	  unPickObject(); 

            	} 
            	else
            		{
            		setNextStateAfterDrop();
            		}

            break;

        case MOVE_PICKB:
        	// come here only where there's something to pick, which must
        	// be a temporary p
        	if(isDest(getCell(m.from_col,m.from_row)))
        		{ unDropObject(); 
        		  setBoardState(PLAY_STATE);
        		}
        	else 
        		{ pickObject(BoardLocation, m.from_col, m.from_row);
        			// if you pick up a gobblet and expose a row of 4, you lose immediately
        		  switch(board_state)
        		  {	default: G.Error("Not expecting pickb in state "+board_state);
        		  	case PLAY_STATE:
        		  		// if we pick a piece off the board, we might expose a win for the other player
        		  		// and otherwise, we are comitted to moving the piece
         		  		break;
        		  	case PUZZLE_STATE:
        		  		break;
        		  }
         		}
 
            break;

        case MOVE_DROP: // drop on chip pool;
            dropObject(m.source, m.to_col, m.to_row);
            setNextStateAfterDrop();

            break;

        case MOVE_PICK:
            unDropObject();
            unPickObject();
            pickObject(m.source, m.from_col, m.from_row);
            setNextStateAfterPick();
            break;


        case MOVE_START:
            setWhoseTurn(m.player);
            acceptPlacement();
            unPickObject();
            // standardize the gameover state.  Particularly importing if the
            // sequence in a game is resign/start
            setBoardState(PUZZLE_STATE);
            {	boolean win1 = WinForPlayerNow(whoseTurn);
            	boolean win2 = WinForPlayerNow(nextPlayer[whoseTurn]);
            	if(win1 || win2) { setGameOver(win1,win2); }
            	else
            	{  setNextStateAfterDone(); 
            	}
            }
            break;

        case MOVE_RESIGN:
            next_rp = !resign_planned;
            // fall into reset
       case MOVE_RESET:
        	switch(board_state)
        	{
        	case PUZZLE_STATE: 
        		if(droppedDest!=null) { acceptPlacement(); }
        		unPickObject();
        		break;
        	default:
       		  unDropObject(); 
   			  setBoardState(PLAY_STATE); 
   			  break;
        	case GAMEOVER_STATE:
        		break;
        	}
        	break;
        case MOVE_EDIT:
    		acceptPlacement();
            setWhoseTurn(FIRST_PLAYER_INDEX);
            // standardize "gameover" is not true
            setBoardState(PUZZLE_STATE);
 
            break;

        default:
            G.Error("Can't execute " + m);
        }

        resign_planned = next_rp;

        return (true);
    }

    // legal to hit the chip storage area
    public boolean LegalToHitChips(int player)
    {
        switch (board_state)
        {
        default:
            G.Error("Not expecting state " + board_state);
         case CONFIRM_STATE:
         case DRAW_STATE:
         case PLAY_STATE: 
        	return((pickedObject==null)
        			?(player==whoseTurn)
        			:((droppedDest==null) 
        					&& (pickedSource.onBoard==false)
        					&&(player==pickedObject.chipNumber())));


		case GAMEOVER_STATE:
			return(false);
        case PUZZLE_STATE:
        	return((pickedObject==null)?true:(player==pickedObject.chipNumber()));
        }
    }
  
    // true if it's legal to drop gobblet  originating from fromCell on toCell
    public boolean LegalToDropOnBoard(CheckerCell fromCell,CheckerChip gobblet,CheckerCell toCell)
    {	
		return(false);

    }
    public boolean LegalToHitBoard(CheckerCell cell)
    {	
        switch (board_state)
        {
 		case PLAY_STATE:
			return(LegalToDropOnBoard(pickedSource,pickedObject,cell));

		case GAMEOVER_STATE:
			return(false);
		case CONFIRM_STATE:
		case DRAW_STATE:
			return(isDest(cell));
        default:
            G.Error("Not expecting state " + board_state);
        case PUZZLE_STATE:
        	return(pickedObject==null?(cell.chipIndex>0):true);
        }
    }
  public boolean canDropOn(CheckerCell cell)
  {	
  	return((pickedObject!=null)				// something moving
  			&&(pickedSource.onBoard 
  					? (cell!=pickedSource)	// dropping on the board, must be to a different cell 
  					: (cell==pickedSource))	// dropping in the rack, must be to the same cell
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

        if (Execute(m))
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
        		case GAMEOVER_STATE:
        		case PLAY_STATE:
        			G.Assert((pickedObject==null)&&(droppedDest==null),"something is moving");
        			pickObject(BoardLocation,m.to_col,m.to_row);
        			dropObject(m.source,m.from_col, m.from_row);
       			    acceptPlacement();
                    break;
        	}
        	break;
        case MOVE_BOARD_BOARD:
        	switch(board_state)
        	{	default: G.Error("Not expecting robot in state "+board_state);
        			break;
        		case GAMEOVER_STATE:
        		case PLAY_STATE:
        			G.Assert((pickedObject==null)&&(droppedDest==null),"something is moving");
        			pickObject(BoardLocation, m.to_col, m.to_row);
       			    dropObject(BoardLocation, m.from_col,m.from_row); 
       			    acceptPlacement();
        			break;
        	}
        	break;
        case MOVE_RESIGN:
            break;
        }
        resign_planned = false;
        win[FIRST_PLAYER_INDEX]=win[SECOND_PLAYER_INDEX]=false; 
        setBoardState(m.state);
        if(whoseTurn!=m.player)
        {  	moveNumber--;
        	setWhoseTurn(m.player);
        }
 }

 Vector GetListOfMoves()
 {	Vector all = new Vector();
 	G.Error("Not implemented");
  	return(all);
 }
 
}
