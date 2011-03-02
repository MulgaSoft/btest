package hex;

import online.common.*;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import online.game.*;
/**
 * HexGameBoard knows all about the game of Hex, which is played
 * on a heagonal board. It gets a lot of logistic support from 
 * common.hexBoard, which knows about the coordinate system.  
 * 
 * This class doesn't do any graphics or know about anything graphical, 
 * but it does know about states of the game that should be reflected 
 * in the graphics.
 * 
 *  The principle interface with the game viewer is the "Execute" method
 *  which processes moves. 
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

class HexGameBoard extends hexBoard<hexCell> implements BoardProtocol,HexConstants
{	static final boolean debug = false;
    //
    // private variables
    //
    private int playerColor[]={White_Chip_Pool,Black_Chip_Pool};
    private hexChip playerChip[]={hexChip.White,hexChip.Black};
    // get the chip pool and chip associated with a player.  these are not 
    // constants because of the swap rule.
	public hexChip getPlayerChip(int p) { return(playerChip[p]); }
	public int getPlayerColor(int p) { return(playerColor[p]); }
// this is required even though it is meaningless for Hex, but possibly important
// in other games.  When a draw by repetition is detected, this function is called.
// the game should have a "draw pending" state and enter it now, pending confirmation
// by the user clicking on done.   If this mechanism is triggered unexpectedly, it
// is probably because the move editing in "editHistory" is not removing last-move
// dithering by the user, or the "Digest()" method is not returning unique results
// other parts of this mechanism: the Viewer ought to have a "repRect" and call
// DrawRepRect to warn the user that repetitions have been seen.
	public void SetDrawState() {G.Error("not expected"); };	
	
    private int chips_on_board = 0;			// number of chips currently on the board
    private int sweep_counter=0;			// used when scanning for blobs
    
    // intermediate states in the process of an unconfirmed move should
    // be represented explicitly, so unwinding is easy and reliable.
    public hexChip pickedObject = null;
    public hexChip lastPicked = null;
    private hexCell blackChipPool = new hexCell();	// dummy source for the chip pools
    private hexCell whiteChipPool = new hexCell();
    private hexCell pickedSource = null; 
    private hexCell droppedDest = null;
    public Object lastDroppedObject = null;	// for image adjustment logic

    
	
	// factory method to generate a board cell
	public hexCell newcell(char c,int r)
	{	return(new hexCell(c,r));
	}
    public HexGameBoard(String init) // default constructor
    {
        drawing_style = STYLE_NOTHING; // don't draw the cells.  STYLE_CELL to draw them
        Grid_Style = HEXGRIDSTYLE;
        doInit(init); // do the initialization 
    }
    public BoardProtocol cloneBoard() 
	{ HexGameBoard dup = new HexGameBoard(gametype); 
	  dup.clone(this);
	  return(dup); 
   	}

    // precompute which border cell decorations needs to be drawn 
    // this is peculiar to the way we draw the borders of the hex board
    // not a general game requirement.
    private void setBorderDirections()
    {	for(hexCell c = allCells;
    		c!=null;
    		c = c.next)
    	{
    	int bd = 0;
        for(int direction=0;direction<6;direction++)
        {		hexCell border0 = c.exitTo(direction);
        		hexCell border1 = c.exitTo(direction+1); 
        		// this is a little complex because the corner cells
        		// are part of two borders.
        		if((border0==null) && (border1==null))
        		{	bd |= (1<<BorderPairIndex[direction]);
        		}
        	}
    	c.borders = bd;
    	}
    }
    // standared init for Hex.  Presumably there could be different
    // initializations for variation games.
    private void Init_Standard(String game)
    {	int[] firstcol = null;
    	int[] ncol = null;
    	if(Hex_INIT.equals(game)) { firstcol = ZfirstInCol; ncol = ZnInCol; }
    	else if(Hex_15_INIT.equals(game)) { firstcol = ZfirstInCol15; ncol = ZnInCol15; }
    	else if(Hex_19_INIT.equals(game)) { firstcol = ZfirstInCol19; ncol = ZnInCol19; }
    	else { G.Error("No init named "+game); }
        gametype = game;
        setBoardState(PUZZLE_STATE);
        initBoard(firstcol, ncol, null); //this sets up the hex board
        
      	setBorderDirections();	// mark the border cells for use in painting
        
        whoseTurn = FIRST_PLAYER_INDEX;
        chips_on_board = 0;
        droppedDest = null;
        pickedSource = null;
        lastDroppedObject = null;

		playerColor[0]=White_Chip_Pool;
		playerColor[1]=Black_Chip_Pool;
		playerChip[0]=hexChip.White;
		playerChip[1]=hexChip.Black;
        // set the initial contents of the board to all empty cells
		for(hexCell c = allCells; c!=null; c=c.next) { c.chip=null; }
    }
    public void sameboard(BoardProtocol f) { sameboard((HexGameBoard)f); }

    /**
     * Robots use this to verify a copy of a board.  If the copy method is
     * implemented correctly, there should never be a problem.  This is mainly
     * a bug trap to see if BOTH the copy and sameboard methods agree.
     * @param from_b
     */
    public void sameboard(HexGameBoard from_b)
    {
        super.sameboard(from_b); // hexboard compares the boards
        
        for(hexCell c = allCells,d=from_b.allCells;  
		c!=null;
		c=c.next, d= d.next)
		{G.Assert(c.sameCell(d),"cells match");
		}

        for (int i = 0; i < win.length; i++)
        {
            G.Assert(win[i] == from_b.win[i], "Win[] matches");
			G.Assert(playerColor[i]==from_b.playerColor[i],"Player colors match");
			G.Assert(playerChip[i]==from_b.playerChip[i],"Player chars must match");
        }

        // here, check any other state of the board to see if
        G.Assert((whoseTurn == from_b.whoseTurn) &&
            (board_state == from_b.board_state) &&
            (moveNumber == from_b.moveNumber) &&
            (chips_on_board == from_b.chips_on_board) &&
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
		// note we can't modernize this without invalidating all the existing
		// digests.
		for(hexCell c=allCells; c!=null; c=c.next)
		{	
            v ^= c.Digest(r);
		}

        if(resign_planned) { v^=r.nextInt(); }
        // for most games, we should also digest whose turn it is
		//int v0 = r.nextInt();
		//int v1 = r.nextInt();
		//v ^= whoseTurn==0 ? v0 : v1;	// player to mvoe
        return (v);
    }

    /* make a copy of a board.  This is used by the robot to get a copy
     * of the board for it to manipulate and analyze without affecting 
     * the board that is being displayed.
     *  */
    public void clone(HexGameBoard from_board)
    {
        HexGameBoard from_b = from_board;
        G.Assert(from_b != this, "can clone from myself");
        doInit(from_b.gametype);
        super.clone(from_b);

        chips_on_board = from_b.chips_on_board;
        whoseTurn = from_b.whoseTurn;
        board_state = from_b.board_state;
        moveNumber = from_b.moveNumber;
        droppedDest = null;
        pickedSource = null;
        pickedObject = null;
        lastPicked = null;

		for(int i=0;i<2;i++) 
		{  win[i] = from_b.win[i];
		   playerColor[i]=from_b.playerColor[i];
		   playerChip[i]=from_b.playerChip[i];
		}
        resign_planned = from_b.resign_planned;

        if(debug) { sameboard(from_b); }
    }

    /* initialize a board back to initial empty state */
    public void doInit(String gtype)
    {

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
        case CONFIRM_SWAP_STATE:
        case CONFIRM_STATE:
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
        case CONFIRM_SWAP_STATE:
        case CONFIRM_STATE:
            return (true);

        default:
            return (false);
        }
    }
    // this is the default, so we don't need it explicitly here.
    // but games with complex "rearrange" states might want to be
    // more selecteive.  This determines if the current board digest is added
    // to the repetition detection machinery.
    public boolean DigestState()
    {	if(resign_planned) { return(false); }
    	return(DoneState());
    }
    //
    // flood fill to make this blob as large as possible.  This is more elaborate
    // than it needs to be for scoring purposes, but it is also used by the robot
    //
    private void expandHexBlob(hexblob blob,hexCell cell)
    {	if(cell==null) {}
    	else if((cell.sweep_counter!=sweep_counter))
    	{
    	cell.sweep_counter = sweep_counter;
    	cell.blob = blob;
    	
     	
    	if(cell.chip==blob.color)
    	  {
    	   	blob.addCell(cell);
    	   	for(int dir = 0; dir<6; dir++)
    		{	expandHexBlob(blob,cell.exitTo(dir));
    		}
    	  }
    	}
    	else if(cell.chip==null)
    	{	// cell was previously encountered on this sweep
    		hexblob other = cell.blob;
    		if((other!=blob)&&(other.color==blob.color))
    		{	// a connection
    			other.addConnection(blob,cell);
    			blob.addConnection(other,cell);
    		}
    	}
    }
    
    OStack<hexblob> findBlobs(int forplayer,OStack<hexblob> all)
    {	sweep_counter++;
    	hexChip pch = playerChip[forplayer];
    	for(hexCell cell = allCells;  cell!=null; cell=cell.next)
    	{	if((cell.sweep_counter!=sweep_counter) && (cell.chip==pch))
    		{
    		hexblob blob = new hexblob(pch);
    		all.push(blob);
    		expandHexBlob(blob,cell);
     		}
    	}
       	return(all);
    }

    public boolean WinForPlayerNow(int player)
    {	if(win[player]) { return(true); }
    	OStack<hexblob> blobs = new OStack<hexblob>(hexblob.class);
    	return(WinForPlayerNow(player,blobs));
     }
    // this method is also called by the robot to get the blobs as a side effect
    public boolean WinForPlayerNow(int player,OStack<hexblob> blobs)
    {
     	findBlobs(player,blobs);
    	return(someBlobWins(blobs,player));
   	
    }

    public boolean someBlobWins(OStack<hexblob> blobs,int player)
    {	// if the span of any blobs is the whole board, we have a winner
    	// in Hex, there is only one winner.
    	for(int i=0;i<blobs.size(); i++)
    	{	hexblob blob = blobs.elementAt(i);
    		int span = blob.span();
    		if(span==ncols)
    		{ return(true); }
     	}
        return (false);
    }


    
    // set the contents of a cell, and maintian the books
    public hexChip SetBoard(hexCell c,hexChip ch)
    {	hexChip old = c.topChip();
    	if(c.onBoard)
    	{
    	if(old!=null) { chips_on_board--; }
     	if(ch!=null) { chips_on_board++; }
    	}
       	c.chip = ch;
    	return(old);
    }
    //
    // accept the current placements as permanant
    //
    public void acceptPlacement()
    {	if(droppedDest!=null)
    	{
        droppedDest = null;
        pickedSource = null;
    	}
     }
    //
    // undo the drop, restore the moving object to moving status.
    //
    private void unDropObject()
    {
    	if(droppedDest!=null) 
    	{	pickedObject = droppedDest.topChip();
    		SetBoard(droppedDest,null); 
    		droppedDest = null;
     	}
    }
    // 
    // undo the pick, getting back to base state for the move
    //
    private void unPickObject()
    {	if(pickedSource!=null) 
    		{ SetBoard(pickedSource,pickedObject);
    		  pickedSource = null;
    		}
		  pickedObject = null;
  }
    // 
    // drop the floating object.
    //
    private void dropObject(Hexmovespec m)
    {
       pickedSource = null;
       switch (m.source)
        {
        default:
            G.Error("Not expecting dest " + m.source);
            break;
        case Black_Chip_Pool:
        case White_Chip_Pool:		// back in the pool, we don't really care where
        	pickedObject = null;
        	pickedSource = null;
        	droppedDest = null;
            break;
        case BoardLocation:	// already filled board slot, which can happen in edit mode
        case EmptyBoard:
           	hexCell c = getCell(m.to_col,m.to_row);
           	if(c.chip!=null) 
           		{ // this is an important bit of communication with editHistory
           		  // tell it that this drop wasn't on an empty cell
           			m.source=BoardLocation; 
           		}
           	SetBoard(c,pickedObject);
           	droppedDest = c;
            lastDroppedObject = pickedObject;
            pickedObject = null;
            break;
        }
     }
    //
    // true if c is the place where something was dropped and not yet confirmed.
    // this is used to mark the one square where you can pick up a marker.
    //
    public boolean isDest(hexCell c)
    {	return(droppedDest==c);
    }
    
	//get the index in the image array corresponding to movingObjectChar 
    // or HitNoWhere if no moving object.  This is used to determine what
    // to draw when tracking the mouse.
    // caution! this method is called in the mouse event process
    public int movingObjectIndex()
    { hexChip ch = pickedObject;
      if(ch!=null)
    	{	return(ch.chipNumber()); 
    	}
      	return (HitNoWhere);
    }
	// pick something up.  Note that when the something is the board,
    // the board location really becomes empty, and we depend on unPickObject
    // to replace the original contents if the pick is cancelled.
    private void pickObject(int source, char col, int row)
    {
        switch (source)
        {
        default:
            G.Error("Not expecting source " + source);
            break;
        case BoardLocation:
        	{
        	hexCell c = getCell(col,row);
        	boolean wasDest = isDest(c);
        	unDropObject(); 
        	if(!wasDest)
        	{
            pickedSource = c;
            lastPicked = pickedObject = c.topChip();
         	lastDroppedObject = droppedDest = null;
			c.chip = null;
        	}}
            break;

        case Black_Chip_Pool:
			if(pickedObject==hexChip.Black) { acceptPlacement(); }
			else 
			{  lastPicked = pickedObject = hexChip.Black;
			}
		   pickedSource = blackChipPool;

            break;

        case White_Chip_Pool:
			if(pickedObject==hexChip.White) { acceptPlacement(); }
			else 
			{
            lastPicked = pickedObject = hexChip.White;
			}
            pickedSource = whiteChipPool;
            break;
        }
    }
    //	
    //true if cell is the place where something was picked up.  This is used
    // by the board display to provide a visual marker where the floating chip came from.
    //
    public boolean isSource(hexCell c)
    {	return(c==pickedSource);
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
            break;
        case CONFIRM_STATE:
        	if(droppedDest==null)
        	{setNextStateAfterDone();
        	}
        	break;
        case PLAY_STATE:
        case FIRST_PLAY_STATE:
        case PLAY_OR_SWAP_STATE:
			setBoardState(CONFIRM_STATE);
			break;
        case PUZZLE_STATE:
			acceptPlacement();
            break;
        }
    }
    private void setNextStateAfterDone()
    {
       	switch(board_state)
    	{
    	default: G.Error("Not expecting state "+board_state);
    		break;
    	case GAMEOVER_STATE: break;
    	case CONFIRM_SWAP_STATE: setBoardState(PLAY_STATE); break;
    	case CONFIRM_STATE:
    	case PUZZLE_STATE:
    	case PLAY_STATE:
    	case PLAY_OR_SWAP_STATE:
    		setBoardState((chips_on_board==1) ? PLAY_OR_SWAP_STATE : PLAY_STATE);
    		break;
    	}

    }
    private void doDone()
    {
        acceptPlacement();

        if (resign_planned)
        {
            win[nextPlayer[whoseTurn]] = true;
    		setBoardState(GAMEOVER_STATE);
        }
        else
        {	if(WinForPlayerNow(whoseTurn)) 
        		{ win[whoseTurn]=true;
        		  setBoardState(GAMEOVER_STATE); 
        		}
        	else {setNextPlayer();
        		setNextStateAfterDone();
        	}
        }
    }
void doSwap()
{ int c = playerColor[0];
	hexChip ch = playerChip[0];
	playerColor[0]=playerColor[1];
	playerChip[0]=playerChip[1];
	playerColor[1]=c;
	playerChip[1]=ch;
	switch(board_state)
	{	
	default: G.Error("Not expecting state "+board_state);
		break;
	case PLAY_OR_SWAP_STATE:
		  setBoardState(CONFIRM_SWAP_STATE);
		  break;
	case CONFIRM_SWAP_STATE:
		  setBoardState(PLAY_OR_SWAP_STATE);
		  break;
	case GAMEOVER_STATE:
	case PUZZLE_STATE: break;
	}

}
    public boolean Execute(commonMove mm)
    {	Hexmovespec m = (Hexmovespec)mm;
        boolean next_rp = false;

        //G.print("E "+m+" for "+whoseTurn+" "+board_state);
        switch (m.op)
        {
		case MOVE_SWAP:	// swap colors with the other player
			doSwap();
			break;
        case MOVE_DONE:

         	doDone();

            break;

        case MOVE_DROPB:
			switch(board_state)
			{ case PUZZLE_STATE: acceptPlacement(); break;
			  case CONFIRM_STATE: unDropObject(); unPickObject(); break;
			  case PLAY_STATE:
			  case PLAY_OR_SWAP_STATE: acceptPlacement(); break;
			}
			pickObject(m.object, m.to_col, m.to_row);
            dropObject(m);
            setNextStateAfterDrop();

            break;

        case MOVE_PICK:
            unDropObject();
            unPickObject();
            // fall through
        case MOVE_PICKB:
        	// come here only where there's something to pick, which must
        	// be a temporary p
        	pickObject(m.source, m.to_col, m.to_row);
        	switch(board_state)
        	{
        	case PUZZLE_STATE:
         		break;
        	case CONFIRM_STATE:
        		setBoardState((chips_on_board==1) ? PLAY_OR_SWAP_STATE : PLAY_STATE);
        		break;
        	default: ;
        	}
            break;

        case MOVE_DROP: // drop on chip pool;
            dropObject(m);
            //setNextStateAfterDrop();

            break;


 
        case MOVE_START:
            setWhoseTurn(m.player);
            acceptPlacement();
            unPickObject();
            int nextp = nextPlayer[whoseTurn];
            // standardize the gameover state.  Particularly importing if the
            // sequence in a game is resign/start
            setBoardState(PUZZLE_STATE);	// standardize the current state
            if((win[whoseTurn]=WinForPlayerNow(whoseTurn))
               ||(win[nextp]=WinForPlayerNow(nextp)))
               	{ setBoardState(GAMEOVER_STATE); 
               	}
            else {  setNextStateAfterDone(); }

            break;

       case MOVE_RESIGN:
            next_rp = !resign_planned;
           // fall through and be like reset
       case MOVE_RESET:
        	switch(board_state)
        	{
        	case PUZZLE_STATE: 
        		acceptPlacement();
        		unPickObject();
        		break;
        	case CONFIRM_SWAP_STATE:
        		doSwap();
        		// fall through
        	default:
        		unDropObject();
        		unPickObject();
        		setBoardState(((whoseTurn==1)&&(chips_on_board==1)) ? PLAY_OR_SWAP_STATE : PLAY_STATE);
        		break;
        	case GAMEOVER_STATE:
        		break;
        	}
        	break;
        case MOVE_EDIT:
        	acceptPlacement();
            setWhoseTurn(FIRST_PLAYER_INDEX);
            setBoardState(PUZZLE_STATE);
 
            break;

        default:
            G.Error("Can't execute " + m);
        }

        resign_planned = next_rp;
        //System.out.println("Ex "+m+" for "+whoseTurn+" "+board_state);

        return (true);
    }

    // legal to hit the chip storage area
    public boolean LegalToHitChips(int player)
    {
        switch (board_state)
        {
        default:
            G.Error("Not expecting state " + board_state);
            return(false);	// not used
        case CONFIRM_STATE:
        case PLAY_OR_SWAP_STATE:
        case FIRST_PLAY_STATE:
        case PLAY_STATE:
        	// for hex, you can pick up a stone in the storage area
        	// but it's really optional
        	return(player==whoseTurn);
		case CONFIRM_SWAP_STATE:
		case GAMEOVER_STATE:
			return(false);
        case PUZZLE_STATE:
            return ((pickedObject!=null)?(pickedObject==playerChip[player]):true);
        }
    }

    public boolean LegalToHitBoard(hexCell c)
    {	if(c==null) { return(false); }
        switch (board_state)
        {
		case PLAY_STATE:
		case FIRST_PLAY_STATE:
		case PLAY_OR_SWAP_STATE:
			return(c.chip==null);
		case CONFIRM_SWAP_STATE:
		case GAMEOVER_STATE:
			return(false);
		case CONFIRM_STATE:
			return(isDest(c) || (c.chip==null));
        default:
            G.Error("Not expecting state " + board_state);
            return(true);	// not used
        case PUZZLE_STATE:
            return (true);
        }
    }
    
    
 /** assistance for the robot.  In addition to executing a move, the robot
    requires that you be able to undo the executetion.  The simplest way
    to do this is to record whatever other information is needed before
    you execute the move.  It's also convenient to automatically supply
    the "done" confirmation for any moves that are not completely self
    executing.
    */
    public void RobotExecute(Hexmovespec m)
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
    public void UnExecute(Hexmovespec m)
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
            G.Error("Can't un execute " + m);
            break;
        case MOVE_DONE:
            break;
            
        case MOVE_SWAP:
        	setBoardState(m.state);
        	doSwap();
        	break;
        case MOVE_DROPB:
        	SetBoard(getCell(m.to_col,m.to_row),null);
        	break;
        case MOVE_RESIGN:
            resign_planned = !resign_planned;

            break;
        }

        setBoardState(m.state);
        if(whoseTurn!=m.player)
        {	moveNumber--;
        	setWhoseTurn(m.player);
        }
 }
    
 OStack<commonMove> GetListOfMoves()
 {	OStack<commonMove> all = new OStack<commonMove>(commonMove.class);
 	if(board_state==PLAY_OR_SWAP_STATE)
 	{
 		all.addElement(new Hexmovespec("swap",whoseTurn));
 	}
 	for(hexCell c = allCells;
 	    c!=null;
 	    c = c.next)
 	{	if(c.topChip()==null)
 		{all.addElement(new Hexmovespec("dropb "+playerChip[whoseTurn].colorName+" "+c.col+" "+c.row,whoseTurn));
 		}
 	}

 	return(all);
 }
 
 // small ad-hoc adjustment to the grid positions
 public void DrawGridCoord(Graphics gc, Color clt,int xpos, int ypos, int cellsize,String txt)
 {   if(Character.isDigit(txt.charAt(0)))
	 	{ xpos-=cellsize/4;
	 	}
 		else
 		{ xpos += cellsize/4; 
 		  ypos += cellsize/4;
 		}
 	G.Text(gc, false, xpos, ypos, -1, 0,clt, null, txt);
 }
// return a very simple representation of the board for the benefit of external robots
// meta-format is "game" "version" xxx
// specific format is "hex" "1" {B or W} { occupied cell }*
//
public String getStateString()
{	StringBuffer occupied = new StringBuffer();
	occupied.append("hex 1 "+ ncols+" "+hexChip.chipColor[whoseTurn]);
	for(hexCell c =allCells; c!=null; c=c.next)
	{	if(c.chip!=null) { occupied.append(" "+c.col+" "+c.row+" "+c.topChip().colorName); }
	}
	return(occupied.toString());
}
}
