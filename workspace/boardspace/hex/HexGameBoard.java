package hex;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import lib.*;
import online.game.*;
import static hex.Hexmovespec.*;

/**
 * HexGameBoard knows all about the game of Hex, which is played
 * on a hexagonal board. It gets a lot of logistic support from 
 * common.hexBoard, which knows about the coordinate system.  
 * 
 * This class doesn't do any graphics or know about anything graphical, 
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
{	static int REVISION = 100;			// 100 represents the initial version of the game
	int revision = 1;					// games with no revision information will be 100
	static final boolean debug = false;
	HexVariation variation = HexVariation.hex;
	private HexState board_state = HexState.Puzzle;	
	private HexState unresign = null;	// remembers the orignal state when "resign" is hit
	private OStack<HexState>robotState = new OStack<HexState>(HexState.class);
	
	public HexState getState() { return(board_state); }
    /**
     * this is the preferred method when using the modern "enum" style of game state
     * @param st
     */
	public void setState(HexState st) 
	{ 	unresign = (st==HexState.Resign)?board_state:null;
		board_state = st;
		if(!board_state.GameOver()) 
			{ G.setValue(win,false); 	// make sure "win" is cleared
			}
	}

    private HexId playerColor[]={HexId.White_Chip_Pool,HexId.Black_Chip_Pool};
    
    private hexChip playerChip[]={hexChip.White,hexChip.Black};
    private hexCell playerCell[]=new hexCell[2];
    // get the chip pool and chip associated with a player.  these are not 
    // constants because of the swap rule.
	public hexChip getPlayerChip(int p) { return(playerChip[p]); }
	public HexId getPlayerColor(int p) { return(playerColor[p]); }
	public hexCell getPlayerCell(int p) { return(playerCell[p]); }
// this is required even though it is meaningless for Hex, but possibly important
// in other games.  When a draw by repetition is detected, this function is called.
// the game should have a "draw pending" state and enter it now, pending confirmation
// by the user clicking on done.   If this mechanism is triggered unexpectedly, it
// is probably because the move editing in "editHistory" is not removing last-move
// dithering by the user, or the "Digest()" method is not returning unique results
// other parts of this mechanism: the Viewer ought to have a "repRect" and call
// DrawRepRect to warn the user that repetitions have been seen.
	public void SetDrawState() {G.Error("not expected"); };	
	OStack<hexCell>animationStack = new OStack<hexCell>(hexCell.class);
    private int chips_on_board = 0;			// number of chips currently on the board
    private int fullBoard = 0;				// the number of cells in the board
    private int sweep_counter=0;			// used when scanning for blobs
    private int directionWhiteHome = -1;
    private int directionBlackHome = -1;
    private boolean swapped = false;
    // intermediate states in the process of an unconfirmed move should
    // be represented explicitly, so unwinding is easy and reliable.
    public hexChip pickedObject = null;
    public hexChip lastPicked = null;
    private hexCell blackChipPool = null;	// dummy source for the chip pools
    private hexCell whiteChipPool = null;
    private hexCell pickedSource = null; 
    private hexCell droppedDest = null;
    private OStack<hexCell>emptyCells=new OStack<hexCell>(hexCell.class);
    private HexState resetState = HexState.Puzzle; 
    public Object lastDroppedObject = null;	// for image adjustment logic

	// factory method to generate a board cell
	public hexCell newcell(char c,int r)
	{	return(new hexCell(HexId.BoardLocation,c,r));
	}
	
	// constructor 
    public HexGameBoard(String init,int players,long key) // default constructor
    {
        drawing_style = DrawingStyle.STYLE_NOTHING; // don't draw the cells.  STYLE_CELL to draw them
        Grid_Style = HEXGRIDSTYLE;
        doInit(init,key,players,REVISION); // do the initialization 
    }
    
    public String gameType() { return(gametype+" "+players_in_game+" "+randomKey+" "+revision); }
    

    public void doInit(String gtype,long key)
    {
    	StringTokenizer tok = new StringTokenizer(gtype);
    	String typ = tok.nextToken();
    	int np = tok.hasMoreTokens() ? G.IntToken(tok) : players_in_game;
    	long ran = tok.hasMoreTokens() ? G.IntToken(tok) : key;
    	int rev = tok.hasMoreTokens() ? G.IntToken(tok) : revision;
    	doInit(typ,ran,np,rev);
    }
    /* initialize a board back to initial empty state */
    public void doInit(String gtype,long key,int players,int rev)
    {	randomKey = key;
    	revision = rev;
    	players_in_game = players;
 		Random r = new Random(734687);	// this random is used to assign hash values to cells, common to all games of this type.
		setState(HexState.Puzzle);
		gtype = gtype.toLowerCase();
		variation = HexVariation.findVariation(gtype);
		G.Assert(variation!=null,"No init named %s",gtype);
		robotState.clear();
		gametype = gtype;
		switch(variation)
		{
		default: G.Error("Not expecting variation %s",variation);
			break;
		case hex_19:
		case hex_15:
		case hex:
			initBoard(variation.firstInCol,variation.ZinCol,null);
		}

		allCells.setDigestChain(r);		// set the randomv for all cells on the board
 		
	    
	    blackChipPool = new hexCell(r,HexId.Black_Chip_Pool);
	    blackChipPool.chip = hexChip.Black;
	    whiteChipPool = new hexCell(r,HexId.White_Chip_Pool);
	    whiteChipPool.chip = hexChip.White;
	    playerCell[FIRST_PLAYER_INDEX] = whiteChipPool; 
	    playerCell[SECOND_PLAYER_INDEX] = blackChipPool; 
	    
	  	setBorderDirections();	// mark the border cells for use in painting
	    
	    whoseTurn = FIRST_PLAYER_INDEX;
	    chips_on_board = 0;
	    droppedDest = null;
	    pickedSource = null;
	    pickedObject = null;
	    resetState = null;
	    lastDroppedObject = null;
	    directionWhiteHome = findDirection('A',1,'A',2);
	    directionBlackHome = findDirection('A',1,'B',1);
		playerColor[0]=HexId.White_Chip_Pool;
		playerColor[1]=HexId.Black_Chip_Pool;
		playerChip[0]=hexChip.White;
		playerChip[1]=hexChip.Black;
	    // set the initial contents of the board to all empty cells
		emptyCells.clear();
		for(hexCell c = allCells; c!=null; c=c.next) { c.chip=null; emptyCells.push(c); }
		fullBoard = emptyCells.size();
		
		randomKey = key;
    
        animationStack.clear();
        swapped = false;
        moveNumber = 1;

        // note that firstPlayer is NOT initialized here
    }

    public HexGameBoard clone()
    {
    	return((HexGameBoard)cloneBoard());
    }
    /** create a copy of this board */
    public BoardProtocol cloneBoard() 
	{ HexGameBoard dup = new HexGameBoard(gametype,players_in_game,randomKey); 
	  dup.copyFrom(this);
	  return(dup); 
   	}
    public void copyFrom(BoardProtocol b) { copyFrom((HexGameBoard)b); }

    /* make a copy of a board.  This is used by the robot to get a copy
     * of the board for it to manipulate and analyze without affecting 
     * the board that is being displayed.
     *  */
    public void copyFrom(HexGameBoard from_b)
    {
        super.clone(from_b);
        revision = from_b.revision;
        chips_on_board = from_b.chips_on_board;
        fullBoard = from_b.fullBoard;
        robotState.copyFrom(from_b.robotState);
        getCell(emptyCells,from_b.emptyCells);
        unresign = from_b.unresign;
        board_state = from_b.board_state;
        droppedDest = null;
        pickedSource = getCell(from_b.pickedSource);
        pickedObject = from_b.pickedObject;
        resetState = from_b.resetState;
        lastPicked = null;

        G.copy(playerColor,from_b.playerColor);
        G.copy(playerChip,from_b.playerChip);
 
        sameboard(from_b); 
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
        super.sameboard(from_b); // // calls sameCell for each cell, also for inherited class variables.
        G.Assert(unresign==from_b.unresign,"unresign mismatch");
        G.Assert(variation==from_b.variation,"variation matches");
        G.Assert(G.sameArrayContents(win,from_b.win),"win mismatch");
        G.Assert(G.sameArrayContents(playerColor,from_b.playerColor),"playerColor mismatch");
        G.Assert(G.sameArrayContents(playerChip,from_b.playerChip),"playerChip mismatch");
        G.Assert(pickedObject==from_b.pickedObject, "picked Object mismatch");
        G.Assert(chips_on_board == from_b.chips_on_board,"chips_on_board mismatch");
 

        // this is a good overall check that all the copy/check/digest methods
        // are in sync, although if this does fail you'll no doubt be at a loss
        // to explain why.
        G.Assert(Digest()==from_b.Digest(),"Digest matches");

    }

    /** 
     * Digest produces a 64 bit hash of the game state.  This is used in many different
     * ways to identify "same" board states.  Some are relevant to the ordinary operation
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
        // the basic digestion technique is to xor a bunch of random numbers. 
    	// many object have an associated unique random number, including "chip" and "cell"
    	// derivatives.  If the same object is digested more than once (ie; once as a chip
    	// in play, and once as the chip currently "picked up", then it must be given a
    	// different identity for the second use.
        //
        Random r = new Random(64 * 1000); // init the random number generator
        long v = super.Digest();

		// many games will want to digest pickedSource too
		// v ^= cell.Digest(r,pickedSource);
		v ^= chip.Digest(r,playerChip[0]);	// this accounts for the "swap" button
		v ^= chip.Digest(r,pickedObject);
		v ^= Digest(r,pickedSource);
		v ^= r.nextLong()*(board_state.ordinal()*10+whoseTurn);
        return (v);
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
        case ConfirmSwap:
        case Confirm:
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
    {	return(board_state.doneState());
    }
    // this is the default, so we don't need it explicitly here.
    // but games with complex "rearrange" states might want to be
    // more selecteive.  This determines if the current board digest is added
    // to the repetition detection machinery.
    public boolean DigestState()
    {	
    	return(board_state.digestState());
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

    
    //
    // flood fill to make this blob as large as possible.  This is more elaborate
    // than it needs to be for scoring purposes, but it is also used by the robot
    //  OStack<hexblob> findBlobs(int forplayer,OStack<hexblob> all)
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


    // flood fill the blob to make it as large as possible, but don't
    // look for connections.  This is used when the blob os only being
    // checked as a possible win
    private boolean expandBlobForWin(hexblob blob,hexCell cell)
    {	if(cell==null) {}
    	else if((cell.sweep_counter!=sweep_counter))
    	{
    	cell.sweep_counter = sweep_counter;
    	cell.blob = blob;
     	if(cell.chip==blob.color)
    	  {
    	   	blob.addCell(cell);
    	   	if(blob.span()==ncols) { return(true); } 	// a win
    	   	for(int dir = 0; dir<6; dir++)
    		{	if(expandBlobForWin(blob,cell.exitTo(dir))) { return(true); }
    		}
    	  }
    	}
    	return(false);
    }
    
    // scan blobs only connected to the home row for the player
    // this is the fast version that only checks for a win
    private boolean findWinningBlob(int player)
    {
    	hexCell home = getCell('A',1);
    	hexChip pch = playerChip[player];
    	sweep_counter++;
    	int scanDirection =  (pch==hexChip.White)
    		? directionWhiteHome
    		: directionBlackHome;
    	while(home!=null)
    	{	
       	if((home.sweep_counter!=sweep_counter) && (home.chip==pch))
        {
        	hexblob blob = new hexblob(pch);
        	if(expandBlobForWin(blob,home)) { return(true); }
        }
        home = home.exitTo(scanDirection);
    	}    	
    	return(false);
    }
    public boolean WinForPlayerNow(int player)
    {	if(win[player]) { return(true); }
    	boolean win = findWinningBlob(player);
    	return(win);
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
    {	hexChip old = c.chip;
    	if(c.onBoard)
    	{
    	if(old!=null) { chips_on_board--;emptyCells.push(c); }
     	if(ch!=null) { chips_on_board++; emptyCells.remove(c,false); }
    	}
       	c.chip = ch;
    	return(old);
    }
    //
    // accept the current placements as permanent
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
    private hexCell unDropObject()
    {	hexCell rv = droppedDest;
    	if(rv!=null) 
    	{	pickedObject = rv.topChip();
    		SetBoard(rv,null); 
    		droppedDest = null;
     	}
    	return(rv);
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
    private void dropObject(hexCell c)
    {
       pickedSource = null;
       switch (c.rackLocation())
        {
        default:
            G.Error("Not expecting dest " + c.rackLocation);
            break;
        case Black_Chip_Pool:
        case White_Chip_Pool:		// back in the pool, we don't really care where
        	pickedObject = null;
        	pickedSource = null;
        	droppedDest = null;
            break;
        case BoardLocation:	// already filled board slot, which can happen in edit mode
        case EmptyBoard:
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
      	return (NothingMoving);
    }
    /**
     * get the cell represented by a source code, and col,row
     * @param source
     * @param col
     * @param row
     * @return
     */
    private hexCell getCell(HexId source, char col, int row)
    {
        switch (source)
        {
        default:
            G.Error("Not expecting source " + source);
        case BoardLocation:
        	return(getCell(col,row));
        case Black_Chip_Pool:
        	return(blackChipPool);
        case White_Chip_Pool:
        	return(whiteChipPool);
        } 	
    }
    public hexCell getCell(hexCell c)
    {
    	return((c==null)?null:getCell(c.rackLocation(),c.col,c.row));
    }
	// pick something up.  Note that when the something is the board,
    // the board location really becomes empty, and we depend on unPickObject
    // to replace the original contents if the pick is cancelled.
    private void pickObject(hexCell c)
    {	pickedSource = c;
        switch (c.rackLocation())
        {
        default:
            G.Error("Not expecting rackLocation " + c.rackLocation);
            break;
        case BoardLocation:
        	{
         	boolean wasDest = isDest(c);
        	unDropObject(); 
        	if(!wasDest)
        	{
            lastPicked = pickedObject = c.topChip();
         	lastDroppedObject = droppedDest = null;
			SetBoard(c,null);
        	}}
            break;

        case Black_Chip_Pool:
        case White_Chip_Pool:
        	lastPicked = pickedObject = c.chip;
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
        case Confirm:
        	if(droppedDest==null)
        	{setNextStateAfterDone();
        	}
        	break;
        case Play:
        case PlayOrSwap:
			setState(HexState.Confirm);
			break;
        case Puzzle:
			acceptPlacement();
            break;
        }
    }
    private void setNextStateAfterDone()
    {	G.Assert(chips_on_board+emptyCells.size()==fullBoard,"cells missing");
       	switch(board_state)
    	{
    	default: G.Error("Not expecting state "+board_state);
    		break;
    	case Gameover: break;
    	case ConfirmSwap: 
    		setState(HexState.Play); 
    		break;
    	case Confirm:
    	case Puzzle:
    	case Play:
    	case PlayOrSwap:
    		setState(((chips_on_board==1)&&(whoseTurn==SECOND_PLAYER_INDEX)&&!swapped) 
    				? HexState.PlayOrSwap
    				: HexState.Play);
    		
    		break;
    	}
       	resetState = board_state;
    }
    private void doDone()
    {
        acceptPlacement();

        if (board_state==HexState.Resign)
        {
            win[nextPlayer[whoseTurn]] = true;
    		setState(HexState.Gameover);
        }
        else
        {	if(WinForPlayerNow(whoseTurn)) 
        		{ win[whoseTurn]=true;
        		  setState(HexState.Gameover); 
        		}
        	else {setNextPlayer();
        		setNextStateAfterDone();
        	}
        }
    }
void doSwap()
{	HexId c = playerColor[0];
	hexChip ch = playerChip[0];
	playerColor[0]=playerColor[1];
	playerChip[0]=playerChip[1];
	playerColor[1]=c;
	playerChip[1]=ch;
	hexCell cc = playerCell[0];
	playerCell[0]=playerCell[1];
	playerCell[1]=cc;
	swapped = !swapped;
	switch(board_state)
	{	
	default: G.Error("Not expecting state "+board_state);
		break;
	case PlayOrSwap:
		  setState(HexState.ConfirmSwap);
		  break;
	case ConfirmSwap:
		  setState(HexState.PlayOrSwap);
		  break;
	case Gameover:
	case Puzzle: break;
	}

}
    public boolean Execute(commonMove mm,replayMode replay)
    {	Hexmovespec m = (Hexmovespec)mm;
        if(replay!=replayMode.Replay) { animationStack.clear(); }

        //G.print("E "+m+" for "+whoseTurn+" "+state);
        switch (m.op)
        {
		case MOVE_SWAP:	// swap colors with the other player
			doSwap();
			break;
        case MOVE_DONE:

         	doDone();

            break;

        case MOVE_DROPB:
        	{
        	hexCell animsrc = null;
			switch(board_state)
			{ case Puzzle: acceptPlacement(); break;
			  case ConfirmSwap:
			  case Confirm: animsrc = unDropObject(); unPickObject(); break;
			  case Play:
			  case PlayOrSwap: acceptPlacement(); break;
			  case Gameover:
			  case Resign:
				  break;
			}
			{
			hexChip po = pickedObject;
			hexCell src = getCell(m.source,m.to_col,m.to_row); 
			hexCell dest =  getCell(HexId.BoardLocation,m.to_col,m.to_row);
			pickObject(src);
            dropObject(dest);
            /**
             * if the user clicked on a board space without picking anything up,
             * animate a stone moving in from the pool.  For Hex, the "picks" are
             * removed from the game record, so there are never picked stones in
             * single step replays.
             */
            if(replay!=replayMode.Replay && (po==null))
            	{ animationStack.push(animsrc==null?src:animsrc);
            	  animationStack.push(dest); 
            	}
			}
            setNextStateAfterDrop();
        	}
            break;

        case MOVE_PICK:
            unDropObject();
            unPickObject();
            // fall through
        case MOVE_PICKB:
        	// come here only where there's something to pick, which must
        	// be a temporary p
        	pickObject(getCell(m.source,m.to_col,m.to_row));
        	switch(board_state)
        	{
        	case Puzzle:
         		break;
        	case Confirm:
        		setState(((chips_on_board==1) && !swapped) ? HexState.PlayOrSwap : HexState.Play);
        		break;
        	default: ;
        	}
            break;

        case MOVE_DROP: // drop on chip pool;
            dropObject(getCell(m.source,m.to_col,m.to_row));
            //setNextStateAfterDrop();

            break;
 
        case MOVE_START:
            setWhoseTurn(m.player);
            acceptPlacement();
            unPickObject();
            int nextp = nextPlayer[whoseTurn];
            // standardize the gameover state.  Particularly importing if the
            // sequence in a game is resign/start
            setState(HexState.Puzzle);	// standardize the current state
            if((win[whoseTurn]=WinForPlayerNow(whoseTurn))
               ||(win[nextp]=WinForPlayerNow(nextp)))
               	{ setState(HexState.Gameover); 
               	}
            else {  setNextStateAfterDone(); }

            break;

       case MOVE_RESIGN:
    	   	setState(unresign==null?HexState.Resign:unresign);
            break;
       case MOVE_RESET:
    	    if(unresign!=null) { setState(unresign); }
        	switch(board_state)
        	{
        	case Puzzle: 
        		unPickObject();
        		break;
        	case ConfirmSwap:
        		doSwap();
        		// fall through
        	default:
        		{	
        			hexCell dd = droppedDest;
            		hexCell ps = pickedSource;
        			if(pickedObject!=null)
        				{ unPickObject(); 
        				  if(ps.onBoard) 
        				  { droppedDest = ps; 
        				  	setState(HexState.Confirm);
        				  }
         				} 
        			else 
        			{
            		unDropObject();  
        			if(pickedObject!=null)
        			{
        				if(ps==null) 
        					{ ps = pickedSource = (pickedObject==hexChip.Black)?blackChipPool:whiteChipPool; 
        					}
        				// animate the stone moving back to the pool
        				animationStack.push(dd);
        				animationStack.push(ps);
        			}
        			unPickObject();
            		setState(resetState);       			
        			}
        		}
        		break;
        	case Gameover:
        		break;
        	}
        	break;
        case MOVE_EDIT:
        	acceptPlacement();
            setWhoseTurn(FIRST_PLAYER_INDEX);
            setState(HexState.Puzzle);
 
            break;

        default:
            G.Error("Can't execute " + m);
        }

        //System.out.println("Ex "+m+" for "+whoseTurn+" "+state);
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
        case Confirm:
        case PlayOrSwap:
        case Play:
        	// for hex, you can pick up a stone in the storage area
        	// but it's really optional
        	return(player==whoseTurn);
		case ConfirmSwap:
		case Resign:
		case Gameover:
			return(false);
        case Puzzle:
            return ((pickedObject!=null)?(pickedObject==playerChip[player]):true);
        }
    }

    public boolean LegalToHitBoard(hexCell c)
    {	if(c==null) { return(false); }
        switch (board_state)
        {
		case Play:
		case PlayOrSwap:
			return(c.chip==null);
		case ConfirmSwap:
		case Gameover:
		case Resign:
			return(false);
		case Confirm:
			return(isDest(c) || (c.chip==null));
        default:
            G.Error("Not expecting state " + board_state);
            return(true);	// not used
        case Puzzle:
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
        robotState.push(board_state); //record the starting state. The most reliable
        // to undo state transistions is to simple put the original state back.
        
        //G.Assert(m.player == whoseTurn, "whoseturn doesn't agree");

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
    public void UnExecute(Hexmovespec m)
    {
        //System.out.println("U "+m+" for "+whoseTurn);
    	HexState state = robotState.pop();
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
        	setState(state);
        	doSwap();
        	break;
        case MOVE_DROPB:
        	SetBoard(getCell(m.to_col,m.to_row),null);
        	break;
        case MOVE_RESIGN:
            break;
        }
        setState(state);
        if(whoseTurn!=m.player)
        {	moveNumber--;
        	setWhoseTurn(m.player);
        }
 }
 public commonMove Get_Random_Hex_Move(Random rand)
 {		int sz = emptyCells.size();
 		int off = G.nextInt(rand,sz);
 		hexCell empty = emptyCells.elementAt(off);
 		G.Assert(empty.chip==null,"isn't empty");
 		return(new Hexmovespec(MOVE_DROPB,empty.col,empty.row,playerColor[whoseTurn],whoseTurn));
 }
 OStack<commonMove> GetListOfMoves()
 {	OStack<commonMove> all = new OStack<commonMove>(commonMove.class);
 	if(board_state==HexState.PlayOrSwap)
 	{
 		all.addElement(new Hexmovespec("swap",whoseTurn));
 	}
 	for(hexCell c = allCells;
 	    c!=null;
 	    c = c.next)
 	{	if(c.topChip()==null)
 		{all.addElement(new Hexmovespec(MOVE_DROPB,c.col,c.row,playerColor[whoseTurn],whoseTurn));
 		}
 	}

 	return(all);
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
     		{	bd |= (1<<hexChip.BorderPairIndex[direction]);
     		}
     	}
 	c.borders = bd;
 	}
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

}
