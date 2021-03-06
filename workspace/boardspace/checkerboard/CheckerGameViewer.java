package checkerboard;

import online.common.*;
import online.common.SimpleSprite.Movement;
import online.game.*;
import online.game.sgf.*;

import java.awt.*;
import java.util.*;
import javax.swing.JCheckBoxMenuItem;

import lib.G;
import static checkerboard.CheckerMovespec.*;

/**
 * This code shows the overall structure appropriate for a game view window.
*/
public class CheckerGameViewer extends commonCanvas 
	implements ViewerProtocol, CheckerConstants, sgf_names
{
     // colors
    private Color reviewModeBackground = new Color(220,165,200);
    private Color HighlightColor = new Color(0.2f, 0.95f, 0.75f);
    private Color rackBackGroundColor = new Color(194,175,148);
    private Color boardBackgroundColor = new Color(220,165,155);
    private Color vcrButtonColor = new Color(0.7f, 0.7f, 0.75f);
 
    // private state
    private CheckerBoard b = null; 	// the board from which we are displaying
    private int CELLSIZE; 			// size of the layout cell.  
    private static int SUBCELL = 4;	// number of cells in a square
    private int SQUARESIZE;			// size of a board square
    
    // addRect is a service provided by commonCanvas, which supports a mode
    // to visualize the layout during development.  Look for "show rectangles"
    // in the options menu.
    //public Rectangle fullRect = addRect("fullRect"); //the whole viewer area
    //public Rectangle boardRect = addRect("boardRect"); //the actual board, normally at the left edge
    //public Rectangle chatRect = addRect("chatRect"); // the chat window
    private Rectangle logRect = addRect("logRect"); //the game log, normally off the the right
    private Rectangle stateRect = addRect("stateRect");
    private Rectangle firstPlayerChipRect = addRect("firstPlayerChipRect");
    private Rectangle secondPlayerChipRect = addRect("secondPlayerChipRect");
    private Rectangle reverseViewRect = addRect("reverse");
    private JCheckBoxMenuItem reverseOption = null;
   
    private Rectangle doneRect = addRect("doneRect");
    private Rectangle editRect = addRect("editRect");
    private Rectangle goalRect = addRect("goalRect");
    private Rectangle progressRect = addRect("progressRect");
     private Rectangle liftRect = addRect("liftRect");
    private Rectangle repRect = addRect("repRect");
    private boolean lifted=false;
    

    public void preloadImages()
    {	
       	CheckerChip.preloadImages(this,ImageDir);
    }


	/**
	 * 
	 * this is the real instance intialization, performed only once.
	 * info contains all the goodies from the environment.
	 * */
    public void init(exHashtable info)
    {	// for games with more than two players, the default players list should be 
    	// adjusted to the actual number, adjusted by the min and max
       	// int players_in_game = Math.max(3,info.getInt(exHashtable.PLAYERS_IN_GAME,4));
    	// players = new commonPlayer[players_in_game];
    	int players_in_game = Math.max(2,info.getInt(exHashtable.PLAYERS_IN_GAME,2));
    	super.init(info);
       	// 
    	// for games that require some random initialization, the random key should be
    	// captured at this point and passed to the the board init too.
        // randomKey = info.getInt(exHashtable.RANDOMSEED,-1);
    	//

        int randomKey = info.getInt(exHashtable.RANDOMSEED,-1);
       
        b = new CheckerBoard(info.getString(exHashtable.GAMETYPE, Variation.Checkers_10.name),randomKey,players_in_game);
        doInit(false);
        reverseOption = myFrame.addOption(s.get(ReverseView),b.reverse_y,deferredEvents);

        
     }

    /** 
     *  used when starting up or replaying and also when loading a new game 
     *  */
    public void doInit(boolean preserve_history)
    {	//System.out.println(myplayer.trueName + " doinit");
        super.doInit(preserve_history);				// let commonViewer do it's things
        int np = b.nPlayers();
        b.doInit(b.gametype,b.randomKey,np);			// initialize the board
        adjustPlayers(np);
        if(!preserve_history)
    	{ PerformAndTransmit(reviewOnly?"Edit":"Start P0", false,replayMode.Live);
    	}

    }
    /** this is called by the game controller when all players have connected
     * and the first player is about to be allowed to make his first move. This
     * may be a new game, or a game being restored, or a player rejoining a game.
     * You can override or encapsulate this method.
     */
    public void startPlaying()
    {	super.startPlaying();
    }

    /**
     * translate the mouse coordinate x,y into a size-independent representation
     * presumably based on the cell grid.  This is used to transmit our mouse
     * position to the other players and spectators, so it will be displayed
     * at approximately the same visual spot on their screen.  
     * The results of this function only have to be interpreted by {@link #decodeScreenZone}
     * Some trickier logic may be needed if the board has several orientations,
     * or if some mouse activity should be censored.
     */
    public String encodeScreenZone(int x, int y,Point p)
    {
    	return(super.encodeScreenZone(x,y,p));
    }
    /**
     * invert the transformation done by {@link #encodeScreenZone}, returning 
     * an x,y pixel address on the main window.
     * @param z
     * @param x
     * @param y
     * @return a point representing the decoded position
     */
    public Point decodeScreenZone(String z,int x,int y)
    {
    	return(super.decodeScreenZone(z,x,y));
    }
	/**
	 * 
	 * this is a debugging hack to give you an event based on clicking in the player name
	 * You can take whatever action you like, or no action.
	 */
    public boolean inPlayRect(int eventX, int eventY)
    {	return(super.inPlayRect(eventX,eventY));
     }

    /**
     * update the players clocks.  The normal thing is to tick the clocks
     * only for the player whose turn it is.  Games with a simtaneous action
     * phase need to do something more complicated.
     * @param inc the increment (in milliseconds) to add
     * @param p the current player, normally the player to update.
     */
    public void updatePlayerTime(long inc,commonPlayer p)
    {
    	super.updatePlayerTime(inc,p);
    }
	/**
	 * this is the main method to do layout of the board and other widgets.  I don't
	 * use swing or any other standard widget kit, or any of the standard layout managers.
	 * they just don't have the flexibility to produce the results I want.  Your milage
	 * may vary, and of course you're free to use whatever layout and drawing methods you
	 * want to.  However, I do strongly encourage making a UI that is resizable within
	 * reasonable limits, and which has the main "board" object at the left.
	 * 
	 *  The basic layout technique used here is to start with a cell which is about the size
	 *  of a board square, and lay out all the other object relative to the board or to one
	 *  another.  The rectangles don't all have to be on grid points, and don't have to
	 *  be nonoverlapping, just so long as the result generally looks good.
	 *  
	 *  When "extraactions" is available, a menu opion "show rectangles" works
	 *  with the "addRect" mechanism to help visualize the layout.
	 */ 
    public void setLocalBounds(int x, int y, int width, int height)
    {   boolean wideMode = width>height*1.5;
    	int separation=3;
        int sncols = (b.boardColumns*SUBCELL+20); // more cells wide to allow for the aux displays
        int snrows = (b.boardRows+1)*SUBCELL;  
        int cellw = width / sncols;
        int chatHeight = selectChatHeight(height);
        int cellh = (height-(wideMode ? 0 : chatHeight)) / snrows;
        int ideal_logwidth = CELLSIZE * 12;
        CELLSIZE = Math.max(1,Math.min(cellw, cellh)); //cell size appropriate for the aspect ration of the canvas
        SQUARESIZE = CELLSIZE*SUBCELL;
        fullRect.x = 0;			// the whole canvas
        fullRect.y = 0;
        fullRect.width = width;
        fullRect.height = height;

        // game log.  This is generally off to the right, and it's ok if it's not
        // completely visible in all configurations.
        
        stateRect.x = boardRect.x + CELLSIZE;
        stateRect.y = (wideMode ? 0 : chatHeight) +CELLSIZE/3;
        stateRect.width = boardRect.width - CELLSIZE;
        stateRect.height = CELLSIZE*2;

        boardRect.x = 0;
        boardRect.y = (wideMode ? 0 : chatHeight)+SQUARESIZE-CELLSIZE;
        boardRect.width = SQUARESIZE * b.boardColumns ;
        boardRect.height = SQUARESIZE * b.boardRows;


        firstPlayerChipRect.x = G.Right(boardRect)-CELLSIZE/2;
        firstPlayerChipRect.y = boardRect.y+CELLSIZE/2;
        firstPlayerChipRect.width = SQUARESIZE;
        firstPlayerChipRect.height = SQUARESIZE;
 
        secondPlayerChipRect.x = firstPlayerChipRect.x;
        secondPlayerChipRect.y = G.Bottom(fullRect)-CELLSIZE-SQUARESIZE;
        secondPlayerChipRect.width = SQUARESIZE;
        secondPlayerChipRect.height = SQUARESIZE;

       
        // "edit" rectangle, available in reviewers to switch to puzzle mode
        editRect.x = G.Right(boardRect)+CELLSIZE*separation;
        editRect.y = G.Bottom(firstPlayerChipRect)+CELLSIZE*2;
        editRect.width = CELLSIZE*6;
        editRect.height = 2*CELLSIZE;

        
        liftRect.x = G.Right(boardRect)+CELLSIZE/2;;
        liftRect.y = G.Bottom(editRect)+CELLSIZE*2;
        liftRect.width = liftRect.height=CELLSIZE*4;

        auxSRect.max=2.0;
        goalRect.x = boardRect.x;		// really just a general message
        goalRect.y = G.Bottom(boardRect)-CELLSIZE;
        goalRect.height = CELLSIZE*2;
        goalRect.width = boardRect.width;
        
        progressRect.x = goalRect.x+goalRect.width/6;	// a simple progress bar when the robot is running.
        progressRect.width = goalRect.width/2;
        progressRect.y = goalRect.y;
        progressRect.height = CELLSIZE/2;

        {
            commonPlayer pl0 = getPlayerOrTemp(0);
            commonPlayer pl1 = getPlayerOrTemp(1);
            {
            Rectangle p0time = pl0.timeRect;
            Rectangle p1time = pl1.timeRect;
            Rectangle p0alt = pl0.extraTimeRect;
            Rectangle p1alt = pl1.extraTimeRect;
            Rectangle p0anim = pl0.animRect;
            Rectangle p1anim = pl1.animRect;
            
            Rectangle firstPlayerRect = pl0.nameRect;
            Rectangle secondPlayerRect = pl1.nameRect;
            Rectangle firstPlayerPicRect = pl0.picRect;
            Rectangle secondPlayerPicRect = pl1.picRect;
            
            //first player name
            firstPlayerRect.x = G.Right(firstPlayerChipRect)+CELLSIZE;
            firstPlayerRect.y = firstPlayerChipRect.y;
            firstPlayerRect.width = CELLSIZE * 10;
            firstPlayerRect.height = CELLSIZE*2;
            //second player name
            secondPlayerRect.x = firstPlayerRect.x;
            secondPlayerRect.y = G.Bottom(boardRect) - firstPlayerRect.height;
            secondPlayerRect.width = firstPlayerRect.width;
            secondPlayerRect.height = firstPlayerRect.height;


            // first player portrait
            firstPlayerPicRect.x = G.Right(editRect)+CELLSIZE;
            firstPlayerPicRect.y = G.Bottom(firstPlayerRect);
            firstPlayerPicRect.width = CELLSIZE * 8;
            firstPlayerPicRect.height = CELLSIZE * 8;
            
     
            // player 2 portrait
            secondPlayerPicRect.x = firstPlayerPicRect.x;
            secondPlayerPicRect.height = firstPlayerPicRect.height;
            secondPlayerPicRect.y = secondPlayerRect.y - secondPlayerPicRect.height;
            secondPlayerPicRect.width = firstPlayerPicRect.width;
           	
            // time dispay for first player
            p0time.x = G.Right(firstPlayerRect);
            p0time.y = firstPlayerRect.y;
            p0time.width = CELLSIZE * 3;
            p0time.height = CELLSIZE;
            p0alt.x = p0time.x;
            p0alt.y = G.Bottom(p0time);
            p0alt.width = p0time.width;
            p0alt.height = p0time.height;
            
            // tfirst player "i'm alive" anumation ball
            p0anim.x = G.Right(p0time);
            p0anim.y = p0time.y;
            p0anim.width = p0time.height;
            p0anim.height = p0time.height;
            // time dispay for second player
            p1time.x = G.Right(secondPlayerRect);
            p1time.y = secondPlayerRect.y;
            p1time.width = p0time.width;
            p1time.height = p0time.height;
            p1alt.x = p1time.x;
            p1alt.y = G.Bottom(p1time);
            p1alt.width = p1time.width;
            p1alt.height = p1time.height;

            p1anim.x = p1time.x+p1time.width;
            p1anim.y = p1time.y;
            p1anim.width = p1time.height;
            p1anim.height = p1time.height;

            int logx = G.Right(p0anim)+CELLSIZE/2;
            logRect.x = logx;
            logRect.y = 0 ;
            logRect.width = Math.min(ideal_logwidth,fullRect.width-logRect.x);
            logRect.height = wideMode ? CELLSIZE*4 : chatRect.height;

            chatRect.x = wideMode ? logx : 0;
            chatRect.y = wideMode ? G.Bottom(logRect)+CELLSIZE: 0;
            chatRect.width = wideMode ? width-logx-CELLSIZE/2 : logx-CELLSIZE/2;
            chatRect.height = wideMode ? Math.min(chatHeight,height-chatRect.y) : chatHeight;
      
                  }}  
        
        // "done" rectangle, should always be visible, but only active when a move is complete.
        doneRect.x = editRect.x;
        doneRect.y = G.Bottom(liftRect)+CELLSIZE*2;
        doneRect.width = editRect.width;
        doneRect.height = editRect.height;

        repRect.x = goalRect.x+CELLSIZE;
        repRect.y = goalRect.y-CELLSIZE;
        repRect.height = CELLSIZE;
        repRect.width = goalRect.width-CELLSIZE;
        
        reverseViewRect.x = liftRect.x;
        reverseViewRect.y = G.Bottom(liftRect)+CELLSIZE;
        reverseViewRect.width = CELLSIZE*2;
        reverseViewRect.height = CELLSIZE*4;
 
        //this sets up the "vcr cluster" of forward and back controls.
        SetupVcrRects(liftRect.x,G.Bottom(doneRect)+2*CELLSIZE,
            CELLSIZE * 8,
            4 * CELLSIZE);
 
        theChat.setBounds(chatRect.x+x,chatRect.y+y,chatRect.width,chatRect.height);
        theChat.setVisible(true);
        generalRefresh();
    }
    
	// draw a box of spare . Notice if any are being pointed at.  Highlight those that are.
    private void DrawReverseMarker(Graphics gc, Rectangle r,HitPoint highlight)
    {	StockArt king = CheckerChip.getChip(b.reverse_y?1:0);
    	StockArt reverse = CheckerChip.getChip(b.reverse_y?0:1);
    	reverse.drawChip(gc,this,r.width,r.x+r.width/2,r.y+r.width/2,null);
    	king.drawChip(gc,this,r.width,r.x+r.width/2,r.y+r.width+r.width/2,null);
    	if(G.pointInRect(highlight,r))
    	{	G.frameRect(gc,Color.red,r);
			highlight.helpText = s.get(ReverseViewExplanation);
    		highlight.hitCode = CheckerId.ReverseViewButton;
    	}
     }  
    private void DrawLiftRect(Graphics gc,HitPoint highlight)
    {	boolean hit = false;
    	if(G.pointInRect(highlight,liftRect))
    	{	hit = true;
    		highlight.hitCode = CheckerId.LiftRect;
    		highlight.dragging = lifted = highlight.down;
    	}
		G.centerImage(gc,CheckerChip.liftIcon.image,liftRect,this); 
		G.frameRect(gc,hit?HighlightColor:Color.black,liftRect);
    }
	// draw a box of spare chips. Notice if any are being pointed at.  Highlight those that are.
    private void DrawCommonChipPool(Graphics gc, CheckerBoard gb, int forPlayer, Rectangle r, int player, HitPoint highlight)
    {	CheckerCell chips[]= gb.rack;
        boolean canHit = gb.LegalToHitChips(forPlayer);
        CheckerCell thisCell = chips[forPlayer];
        CheckerChip thisChip = thisCell.topChip();
        boolean canDrop = (getMovingObject()>=0);
        boolean canPick = (thisChip!=null);
        HitPoint pt = (canHit && (canPick||canDrop))? highlight : null; 
        String msg = ""+gb.chips_on_board[forPlayer];
        thisCell.drawStack(gc,this,pt,r.width,r.x+r.width/2,r.y+r.height/2,0,0,msg);

        if((highlight!=null) && (highlight.hitObject==thisCell))
        {	highlight.arrow = canDrop ? StockArt.DownArrow : StockArt.UpArrow;
        	highlight.awidth = r.width/2;
        }
     }

    //
    // sprites are normally a game piece that is "in the air" being moved
    // around.  This is called when dragging your own pieces, and also when
    // presenting the motion of your opponent's pieces, and also during replay
    // when a piece is picked up and not yet placed.  While "obj" is nominally
    // a game piece, it is really whatever is associated with b.movingObject()
    //
    public void drawSprite(Graphics g,int obj,int xp,int yp)
    {  	// draw an object being dragged
    	CheckerChip ch = CheckerChip.getChipNumber(obj);// Tiles have zero offset
    	ch.drawChip(g,this,SQUARESIZE,xp,yp,null);
     }

    // also related to sprites,
    // default position to display static sprites, typically the "moving object" in replay mode
    public Point spriteDisplayPoint()
	{   return(new Point(G.Right(boardRect)-SQUARESIZE/2,G.Bottom(boardRect)-SQUARESIZE/2));
	}


    /** this is used by the game controller to supply entertainment strings to the lobby */
    public String gameProgressString()
    {	// this is what the standard method does
    	// return ((reviewer ? s.get(ReviewAction) : ("" + viewMove)));
    	return(super.gameProgressString());
    }



    /* draw the deep unchangable objects, including those that might be rather expensive
     * to draw.  This background layer is used as a backdrop to the rest of the activity.
     * in our cease, we draw the board and the chips on it. 
     * */
    private void drawFixedElements(Graphics gc, CheckerBoard gb,Rectangle brect)
    {	boolean reviewBackground = reviewMode()&&!mutable_game_record;
      // erase
      gc.setColor(reviewBackground ? reviewModeBackground : boardBackgroundColor);
      //G.fillRect(gc, fullRect);
      G.tileImage(gc,CheckerChip.backgroundTile.image, fullRect, this);   
      if(reviewBackground)
      {	 
        G.tileImage(gc,CheckerChip.backgroundReviewTile.image,boardRect, this);   
      }
       
      // if the board is one large graphic, for which the visual target points
      // are carefully matched with the abstract grid
      //G.centerImage(gc,images[BOARD_INDEX], brect,this);

      gb.DrawGrid(gc,brect,use_grid,Color.white,Color.black,Color.blue,Color.black);
    }

   /* draw the board and the chips on it. */
    private int liftSteps=0;
    private void drawBoardElements(Graphics gc, CheckerBoard gb, Rectangle brect, HitPoint highlight)
    {
    	liftSteps = lifted ? Math.min(++liftSteps,12) : Math.max(--liftSteps,0);
     	boolean dolift = (liftSteps>0);
     	if(dolift && (liftSteps<12))
     		{ // this induces a very simple animation
     		repaint(20); 
     		}
     	//
        // now draw the contents of the board and anything it is pointing at
        //
     	
        int perspective_offset = 0;
        // conventionally light source is to the right and shadows to the 
        // left, so we want to draw in right-left top-bottom order so the
        // solid parts will fall on top of existing shadows. 
        // when the rotate view is in effect, top and bottom, left and right switch
        // but this iterator still draws everything in the correct order for occlusion
        // and shadows to work correctly.
    	for (int row = gb.topRow(),stepRow=gb.stepRow(),lastRow=gb.bottomRow()+stepRow;
		row!=lastRow;
		row += stepRow)	
    	{ 
		for (int colNum = gb.leftColNum(),stepCol=gb.stepColNum(),lastCol=gb.rightColNum()+stepCol;
		     colNum!=lastCol;
		     colNum+=stepCol)
        	{ 
			char thiscol = (char)('A'+colNum);
        	// note that these accessors "lastRowInColumn" etc
        	// are not really needed for simple boards, but they
        	// also work for hex boards and boards with cut out corners
            CheckerCell cell = gb.getCell(thiscol,row);
            int ypos = (brect.y + brect.height) - gb.cellToY(thiscol, row);
            int xpos = brect.x + gb.cellToX(thiscol, row);
            HitPoint hitNow = gb.legalToHitBoard(cell) ? highlight : null;
            if( cell.drawStack(gc,this,hitNow,SQUARESIZE,xpos,ypos,liftSteps,0.1,null)) 
            	{ // draw a highlight rectangle here, but defer drawing an arrow until later, after the moving chip is drawn
            	hitNow.arrow =(getMovingObject()>=0) 
      				? StockArt.DownArrow 
      				: cell.topChip()!=null?StockArt.UpArrow:null;
            	hitNow.awidth = SQUARESIZE/2;
            	G.frameRect(gc,Color.red,xpos-CELLSIZE,ypos-CELLSIZE-((cell.topChip()==null)?0:perspective_offset),CELLSIZE*2,CELLSIZE*2);
            	
            	}

        	}
    	}
    }
     public void drawAuxControls(Graphics gc,HitPoint highlight)
    {  DrawLiftRect(gc,highlight);
       DrawReverseMarker(gc,reverseViewRect,highlight);
    }
    //
    // draw the board and things on it.  If gc!=null then actually 
    // draw, otherwise just notice if the highlight should be on
    //
    public void redrawBoard(Graphics gc, HitPoint highlight)
    {  CheckerBoard gb = b;
        if(gc!=null)
    	   {  //possibly select an alternate board
    	   	  CheckerBoard disb = (CheckerBoard) disB();
    	      if(disb!=null) 
    	      { gb = disb; 
     	    	G.frameRect(gc,Color.black,boardRect); // give a visual indicator
    	      }
    	   }
      boolean ourTurn = OurMove();
      boolean moving = getMovingObject()>=0;
      HitPoint ot = ourTurn ? highlight : null;	// hit if our turn
      HitPoint select = moving?null:ot;	// hit if our turn and not dragging
      HitPoint ourSelect = (moving && !reviewMode()) ? null : highlight;	// hit if not dragging
      CheckerState vstate = gb.getState();
       redrawGameLog(gc, ourSelect, logRect, boardBackgroundColor);
    
        drawBoardElements(gc, gb, boardRect, ot);
        DrawCommonChipPool(gc, gb,FIRST_PLAYER_INDEX,firstPlayerChipRect, gb.whoseTurn,ot);
        DrawCommonChipPool(gc, gb, SECOND_PLAYER_INDEX, secondPlayerChipRect,gb.whoseTurn,ot);

        if (gc != null)
        {
            gc.setFont(standardBoldFont);
        }
		if (vstate != CheckerState.Puzzle)
        {
             if (G.handleRoundButton(gc, doneRect, 
            		(b.DoneState()? select : null), s.get(DoneAction),
                    HighlightColor, rackBackGroundColor))
            {	// always display the done button, but only make it active in
            	// the appropriate states
                select.hitCode = DefaultId.HitDoneButton;
            }
            if (allowed_to_edit)
            {
            	if (G.handleRoundButton(gc, editRect, select,
                            s.get(EditAction), HighlightColor,
                            rackBackGroundColor))
                {
                    select.hitCode = DefaultId.HitEditButton;
                }
       }}

 		drawPlayerStuff(gc,(vstate==CheckerState.Puzzle),moving?null:highlight,HighlightColor,rackBackGroundColor);


        if (gc != null)
        {	
            standardGameMessage(gc,
            		vstate==CheckerState.Gameover
            			?gameOverMessage()
            			:s.get(vstate.getDescription()),
            				vstate!=CheckerState.Puzzle,
            				gb.whoseTurn,
            				stateRect);
            goalAndProgressMessage(gc,highlight,Color.black,s.get(VictoryCondition),progressRect, goalRect);
         }
        DrawRepRect(gc,b.Digest(),repRect);
        drawVcrGroup(ourSelect, gc, HighlightColor, vcrButtonColor);
        drawAuxControls(gc,ourSelect);

    }

    /**
     * Execute a move by the other player, or as a result of local mouse activity,
     * or retrieved from the move history, or replayed form a stored game. 
     * @param mm the parameter is a commonMove so the superclass commonCanvas can
     * request execution of moves in a generic way.
     * @return true if all went well.  Normally G.Error would be called if anything went
     * seriously wrong.
     */
     public boolean Execute(commonMove mm,replayMode replay)
    {	
        if(b.getState()==CheckerState.Puzzle)
    	{   mm.setSliderNumString("--");
    		switch(mm.op)
        	{
    		case MOVE_DROPB:
    			lastDropped = b.pickedObject;
        	case MOVE_PICK: 
        	case MOVE_PICKB: 
        		break;
    		default:
    			mm.setLineBreak(true);
        	}
    	}
 
        handleExecute(b,mm,replay);
        startBoardAnimations(replay);
        if(replay!=replayMode.Replay) { playSounds(mm); }
 
        return (true);
    }
     
     void startBoardAnimations(replayMode replay)
     {
        if(replay!=replayMode.Replay)
     	{	while(b.animationStack.size()>1)
     		{
     		CheckerCell dest = b.animationStack.pop();
     		CheckerCell src = b.animationStack.pop();
    		//
    		// in cases where multiple chips are flying, topChip() may not be the right thing.
    		//
     		startAnimation(src,dest,dest.topChip());
     		}
     	}
        	b.animationStack.clear();
     } 
     void startAnimation(CheckerCell from,CheckerCell to,CheckerChip top)
     {	if((from!=null) && (to!=null) && (top!=null))
     	{	double speed = masterAnimationSpeed*0.5;
      		if(debug)
     		{
     			G.Assert(!((from.current_center_x==0) && (from.current_center_y==0)),"From Cell %s center is not set",from);
        			G.Assert(!((to.current_center_x==0) && (to.current_center_y==0)),"To %s center is not set",to);
     		}
     		
     		// make time vary as a function of distance to partially equalize the runtim of
     		// animations for long verses short moves.
     		double dist = G.distance(from.current_center_x, from.current_center_y, to.current_center_x,  to.current_center_y);
     		double full = G.distance(0,0,boardRect.width,boardRect.height);
     		double endtime = speed*Math.sqrt(dist/full);
     		
     		SimpleSprite newSprite = new SimpleSprite(true,top,
     				(int)b.CELLSIZE,	// use the same cell size as drawSprite would
     				endtime,
             		from.current_center_x,from.current_center_y,
             		to.current_center_x,to.current_center_y);
     		newSprite.movement = Movement.SlowIn;
             to.addActiveAnimation(newSprite);
   			addSprite(newSprite);
   			}
     }
/**
 * parse a move specifier on behalf of the current player.  This is called by the 
 * "game" object when it receives a move from the other player.  Note that it may
 * be called while we are in review mode, so the current state of the board should
 * not be considered.
 */
    public commonMove ParseNewMove(String st)
    {
        return (new CheckerMovespec(st, -1));
    }
    


    /**
     * prepare to add nmove to the history list, but also edit the history
     * to remove redundant elements, so that indecisiveness by the user doesn't
     * result in a messy game log.  
     * 
     * For all ordinary cases, this is now handled by the standard implementation
     * in commonCanvas, which uses the board's Digest() method to distinguish new
     * states and reversions to past states.
     * 
     * For reference, the commented out method below does the same thing for "Hex". 
     * You could resort to similar techniques to replace or augment what super.EditHistory
     * does, but your efforts would probably be better spent improving your Digest() method
     * so the commonCanvas method gives the desired result.
     * 
     * Note that it should always be correct to simply return nmove and accept the messy
     * game record.
     * 
     * This may require that move be merged with an existing history move
     * and discarded.  Return null if nothing should be added to the history
     * One should be very cautious about this, only to remove real pairs that
     * result in a null move.  It is vital that the operations performed on
     * the history are identical in effect to the manipulations of the board
     * state performed by "nmove".  This is checked by verifyGameRecord().
     * 
     * in commonEditHistory()
     * 
     */

//    public commonMove EditHistory(commonMove nmove)
//    {
//    	CheckerMovespec newmove = (CheckerMovespec) nmove;
//    	CheckerMovespec rval = newmove;			// default returned value
//        int size = History.size() - 1;
//        int idx = size;
//        int state = b.board_state;
// 
//        while (idx >= 0)
//            {	int start_idx = idx;
//            CheckerMovespec m = (CheckerMovespec) History.elementAt(idx);
//                if(m.next!=null) { idx = -1; }
//                else 
//               {
//                switch (newmove.op)
//                {
//                case MOVE_RESET:
//                	rval = null;	// reset never appears in the record
//                 case MOVE_RESIGN:
//                	// resign unwind any preliminary motions
//                	switch(m.op)
//                	{
//                  	default:	
//                 		if(state==PUZZLE_STATE) { idx = -1; break; }
//                 	case MOVE_PICK:
//                 	case MOVE_PICKB:
//               		UndoHistoryElement(idx);	// undo back to last done
//                		idx--;
//                		break;
//                	case MOVE_DONE:
//                	case MOVE_START:
//                	case MOVE_EDIT:
//                		idx = -1;	// stop the scan
//                	}
//                	break;
//                	
//             case MOVE_DONE:
//             default:
//            		idx = -1;
//            		break;
//               case MOVE_DROPB:
//                	if(m.op==MOVE_PICKB)
//                	{	if((newmove.to_col==m.from_col)
//                			&&(newmove.to_row==m.from_row))
//                		{ UndoHistoryElement(idx);	// pick/drop back to the same spot
//                		  idx--;
//                		  rval=null;
//                		}
//                	else if(idx>0)
//                	{ CheckerMovespec m2 = (CheckerMovespec)History.elementAt(idx-1);
//                	  if((m2.op==MOVE_DROPB)
//                			  && (m2.to_col==m.from_col)
//                			  && (m2.to_row==m.from_row))
//                	  {	// sequence is pick/drop/pick/drop, edit out the middle pick/drop
//                		UndoHistoryElement(idx);
//                	  	UndoHistoryElement(idx-1);
//                	  	idx = idx-2;
//                	  }
//                	  else { idx = -1; }
//                		
//                	}
//                	else { idx = -1; }
//                	}
//                	else { idx = -1; }
//                	break;
//                	
//            	}
//               }
//            G.Assert(idx!=start_idx,"progress editing history");
//            }
//         return (rval);
//    }
//
   /** 
     * this method is called from deep inside PerformAndTransmit, at the point
     * where the move has been executed and the history has been edited.  It's
     * purpose is to veryfy that the history accurately represents the current
     * state of the game, and that the fundamental game machinery is in a consistent
     * and reproducable state.  Basically, it works by creating a duplicate board
     * resetting it and feeding the duplicate the entire history, and then verifying 
     * that the duplcate is the same as the original board.  It's perfectly ok, during
     * debugging and development, to temporarily change this method into a no-op, but
     * be warned if you do this because it is throwing an error, there are other problems
     * that need to be fixed eventually.
     */
    public void verifyGameRecord()
    {	super.verifyGameRecord();
    }    
    // for reference, here's the standard definition
    //   public void verifyGameRecord()
    //   {	BoardProtocol ourB =  getBoard();
    //   	int ourDig = ourB.Digest();
    //   	BoardProtocol dup = dupBoard = ourB.cloneBoard();
    //   	int dupDig = dup.Digest();
    //   	G.Assert(dupDig==ourDig,"Duplicate Digest Matches");
    //   	dup.doInit();
    //   	int step = History.size();
    //   	int limit = viewStep>=0 ? viewStep : step;
    //   	for(int i=0;i<limit;i++) 
    //   		{ commonMove mv = (commonMove)History.elementAt(i);
    //   		  //G.print(".. "+mv);
    //   		  dup.Execute(mv); 
    //   		}
    //   	int dupRedig = dup.Digest();
    //   	G.Assert(dup.whoseTurn()==ourB.whoseTurn(),"Replay whose turn matches");
    //   	G.Assert(dup.moveNumber()==ourB.moveNumber(),"Replay move number matches");
    //   	if(dupRedig!=ourDig)
    //   	{
    //   	//int d0 = ourB.Digest();
    //   	//int d1 = dup.Digest();
    //   	G.Assert(false,"Replay digest matches");
    //   	}
    //   	// note: can't quite do this because the timing of "SetDrawState" is wrong.  ourB
    //   	// may be a draw where dup is not if ourB is pending a draw.
    //   	//G.Assert(dup.getState()==ourB.getState(),"Replay state matches");
    //   	dupBoard = null;
    //   }
    
private void playSounds(commonMove mm)
{
	CheckerMovespec m = (CheckerMovespec) mm;

    // add the sound effects
    switch(m.op)
    {
    case MOVE_RACK_BOARD:
    case MOVE_BOARD_BOARD:
      	 playASoundClip(light_drop,100);
       	 playASoundClip(heavy_drop,100);
   	break;
     case MOVE_PICK:
    	 playASoundClip(light_drop,100);
    	 break;
    case MOVE_PICKB:
    	playASoundClip(light_drop,100);
    	break;
    case MOVE_DROP:
    	break;
    case MOVE_DROPB:
      	 playASoundClip(heavy_drop,100);
      	break;
    default: break;
    }
	
}

 
/**
 * the preferred mouse gesture style is to let the user "pick up" objects
 * by simply clicking on them, but we also allow him to click and drag. 
 * StartDragging is called when he has done this.
 */
    public void StartDragging(HitPoint hp)
    {
        int mo = getMovingObject();
        if ((mo < 0) && (hp.hitCode instanceof CheckerId)) // not dragging anything yet, so maybe start
        {
        
        if(hp!=null)
        {
        CheckerId hitObject = (CheckerId)hp.hitCode;
		CheckerCell cell = (CheckerCell)hp.hitObject;
		CheckerChip chip = (cell==null) ? null : cell.topChip();
		if(chip!=null)
		{
	    switch(hitObject)
	    {
	    case LiftRect:
             break;
	    case Black_Chip_Pool:
	    	PerformAndTransmit("Pick B "+cell.row+" "+chip.id.shortName);
	    	break;
	    case White_Chip_Pool:
	    	PerformAndTransmit("Pick W "+cell.row+" "+chip.id.shortName);
	    	break;
	    case BoardLocation:
	    	// note, in this implementation the board squares are themselves pieces on the board
	    	// if the board becomes a graphic, then this > should be >= to enable click-and-drag 
	    	// behavior as well as click-to-pick
	    	if(cell.chipIndex>0)
	    		{
	    		PerformAndTransmit("Pickb "+cell.col+" "+cell.row+" "+chip.id.shortName);
	    		}
	    	break;
		default:
			break;
        }

        if (getMovingObject() >= 0)
        {	// if we got something started, inform the mouse handler
            hp.dragging = true;
        } }
        }}
    }

 	/** 
	 * this is called on "mouse up".  We may have been just clicking
	 * on something, or we may have just finished a click-drag-release.
	 * We're guaranteed just one mouse up, no bounces.
	 */
    public void StopDragging( HitPoint hp)
    {	CellId id = hp.hitCode;
    	if(!(id instanceof CheckerId)) 
    		{ // handle all the actions that aren't ours
    			missedOneClick = performStandardActions(hp,missedOneClick); 
    		}
    	else {
    	missedOneClick = false;
        CheckerId hitObject = (CheckerId)id;
		CheckerState state = b.getState();
		CheckerCell cell = (CheckerCell)hp.hitObject;
		CheckerChip chip = (cell==null) ? null : cell.topChip();
        switch (hitObject)
        {
        default:
           G.Error("Hit Unknown object " + hitObject);
           break;

        case ReverseViewButton:
       	 reverseOption.setState(b.reverse_y = !b.reverse_y);
       	 generalRefresh();
       	 break;

        case LiftRect:
        	break;
         case BoardLocation:	// we hit the board 
			switch(state)
			{
			default: G.Error("Not expecting drop on filled board in state "+state);
			case Confirm:
			case Play:
			case Puzzle:
				if(b.movingObjectIndex()>=0)
				{ if(cell!=null) { PerformAndTransmit("Dropb "+cell.col+" "+cell.row); }
				}
				else if(chip!=null)
				{
				PerformAndTransmit( "Pickb "+cell.col+" "+cell.row+" "+chip.id.shortName);
				}
				break;
			}
			break;
			
        case White_Chip_Pool:
        case Black_Chip_Pool:
        	{
        	int mov = b.movingObjectIndex();
        	String col =  hitObject.shortName;
            if(mov>=0) 
			{//if we're dragging a black chip around, drop it.
            	switch(state)
            	{
            	default: G.Error("can't drop on rack in state "+state);
                case Play:
            		PerformAndTransmit(commonMove.RESET);
            		break;

               	case Puzzle:
            		PerformAndTransmit("Drop"+col+cell.row+" "+mov);
            		break;
            	}
			}
         	}
            break;

        }}
        repaint(20);
    }

    
    // draw the fixed elements, using the saved background if it is available
    // and believed to be valid.
    public void drawFixedElements(Graphics offGC,boolean complete)
    {
      	complete |= createAllFixed(fullRect.width, fullRect.height); // create backing bitmaps;
      	Image allFixed = allFixed();		 // create backing bitmaps;

     	if(allFixed==null)
    	{	// no deep background, draw on the immediate background
    		drawFixedElements(offGC, b,boardRect);	
    	}
    	else
    	{	// draw the fixed background elements, either into the fixed bitmap
        	// or to the immediate bitmap, or to the screen directly.
        	if(complete) 
    	    	 { // redraw the deep background
    	    		Graphics allFixedGC = allFixed.getGraphics();
    	    		G.setClip(allFixedGC,fullRect);
    	            drawFixedElements(allFixedGC, b,boardRect);
    	    	 }
        	// draw the deep background on the immediate background
        	offGC.drawImage(allFixed,0,0,fullRect.width,fullRect.height,this);
    	}
     }
    /** this is the place where the canvas is actually repainted.  We get here
     * from the event loop, not from the normal canvas repaint request.
     */
    public void paintCanvas(Graphics g, boolean complete,HitPoint hp)
    {
       	CheckerBoard disb = (CheckerBoard) disB();
       	CheckerBoard gb = (disb == null) ? b : disb;

	    gb.SetDisplayParameters(0.94,1.0,  0.12,0.1,  0);
	    gb.SetDisplayRectangle(boardRect);
       
      	Image offScreen = createOffScreen(fullRect.width, fullRect.height); // create backing bitmaps;
      	Graphics offGC = (offScreen==null) 
      		? g 
      		: offScreen.getGraphics();
      	G.setClip(offGC,fullRect);
     	drawFixedElements(offGC,complete);
   	
    	// draw the board contents and changing elements.
        redrawBoard(offGC,hp);
        //      draw clocks, sprites, and other ephemera
        drawClocksAndMice(offGC, null);

        DrawTileSprite(offGC,hp); //draw the floating tile, if present
        DrawArrow(offGC,hp);
        
        // these are optional display hacks for debugging the applet,
        // enabled by menu options under "extraactions" control.
        //
        ShowStats(offGC,boardRect.x,G.Bottom(fullRect)-CELLSIZE*2);	// add some stats on top of everything
        showRectangles(offGC, CELLSIZE); //show rectangles in the UI
        
        drawSprites(offGC);
        
        if(offScreen!=null)
        	{
            displayClipped(g,fullRect,chatRect,offScreen);
        	}
    }
    /**
     * this is a token or tokens that initialize the variation and
     * set immutable parameters such as the number of players
     * and the random key for the game.  It can be more than one
     * token, which ought to be parseable by {@link #performHistoryInitialization}
     * @return return what will be the init type for the game
     */
    public String gameType() 
    { 
    	return(""+b.gametype+" "+b.randomKey+" "+b.nPlayers()); 
   }
    public String sgfGameType() { return(Checker_SGF); }

    // the format is just what is produced by FormHistoryString
    //
    // this is completely standardized
    //public void performHistoryTokens(StringTokenizer his)
    //{	String command = "";
    //    // now the rest
    //    while (his.hasMoreTokens())
    //    {
    //        String token = his.nextToken();
    //        if (",".equals(token) || ".end.".equals(token))
    //        {
    //            if (!"".equals(command))
    //            {
    //                PerformAndTransmit(command, false,false);
    //                command = "";
    //            }
    //        }
    //       else
    //        {
    //            command += (" " + token);
    //        }
    //    }	
    //}  
    //public void performPlayerInitialization(StringTokenizer his)
    //{	int fp = G.IntToken(his);
    //	BoardProtocol b = getBoard();
    //    if (fp < 0)   {  fp = 0;  }
    //    b.setWhoseTurn(fp);
    //    players[fp].ordinal = 0;
    //    players[(fp == 0) ? 1 : 0].ordinal = 1;
    //	
    //}

    
    
    /**
     * parse and perform the initialization sequence for the game, which
     * was produced by {@link online.game.commonCanvas#gameType}
     */
    public void performHistoryInitialization(StringTokenizer his)
    {	String token = his.nextToken();		// should be a checker init spec
    	long rk = G.LongToken(his);
    	int np = G.IntToken(his);
    	// make the random key part of the standard initialization,
    	// even though games like checkers probably don't use it.
        b.doInit(token,rk);
        adjustPlayers(np);

    }

    
 //   public void doShowText()
 //   {
 //       if (debug)
 //       {
 //           super.doShowText();
 //       }
 //       else
 //       {
 //           theChat.postMessage(GAMECHANNEL,KEYWORD_CHAT,
 //               s.get(CensoredGameRecordString));
//        }
//    }

    /** handle action events
     * 
     */
    public boolean handleDeferredEvent(Object target,String command)
    {
    	if(target==reverseOption)
    	{
    	b.reverse_y = reverseOption.getState();
    	generalRefresh();
    	return(true);
    	}
    	else 
    	return(super.handleDeferredEvent(target,command));
     }
/** handle the run loop, and any special actions we need to take.
 * The mouse handling and canvas painting will be called automatically
 *  */
    
 //   public void ViewerRun()
 //   {
 //       super.ViewerRun();
 //   }

    /** start the robot.  This is used to invoke a robot player.  Mainly this needs 
     * to know the class for the robot and any initialization it requires.  Normally
     * the the robot player p is also returned.  Raj (and other simultaneous play games)
     * might return a different player, the one actually started.
     *  */
    public commonPlayer startRobot(commonPlayer p,commonPlayer runner)
    {	// this is what the standard method does:
    	// int level = sharedInfo.getInt(sharedInfo.ROBOTLEVEL,0);
    	// RobotProtocol rr = newRobotPlayer();
    	// rr.InitRobot(sharedInfo, getBoard(), null, level);
    	// p.startRobot(rr);
    	return(super.startRobot(p,runner));
    }

    public BoardProtocol getBoard()   {    return (b);   }
    public SimpleRobotProtocol newRobotPlayer() { return(new CheckerPlay()); }


    /** replay a move specified in SGF format.  
     * this is mostly standard stuff, but the key is to recognise
     * the elements that we generated in sgf_save
     */
    public void ReplayMove(sgf_node no)
    {
        String comments = "";
        sgf_property prop = no.properties;

        while (prop != null)
        {
            String name = prop.getName();
            String value = (String) prop.getValue();

            if (setup_property.equals(name))
            {	StringTokenizer st = new StringTokenizer(value);
            	String typ = st.nextToken();
            	long ran = G.LongToken(st);
                b.doInit(typ,ran);
                adjustPlayers(b.nPlayers());
             }
            else if (name.equals(comment_property))
            {
                comments += value;
            }
            else if (name.equals(game_property))
            {
                if (!(value.toLowerCase().equals("checkers") || value.equals(Checker_SGF)))
                {
                	G.Error("game type " + value + " is not this game");
                }
                adjustPlayers(b.nPlayers());
            }
            else if (parseVersionCommand(name,value,2)) {}
            else if (parsePlayerCommand(name,value)) {}
            else 
            {
                replayStandardProps(name,value);
            }

            prop = prop.next;
        }

        if (!"".equals(comments))
        {
            setComment(comments);
        }
    }
}

