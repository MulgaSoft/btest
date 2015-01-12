package tablut;

import online.common.*;

import java.awt.*;
import java.util.*;


import lib.G;

import online.game.*;
/**
 * 
 * Overall Architecture
 * 
 * The site provides the lobby, choice game and opponents, communication between the players, information 
 * for spectators,  rankings, and a host of other services.  Each game has to be concerned only with 
 * the game itself.   An individual game (say, Hex) is launched and each client independantly initializes
 * itself to a common starting state.   Thereafter each player specifies messages to be broadcast to the
 * other participants, and receives messages being broadcast by the other participants, which keep everyone
 * informed about the state of the game.  There is no common "true" state of the game - all the participants
 * keep in step by virtue of seeing the same stream of messages.    Messages are mostly simple "pick up a stone"
 * "place a stone on space x" and so on.
 * 
 * The things a game must implement are specified by the class "ViewerProtocol", and a game could just
 * start there and be implemented completely from scratch, but in practice there is another huge pile
 * of things that every game has to do; dealing with graphis, mouse events, saving and restoring the
 * game state from static records, replaying and reviewing games and so on.   These are implemented in the 
 * class "commonCanvas" and by several board-like base classes for Hex and Square geometry boards.   
 * All the existing games for boardspace use these classes to provide graphics and basic board representation.
 * 
 * For games with robot players, there is another huge pile of things that a robot has to do, generating
 * moves, evaluating and choosing the best, and implementing a lookahead several moves deep.   There's a
 * standard framework for this using the "RobotProtocol" class and the "SearchDriver" class. 
 */
import online.game.sgf.sgf_names;
import online.game.sgf.sgf_node;
import online.game.sgf.sgf_property;

/**
 * 
 * Change History

 * The main classes are:
 *  TabGameViewer - this class, a canvas for display and mouse handling
 *  TabGameBoard - board representaion and implementation of the game logic
 *  Tabmovespec - representation, parsing and printing of move specifiers
 *  TabPlay - a robot to play the game
 *  TabConstants - static constants shared by all of the above.  
 *  
 *  The primary purpose of the TabGameViewer class is to do the actual
 *  drawing and to mediate the mouse gestures.   However, all the actual
 *  work is done in an event loop, rather than in direct reposonse to mouse or
 *  window events, so there is only one process involved.  With a single 
 *  process, there are no worries about synchronization among processes
 *  of lack of synchronization - both major causes of flakey user interfaces.
 *  
 *  The actual mouse handling is done by the commonCanvas class, which simply 
 *  records the recent mouse activity, and triggers "MouseMotion" to be called
 *  while the main loop is executing.
 *  
 *  Similarly, the actual "update" and "paint" methods for the canvas are handled
 *  by commonCanvas, which merely notes that a paint is needed and returns immediately.
 *  paintCanvas is called in the event loop.
 *  
 *  The drawing methods here combine mouse handling and drawing in a slightly
 *  nonstandard way.  Most of the drawing routines also accept a "HitPoint" object
 *  which contains the coordinates of the mouse.   As objects are drawn, we notice
 *  if the current object contains the mouse point, and if so deposit a code for 
 *  the current object in the HitPoint.  the Graphics object for drawing can be null,
 *  in which case no drawing is actully done, but the mouse sensitivity is checked
 *  anyway.  This method of combining drawing with mouse sensitivity helps keep the
 *  mouse sensitivity accurate, because it is always in agreement with what is being
 *  drawn.
 *  
 *  Steps to clone this hierarchy to start the next game
 *  1) copy the hierarchy to a brother directory
 *  2) open eclipse, then select the root and "refresh".  This should result
 *     in just a few complaints about package mismatches for the clones.
 *  3) fix the package names in the clones
 *  4) rename each of the classes in the clones, using refactor/rename
*/
public class TabGameViewer extends commonCanvas 
	implements ViewerProtocol, TabConstants, sgf_names
{
     // colors
    private Color reviewModeBackground = new Color(220,165,200);
    private Color HighlightColor = new Color(0.2f, 0.95f, 0.75f);
    private Color RingFillColor = new Color(10, 163, 190);
    private Color RingTextColor = Color.black;
    private Color GridColor = Color.black;
    private Color rackBackGroundColor = new Color(225,225,255);
    private Color boardBackgroundColor = new Color(220,165,155);
    private Color vcrButtonColor = new Color(0.7f, 0.7f, 0.75f);
 
    // images, shared among all instances of the class so loaded only once
    private static Image[] textures = null;// background textures
    
    // private state
    private TabGameBoard b = null; //the board from which we are displaying
    private int CELLSIZE; 	//size of the layout cell
    private int BOARDCELLSIZE;
    // addRect is a service provided by commonCanvas, which supports a mode
    // to visualize the layout during development.  Look for "show rectangles"
    // in the options menu.
    //private Rectangle fullRect = addRect("fullRect"); //the whole viewer area
    //private Rectangle boardRect = addRect("boardRect"); //the actual board, normally at the left edge
    private Rectangle logRect = addRect("logRect"); //the game log, normally off the the right
    private Rectangle stateRect = addRect("stateRect");
    private Rectangle silverShipRect = addRect("silverShip");
    private Rectangle goldShipRect = addRect("goldShip");
    private Rectangle goldFlagRect = addRect("goldFlag");
    private Rectangle doneRect = addRect("doneRect");
    private Rectangle editRect = addRect("editRect");
    private Rectangle goalRect = addRect("goalRect");
    private Rectangle progressRect = addRect("progressRect");
	private Rectangle swapRect=addRect("swapRect");
	private Rectangle repRect = swapRect;

    public Rectangle flagshipWinRect = addRect("flagWinRect");		// flagship wins only in the corner
    public Rectangle flagOwnRect = addRect("flagOwnRect");			// only king can occupy the center
    public Rectangle flagCaptureRect = addRect("flagCaptureRect");	// flagship can capture
    public Rectangle flagFoursideRect = addRect("flagFoursideRect");// flagship captured on 4 sides only


    public void preloadImages()
    {	TabChip.preloadImages(this,ImageDir);
    	if (textures == null)
    	{ 	// note that for this to work correctly, the images and masks must be the same size.  
        	// Refer to http://www.andromeda.com/people/ddyer/java/imagedemo/transparent.html
    		
    		// images and textures are static variables, so they're shared by
    		// the entire class and only get loaded once.  Special synchronization
    		// tricks are used to make sure.
          textures = load_images(ImageDir,TextureNames);
    	}
    }

	/**
	 * 
	 * this is the real instance intialization, performed only once.
	 * info contains all the goodies from the environment.
	 * */
    public void init(exHashtable info)
    {
        super.init(info);
       
        b = new TabGameBoard(info.getString(exHashtable.GAMETYPE, Default_Tablut_Game));
        doInit(false);
   }

    /** 
     *  used when starting up or replaying and also when loading a new game 
     *  */
    public void doInit(boolean preserve_history)
    {
        //System.out.println(myplayer.trueName + " doinit");
        super.doInit(preserve_history);				// let commonViewer do it's things
        b.doInit(b.gameType());						// initialize the board
        if(!preserve_history)
    	{ PerformAndTransmit(reviewOnly?"Edit":"Start P0", false,replayMode.Live);
    	}
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
	 *  of a board square, and lay out all the other objects relative to the board or to one
	 *  another.  The rectangles don't all have to be on grid points, and don't have to
	 *  be nonoverlapping, just so long as the result generally looks good.
	 *  
	 *  When "extraactions" is available, a menu opion "show rectangles" works
	 *  with the "addRect" mechanism to help visualize the layout.
	 */ 
    public void setLocalBounds(int x, int y, int width, int height)
    {	int bs = Math.max(9,b.ncols);
        int ncols = bs+7; // more cells wide to allow for the aux displays
        int nrows = bs+4 ;  
        int cellw = width / ncols;
        int chatHeight = selectChatHeight(height);
        int cellh = (height-chatHeight) / nrows;
        
        CELLSIZE = Math.max(2,Math.min(cellw, cellh)); //cell size appropriate for the aspect ratio of the canvas
        BOARDCELLSIZE = CELLSIZE;
        if(b.ncols<9) 
        	{ // slightly ad hoc, makes the size 7 board look similar to 9 and 11
        	  // technically, this should be 9*boardcellsize/7
        	BOARDCELLSIZE=(int)((8.7*BOARDCELLSIZE)/7.0); 
  
        	}
        fullRect.x = 0;			// the whole canvas
        fullRect.y = 0;
        fullRect.width = width;
        fullRect.height = height;
        
        boardRect.x = CELLSIZE;		// the main board
        boardRect.y = chatHeight+CELLSIZE/2;
        boardRect.width = CELLSIZE*(bs+1);
        boardRect.height = CELLSIZE *(bs+1 );

        stateRect.x = CELLSIZE/2;
        stateRect.y = chatHeight;
        stateRect.height = CELLSIZE/2;
        stateRect.width = boardRect.width;
        
        chatRect.x = fullRect.x;		// the chat area
        chatRect.y = fullRect.y;
        chatRect.width = fullRect.width-CELLSIZE*6;
        chatRect.height = chatHeight;

        logRect.x = chatRect.x + chatRect.width+CELLSIZE/2 ;	// the game log
        logRect.y = chatRect.y ;
        logRect.width = CELLSIZE * 5;
        logRect.height = chatRect.height;


		//this sets up the "vcr cluster" of forward and back controls.
        SetupVcrRects(CELLSIZE/4,  G.Bottom(boardRect)+CELLSIZE/2, 4*CELLSIZE, 2 * CELLSIZE);
        auxSRect.max = 3.0;		// max scale on the aux sliders
        
        goalRect.x = CELLSIZE * 2;		// really just a general message
        goalRect.y = boardRect.y + boardRect.height;
        goalRect.height = CELLSIZE/2;
        goalRect.width = CELLSIZE*9;
        
        progressRect.x = goalRect.x+goalRect.width/6;	// a simple progress bar when the robot is running.
        progressRect.width = goalRect.width/2;
        progressRect.y = G.Bottom(goalRect);
        progressRect.height = CELLSIZE/4;



        {
            commonPlayer pl0 = getPlayerOrTemp(0);
            commonPlayer pl1 = getPlayerOrTemp(1);
            Rectangle p0time = pl0.timeRect;
            Rectangle p1time = pl1.timeRect;
            Rectangle p0anim = pl0.animRect;
            Rectangle p1anim = pl1.animRect;
            Rectangle firstPlayerRect = pl0.nameRect;
            Rectangle firstPlayerPicRect = pl0.picRect;
            Rectangle secondPlayerPicRect = pl1.picRect;
            Rectangle secondPlayerRect = pl1.nameRect;

            //first player name
            firstPlayerRect.x = G.Right(boardRect)+CELLSIZE;
            firstPlayerRect.y = boardRect.y;
            firstPlayerRect.width = CELLSIZE * 3;
            firstPlayerRect.height = CELLSIZE;
            
            //second player name
            secondPlayerRect.x = firstPlayerRect.x;
            secondPlayerRect.y = G.Bottom(boardRect)+CELLSIZE/2;
            secondPlayerRect.width = firstPlayerRect.width;
            secondPlayerRect.height = firstPlayerRect.height;
            
            silverShipRect.x = firstPlayerRect.x;
            silverShipRect.y = G.Bottom(firstPlayerRect)+CELLSIZE/3;
            silverShipRect.width = BOARDCELLSIZE;
            silverShipRect.height = BOARDCELLSIZE;
            // first player portrait
            firstPlayerPicRect.x = G.Right(firstPlayerRect)+CELLSIZE;
            firstPlayerPicRect.y = firstPlayerRect.y ;
            firstPlayerPicRect.width = CELLSIZE * 3;
            firstPlayerPicRect.height = CELLSIZE * 3;

            goldShipRect.x = silverShipRect.x;
            goldShipRect.y = secondPlayerRect.y-silverShipRect.height;
            goldShipRect.width = silverShipRect.width;
            goldShipRect.height = silverShipRect.height;
            
            goldFlagRect.x =secondPlayerRect.x-CELLSIZE*2;
            goldFlagRect.y = secondPlayerRect.y+CELLSIZE/2;
            goldFlagRect.width = silverShipRect.width;
            goldFlagRect.height = silverShipRect.height;
          

            // player 2 portrait
            secondPlayerPicRect.x = firstPlayerPicRect.x;
            secondPlayerPicRect.height = firstPlayerPicRect.height;
            secondPlayerPicRect.y = G.Bottom(secondPlayerRect)-firstPlayerPicRect.height;
            secondPlayerPicRect.width = firstPlayerPicRect.width;
                 
            // time dispay for first player
            p0time.x = G.Right(silverShipRect)+CELLSIZE/4;
            p0time.y = silverShipRect.y;
            p0time.width = CELLSIZE * 2;
            p0time.height = CELLSIZE;
            
            // first player "i'm alive" anumation ball
            p0anim.x = G.Right(firstPlayerRect)+CELLSIZE/4;
            p0anim.y = firstPlayerPicRect.y+CELLSIZE/4;
            p0anim.width = CELLSIZE/2;
            p0anim.height = CELLSIZE/2;
            // time dispay for second player
            p1time.x = p0time.x;
            p1time.y = goldShipRect.y ;
            p1time.width = p0time.width;
            p1time.height = p0time.height;
            
            p1anim.x = p0anim.x;
            p1anim.y = secondPlayerRect.y+CELLSIZE/4;
            p1anim.width = p0anim.width;
            p1anim.height = p0anim.height;
            
    		flagCaptureRect.x = G.Right(editRect)+CELLSIZE/2;	// flagship can capture
            flagCaptureRect.y = G.Bottom(firstPlayerPicRect)+CELLSIZE/2;
            flagCaptureRect.width = CELLSIZE*8;
            flagCaptureRect.height = CELLSIZE;
            
            flagFoursideRect.x = flagCaptureRect.x;
            flagFoursideRect.y = G.Bottom(flagCaptureRect)+CELLSIZE/4;
            flagFoursideRect.width = flagCaptureRect.width;
            flagFoursideRect.height = flagCaptureRect.height;
            
            flagOwnRect.x = flagFoursideRect.x;
            flagOwnRect.y = G.Bottom(flagFoursideRect)+CELLSIZE/4;
            flagOwnRect.width = flagFoursideRect.width;
            flagOwnRect.height = flagFoursideRect.height;

            flagshipWinRect.x = flagOwnRect.x;
            flagshipWinRect.y = G.Bottom(flagOwnRect)+CELLSIZE/4;
            flagshipWinRect.width = flagOwnRect.width;
            flagshipWinRect.height = flagOwnRect.height;
       
        	
            }


        // "edit" rectangle, available in reviewers to switch to puzzle mode
        doneRect.x = G.Right(boardRect)+CELLSIZE;
        doneRect.y = goldShipRect.y-CELLSIZE*2;
        doneRect.width = CELLSIZE*3;
        doneRect.height = CELLSIZE;
       
        
        // "done" rectangle, should alway be visible, but only active when a move is complete.
        editRect.x = doneRect.x;
        editRect.y = G.Bottom(silverShipRect);
        editRect.width = doneRect.width;
        editRect.height = doneRect.height;

        // swaprect is reprect too
		swapRect.x = G.Right(boardRect)+CELLSIZE;	//the "swap colors" button which appears briefly
		swapRect.y = doneRect.y-3*CELLSIZE/2;
		swapRect.width = CELLSIZE * 3;
		swapRect.height = CELLSIZE ;

  
        theChat.setBounds(chatRect.x+x,chatRect.y+y,chatRect.width,chatRect.height);
        theChat.setVisible(true);
        generalRefresh();
    }


   public void drawSprite(Graphics g,int obj,int xp,int yp)
   {	TabChip.getChip(obj).drawChip(g,this,BOARDCELLSIZE,xp,yp,null);
  
   }
   // also related to sprites,
   // default position to display static sprites, typically the "moving object" in replay mode
   public Point spriteDisplayPoint()
	{   return(new Point(G.Right(boardRect)-(BOARDCELLSIZE/4),G.Bottom(boardRect)-(BOARDCELLSIZE/4)));
	}


    /* draw the deep unchangable objects, including those that might be rather expensive
     * to draw.  This background layer is used as a backdrop to the rest of the activity.
     * in our cease, we draw the board and the chips on it. 
     * */
    private void drawFixedElements(Graphics gc, TabGameBoard gb,Rectangle brect)
    {boolean review = reviewMode() && !mutable_game_record;
      // erase
      gb.SetDisplayRectangle(brect); 	   // this is necessary to inform disb of the board geometry

      gc.setColor(review ? reviewModeBackground : boardBackgroundColor);
      G.tileImage(gc,textures[BACKGROUND_TILE_INDEX], fullRect, this);   
      if(review)
      {	 
        G.tileImage(gc,textures[BACKGROUND_REVIEW_INDEX],boardRect, this);   
      }
       
      // draw a picture of the board. In this version we actually draw just the grid
      // to draw the cells, set gb.Drawing_Style in the board init method
      gb.DrawGrid(gc, brect, use_grid, boardBackgroundColor, RingFillColor, RingTextColor,GridColor);

      // draw the tile grid.  The positions are determined by the underlying board
      // object, and the tile itself if carefully crafted to tile the hex board
      // when drawn this way.  For the current Hex graphics, we could use the
      // simpler loop for(HexCell c = b.allCells; c!=null; c=c.next) {}
      // but for more complex graphics with overlapping shadows or stacked
      // objects, this double loop is useful if you need to control the
      // order the objects are drawn in.
      int lastincol = gb.ncols;
       for (int col = lastincol-1; col >= 0; col--)
       {
           char thiscol = (char) ('A' + col);
           for (int thisrow = lastincol-1; thisrow >= 0; thisrow--) // start at row 1 (0 is the grid) 
           { //where we draw the grid
        	  TabCell cell = b.getCell(thiscol,thisrow+1);
              int ypos = (brect.y + brect.height) - gb.cellToY(thiscol, thisrow+1);
              int xpos = brect.x + gb.cellToX(thiscol, thisrow);
              TabChip tile = TabChip.HexTile;
              if(cell.flagArea) { tile = TabChip.HexTile_Gold2; }
              else if(cell.centerArea) { tile = TabChip.HexTile_Gold; }
              tile.drawChip(gc,this,(int)(BOARDCELLSIZE*0.8),xpos,ypos,null);
           }
       }
 
    }

    public commonMove EditHistory(commonMove m)
    {	return(EditHistory(m,m.op==MOVE_SETOPTION));
    }
    
   /* draw the board and the chips on it. the gc will normally draw on a background
    * array which contains the slowly changing part of the board. 
    * */
    private void drawBoardElements(Graphics gc, TabGameBoard gb, Rectangle brect, HitPoint highlight)
    {
        //
        // now draw the contents of the board and highlights or ornaments.  We're also
    	// called when not actually drawing, to determine if the mouse is pointing at
    	// something which might allow an action.  Either gc or highlight might be
    	// null, but not both.
        //

        // using closestCell is preferable to G.PointInside(highlight, xpos, ypos, CELLRADIUS)
        // because there will be no gaps or overlaps between cells.
        TabCell closestCell = gb.closestCell(highlight,brect);
        boolean hitCell = gb.LegalToHitBoard(closestCell);
         if(hitCell)
        { // note what we hit, row, col, and cell
          highlight.hitCode = (closestCell.chip == null) ? TabId.EmptyBoardLocation : TabId.BoardLocation;
          highlight.hitObject = closestCell;
          highlight.col = closestCell.col;
          highlight.row = closestCell.row;
          highlight.arrow = (closestCell.chip == null)?StockArt.DownArrow:StockArt.UpArrow;
          highlight.awidth = CELLSIZE;
        }
        // this enumerates the cells in the board in an arbitrary order.  A more
        // conventional double xy loop might be needed if the graphics overlap and
        // depend on the shadows being cast correctly.
        if (gc != null)
        {
        Hashtable<TabCell,TabCell> captures = gb.droppedObjectCaptures();
         for(TabCell cell = gb.allCells; cell!=null; cell=cell.next)
          {
            boolean drawhighlight = (hitCell && (cell==closestCell)) 
   				|| gb.isDest(cell) 		// is legal for a "drop" operation
   				|| gb.isSource(cell);	// is legal for a "pick" operation+
            boolean drawSomething = drawhighlight || (cell.chip!=null);
            if(drawSomething)
            {
             TabChip ch = cell.topChip();
         	 int ypos = (brect.y + brect.height) - gb.cellToY(cell);
             int xpos = brect.x + gb.cellToX(cell);
  
             if (drawhighlight)
             { // checking for pointable position
            	TabChip.Selection.drawChip(gc,this,BOARDCELLSIZE,xpos,ypos,null);
             }

             if (ch!=null)
             {
              ch.drawChip(gc,this,BOARDCELLSIZE,xpos,ypos,null);
              if(captures.get(cell)!=null)
              {
              StockArt.SmallX.drawChip(gc,this,BOARDCELLSIZE*2,xpos,ypos,null);
              }
              }
            }

            }
        }
    }
	// draw a box of spare chips. For hex it's purely for effect.
    private void drawShipRect(Graphics gc, TabGameBoard gb,Rectangle r, TabCell pool, HitPoint highlight)
    {	TabChip ch = pool.topChip();
        boolean canhit = b.LegalToHitChips(pool) && G.pointInRect(highlight, r);
        if (canhit)
        {
            highlight.hitCode = pool.rackLocation;
        }

        if (gc != null)
        { 
        	int cx = r.x+CELLSIZE/2;
        	int cy = r.y+BOARDCELLSIZE/2;
           if(canhit)
           {
            TabChip.Selection.drawChip(gc,this,BOARDCELLSIZE,cx,cy,null);
           }
           ch.drawChip(gc,this,BOARDCELLSIZE,cx,cy,null);
        }
    }
    
    private boolean drawOptionRect(Graphics gc,HitPoint hp,TabGameBoard gb,Rectangle r,TabId op)
    {   boolean hit = G.pointInRect(hp,r);
		if(hit) { hp.hitCode = op; }
    	if(gc!=null)
    	{
    	int half = r.height/2;
    	boolean val = gb.getOptionValue(op);
    	Color selectColor = hit?Color.red:Color.gray;
    	String trueVal = "  "+s.get(op.trueName);
    	String falseVal = "  "+s.get(op.falseName);
    	G.Text(gc,false,r.x,r.y,r.width,half,val?Color.black:selectColor,val?Color.white:null,trueVal);
       	G.Text(gc,false,r.x,r.y+half,r.width,r.height/2,val?selectColor:Color.black,val?null:Color.white,falseVal);
    	G.frameRect(gc,Color.black,r);
    	}
    	return(false);
    }
    
    /*
     * draw the main window and things on it.  
     * If gc!=null then actually draw, 
     * If selectPos is not null, then as you draw (or pretend to draw) notice if
     * you are drawing under the current position of the mouse, and if so if you could
     * click there to do something.  Care must be taken to consider if a click really
     * ought to be allowed, considering spectator status, use of the scroll controls,
     * if some board token is already actively moving, and if the game is active or over.
     * 
     * This dual purpose (draw, and notice mouse sensitive areas) tends to make the
     * code a little complicated, but it is the most reliable way to make sure the
     * mouse logic is in sync with the drawing logic.
     * 
    General GUI checklist

    vcr scroll section always tracks, scroll bar drags
    lift rect always works
    zoom rect always works
    drag board always works
    pieces can be picked or dragged
    moving pieces always track
    stray buttons are insensitive when dragging a piece
    stray buttons and pick/drop are inactive when not on turn
*/
    public void redrawBoard(Graphics gc, HitPoint selectPos)
    {  TabGameBoard gb = b;
       if(gc!=null)
    	   {  // maybe select an alternate board for debugging robot play.  This alternate
    	      // board is not really intended for continuous display, or mouse sensitivity,
    	   	  // but it is absolutely invaluable to be able to see a representation of the
    	   	  // robot's current board when debugging some tricky bit of the robot player.
    	      TabGameBoard disb = (TabGameBoard) disB();
    	      if(disb!=null) 
    	      { gb = disb; 
    	    	G.frameRect(gc,Color.black,boardRect); // give a visual indicator
    	      }
    	   }
       TablutState state = gb.getState();
       boolean moving = (getMovingObject()>=0);
       gb.SetDisplayRectangle(boardRect); 	   // this is necessary to inform disb of the board geometry
       // 
       // if it is not our move, we can't click on the board or related supplies.
       // we accomplish this by supressing the highlight pointer.
       //
       HitPoint ourTurnSelect = OurMove() ? selectPos : null;
       //
       // even if we can normally select things, if we have already got a piece
       // moving, we don't want to hit some things, such as the vcr group
       //
       HitPoint buttonSelect = moving ? null : ourTurnSelect;
       // hit anytime nothing is being moved, even if not our turn or we are a spectator
       HitPoint nonDragSelect = (moving && !reviewMode()) ? null : selectPos;
       
       redrawGameLog(gc, nonDragSelect, logRect, boardBackgroundColor);
       drawBoardElements(gc, gb, boardRect, ourTurnSelect);
       drawShipRect(gc,gb,silverShipRect,b.playerChipPool(FIRST_PLAYER_INDEX),selectPos);
       drawShipRect(gc,gb,goldShipRect,b.playerChipPool(SECOND_PLAYER_INDEX),selectPos);
       
       {
       HitPoint sp = selectPos;
       switch(state)
       {	
       case PUZZLE_STATE:
    	   if(gb.moveNumber>1) { sp=null; }
    	   break;
       case REARRANGE_GOLD_STATE:
       case REARRANGE_SILVER_STATE: 
    	   break;
       default: sp=null;
       }
       drawOptionRect(gc,sp,gb,flagshipWinRect,TabId.CornerWin);
       drawOptionRect(gc,sp,gb,flagOwnRect,TabId.ExclusiveCenter);
       drawOptionRect(gc,sp,gb,flagCaptureRect,TabId.FlagShipCaptures);
       drawOptionRect(gc,sp,gb,flagFoursideRect,TabId.FourSideCaptures);
       }
 
       if((state==TablutState.PUZZLE_STATE) || (state==TablutState.GAMEOVER_STATE))
       	{ drawShipRect(gc,gb,goldFlagRect,b.goldFlagPool,selectPos);
       	}
       if (gc != null)
       {
           gc.setFont(standardBoldFont);
       }
       drawPlayerStuff(gc,(state==TablutState.PUZZLE_STATE)?buttonSelect:null,
	   			HighlightColor, rackBackGroundColor);

       
       // draw the board control buttons 
		if((state==TablutState.CONFIRM_SWAP_STATE) 
			|| (state==TablutState.PLAY_OR_SWAP_STATE) 
			|| (state==TablutState.PUZZLE_STATE))
		{ // make the "swap" button appear if we're in the correct state
			if(G.handleRoundButton(gc, swapRect, buttonSelect, s.get(SwapAction),
                HighlightColor, rackBackGroundColor))
			{ buttonSelect.hitCode = DefaultId.HitSwapButton;
			}
		}

		if (state != TablutState.PUZZLE_STATE)
        {	// if in any normal "playing" state, there should be a done button
			// we let the board be the ultimate arbiter of if the "done" button
			// is currently active.
            if (G.handleRoundButton(gc, doneRect, 
            		(gb.DoneState() ? buttonSelect : null), s.get(DoneAction),
                    HighlightColor, rackBackGroundColor))
            {	// always display the done button, but only make it active in
            	// the appropriate states
                buttonSelect.hitCode = DefaultId.HitDoneButton;
            }
            if (allowed_to_edit)
            {	// reviewer is active if there was a game here, and we were a player, 
            	// or all the time in review rooms.
                if (G.handleRoundButton(gc, editRect, buttonSelect, s.get(EditAction),
                            HighlightColor, rackBackGroundColor))
                {
                    buttonSelect.hitCode = DefaultId.HitEditButton;
                }
 
            }      
            }



 
        if (gc != null)
        {	// draw the avatars
            standardGameMessage(gc,
            		state==TablutState.GAMEOVER_STATE?gameOverMessage():s.get(state.getDescription()),
            				state!=TablutState.PUZZLE_STATE,
            				gb.whoseTurn,
            				stateRect);
            goalAndProgressMessage(gc,selectPos,s.get(GoalString),progressRect, goalRect);
            DrawRepRect(gc,b.Digest(),repRect);
        }
        // draw the vcr controls
        drawVcrGroup(nonDragSelect, gc, HighlightColor, vcrButtonColor);

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
    	 // record some state so the game log will look pretty
        switch(b.getState())
        {
        case PUZZLE_STATE:
        case REARRANGE_GOLD_STATE:
        case REARRANGE_SILVER_STATE:
	    	{   mm.setSliderNumString("--");
	    		switch(mm.op)
	        	{
	    		case MOVE_MOVE:
	        	case MOVE_PICK: 
	        	case MOVE_PICKB: 
	        		break;
	    		default:
	    			mm.setLineBreak(true);
	        	}
	    	}
	    	break;
	    default: ;
        }
        handleExecute(b,mm,replay);
        if(replay!=replayMode.Replay) { playSounds((Tabmovespec)mm); }
		lastDropped = b.lastDroppedDest;	// this is for the image adjustment logic
       return (true);
    }
     void playSounds(Tabmovespec mm)
     {
   	  switch(mm.op)
   	  {
   	  case MOVE_DROPB:
   	  case MOVE_PICKB:
   	  case MOVE_PICK:
   	  case MOVE_DROP:
   		  playASoundClip(light_drop,100);
   		  break;
   	  default: break;
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
        return (new Tabmovespec(st, -1));
    }

    

/**
 * the preferred mouse gesture style is to let the user "pick up" objects
 * by simply clicking on them, but we also allow him to click and drag. 
 * StartDragging is called when he has done this.
 */
    public void StartDragging(HitPoint hp)
    {
        TabChip mo = b.pickedObject;
        if ((mo==null) && (hp.hitCode instanceof TabId))// not dragging anything yet, so maybe start
        {

        TabId hitObject =  (TabId)hp.hitCode;
 	    switch(hitObject)
	    {
	    default: break;
	    
        case GoldFlagLocation:
	    case SilverShipLocation:
	    case GoldShipLocation:
	    	PerformAndTransmit("Pick "+hitObject.shortName);
	    	break;
	    case BoardLocation:
	    	PerformAndTransmit("Pickb "+hp.col+" "+hp.row);
	    	break;
        }

        if (b.movingObjectIndex() >= 0)
	        {	// if we got something started, inform the mouse handler
	            hp.dragging = true;
	        } 
         }
    }
	private void doDropChip(char col,int row)
	{	TablutState state = b.getState();
		switch(state)
		{
		default: G.Error("Not expecting state "+state);
		case PUZZLE_STATE:
			{
			TabChip mo = b.lastPicked();
			if(mo!=null) { PerformAndTransmit("dropb "+mo.chipName+" "+col+" "+row); }
			}
			break;
		case CONFIRM_STATE:
		case CONFIRM_NOSWAP_STATE:
		case PLAY_STATE:
		case REARRANGE_GOLD_STATE:
		case REARRANGE_SILVER_STATE:
		case PLAY_OR_SWAP_STATE:
			PerformAndTransmit("dropb "+b.getPlayerChip(b.whoseTurn).chipName	+ " "+col+" "+row);
			break;
					                 
		
		}
	}
	/** 
	 * this is called on "mouse up".  We may have been just clicking
	 * on something, or we may have just finished a click-drag-release.
	 * We're guaranteed just one mouse up, no bounces.
	 */
    public void StopDragging(HitPoint hp)
    {
        CellId id = hp.hitCode;
        if(!(id instanceof TabId)) {   missedOneClick = performStandardActions(hp,missedOneClick);}
    	else {
    	missedOneClick = false;
    	TabId hitCode = (TabId)hp.hitCode;
        TabCell hitObject = (TabCell)hp.hitObject;
        TablutState state = b.getState();
        switch (hitCode)
        {
        default:
                G.Error("Hit Unknown object " + hitObject);
        	break;
        case BoardLocation:	// we hit an occupied part of the board 
			switch(state)
			{
			default: G.Error("Not expecting drop on filled board in state "+state);
			case CONFIRM_STATE:
			case PLAY_STATE:
			case PLAY_OR_SWAP_STATE:
				if(!b.isDest(hitObject))
					{
					// note that according to the general theory, this shouldn't
					// ever occur because inappropriate spaces won't be mouse sensitve.
					// this is just defense in depth.
					G.Error("shouldn't hit a chip in state "+state);
					}
				// fall through and pick up the previously dropped piece
			case PUZZLE_STATE:
				PerformAndTransmit("Pickb "+hp.col+" "+hp.row);
				break;
			}
			break;
			
        case EmptyBoardLocation:
			switch(state)
			{
				default:
					G.Error("Not expecting hit in state "+state);
				case CONFIRM_STATE:
				case PLAY_STATE:
				case CONFIRM_NOSWAP_STATE:
				case PLAY_OR_SWAP_STATE:
				case PUZZLE_STATE:
				case REARRANGE_GOLD_STATE:
				case REARRANGE_SILVER_STATE:
					doDropChip(hp.col,hp.row);
					break;
			}
			break;
        case SilverShipLocation:
        case GoldShipLocation:
        case GoldFlagLocation:
        	PerformAndTransmit("Drop "+b.pickedObject.chipName);
             break;
        case CornerWin:
        case FlagShipCaptures:
        case ExclusiveCenter:
        case FourSideCaptures:
         	{
        	boolean newval = !b.getOptionValue(hitCode);
        	PerformAndTransmit("SetOption "+hitCode.shortName+" "+newval);
        	}
        	break;
        }
        }

         repaint(20);
    }


    
    // draw the fixed elements, using the saved background if it is available
    // and believed to be valid.
    public void drawFixedElements(Graphics offGC,boolean complete)
    {
      	complete |= createAllFixed(fullRect.width, fullRect.height); // create backing bitmaps;
     	complete |= b.changedOptions();
     	Image allFixed = allFixed();
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
     * 
     * if complete is true, we definitely want to start from scratch, otherwise
     * only the known changed elements need to be painted.  Exactly what this means
     * is game specific, but for hex the underlying empty board is cached as a deep
     * background, but the chips are painted fresh every time.
     * 
     * this used to be very important to optimize, but with faster machines it's
     * less important now.  The main strategy we employ is to paint EVERYTHING
     * into a background bitmap, then display that bitmap to the real screen
     * in one swell foop at the end.
     */
    public void paintCanvas(Graphics g, boolean complete,HitPoint hp)
    {
    	// three ways to do this, 
    	// Preferred: with a "deep background" screen of fixed elements. 
    	//		This is theoretically the lowest overhead and maybe noticably
    	//		better performance on older machines, but requires two bitmaps
    	//		the size of the whole window.
    	// Second: with only an immediate bitmap.  This uses one bitmap but requires
    	//		everything to be drawn every time.  If your backgrounds are elaboate
    	//		this might be noticably poor performance.
    	// Third: directly to the screen.  This is definitely not recommended, unless
    	// 		perhaps you are debugging some deep mystery about what is shown where.
    	// 		it's very "flashy" in a bad way.
    	// note that on machines in memory trouble, an intermediate state
    	// to total failure might have one or both of these bitmaps 
    	// involuntarily unavailable.
    	//
      	Image offScreen = createOffScreen(fullRect.width, fullRect.height); // create the intermediate bitmap
      	Graphics offGC = (offScreen==null) ? g : offScreen.getGraphics();
      	G.setClip(offGC,fullRect);
        drawFixedElements(offGC,complete);	// draw the board into the deep background
   	
    	// draw the board contents and changing elements.
        redrawBoard(offGC,hp);
        //      draw clocks, sprites, and other ephemera
        drawClocksAndMice(offGC, null);
        DrawTileSprite(offGC,hp); //draw the floating tile we are dragging, if present
        DrawArrow(offGC,hp);
        // these are optional display hacks for debugging the applet,
        // enabled by menu options under "extraactions" control.
        //
        ShowStats(offGC,vcrRect.x+vcrRect.width+10,vcrRect.y+vcrRect.height/2);	// add some stats on top of everything
 
        showRectangles(offGC, CELLSIZE); //show rectangles in the UI
        drawSprites(offGC);
        
        if(offScreen!=null)
        	{ // display the completed result if it was drawn into a backing bitmap rather than directly
        	displayClipped(g,fullRect,chatRect,offScreen);
        	}
    }
    
    // return what will be the init type for the game
    public String gameType() // this is the subgame "setup" within the master type.
    	{ return(b.gameType()); 
    	}	
    public String sgfGameType() { return(Tablut_SGF); }	// this is the official SGF number assigned to the game
    public void performHistoryInitialization(StringTokenizer his)
    {   //the initialization sequence
    	String token = his.nextToken();
    	String initToken = token;
    	do { String nt = his.nextToken();
    		 if(ENDOPTIONS.equals(nt)) { break; }
    		 initToken += " " + nt;
    		} while(true);

        b.doInit(initToken);
       // G.Error("Check this");
     }

/** handle the run loop, and any special actions we need to take.
 * The mouse handling and canvas painting will be called automatically.
 * 
 * This is a good place to make notes about threads.  Threads in Java are
 * very dangerous and tend to lead to all kinds of undesirable and/or flakey
 * behavior.  The fundamental problem is that there are three or four sources
 * of events from different system-provided threads, and unless you are very
 * careful, these threads will all try to use and modify the same data
 * structures at the same time.   Java "synchronized" declarations are
 * hard to get right, resulting in synchronization locks, or lack of
 * synchronization where it is really needed.
 * 
 * This toolkit addresses this problem by adopting the "one thread" model,
 * and this is where it is.  Any other threads should do as little as possible,
 * mainly leave breadcrumbs that will be picked up by this thread.
 * 
 * In particular:
 * GUI events do not respond in the native thread.  Mouse movement and button
 * events are noted for later.  Requests to repaint the canvas are recorded but
 * not acted upon.
 * Network I/O events, merely queue the data for delivery later.
 *  */
    
    public void ViewerRun()
    {
        super.ViewerRun();
    }
    /**
     * returns true if the game is over "right now", but also maintains 
     * the gameOverSeen instance variable and turns on the reviewer variable
     * for non-spectators.
     */
    //public boolean GameOver()
    //{	// the standard method calls b.GameOver() and maintains
    	// two variables.  
    	// "reviewer=true" means we were a player and the end of game has been reached.
    	// "gameOverSeen=true" means we have seen a game over state 
    //	return(super.GameOver());
    //}
    
    /** this is used by the stock parts of the canvas machinery to get 
     * access to the default board object.
     */
    public BoardProtocol getBoard()   {    return (b);   }

    //** this is used by the game controller to supply entertainment strings to the lobby */
    // public String gameProgressString()
    // {	// this is what the standard method does
    // 	// return ((mutable_game_record ? Reviewing : ("" + viewMove)));
    // 	return(super.gameProgressString());
    // }


/** this is used by the scorekeeper to determine who won. Draws are indicated
 * by both players returning false.  Be careful not to let both players return true!
 */
    public boolean WinForPlayer(commonPlayer p)
    { // this is what the standard method does
      // return(getBoard().WinForPlayer(p.index));
      return (super.WinForPlayer(p));
    }

    /** factory method to create a robot */
    public SimpleRobotProtocol newRobotPlayer() 
    {  int level = sharedInfo.getInt(exHashtable.ROBOTLEVEL,0);
       switch(level)
       { default: G.Error("not defined");
       	 case 0:	return(new TabPlay());
       }
    }

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
            {
                b.doInit(value);
             }
            else if (name.equals(comment_property))
            {
                comments += value;
            }
            else if (name.equals(game_property))
            {
                if (!(value.equals(Tablut_SGF)))
                {
                	G.Error("game type " + value + " is not this game");
                }
            }
            else if (parseVersionCommand(name,value,2)) {}
            else if (parsePlayerCommand(name,value)) {}
            else
            {	// handle standard game properties, and also publish any
            	// unexpected names in the chat area
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
