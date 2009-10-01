package hex;

import online.common.*;


import java.awt.*;
import java.util.*;
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
 *
 * This is intended to be maintained as the reference example how to interface to boardspace.
 *
 * The overall structure here is a collection of classes specific to Hex, which extend
 * or use supporting online.game.* classes shared with the rest of the site.  The top level 
 * class is a Canvas which implements ViewerProtocol, which is created by the game manager.  
 * The game manager has very limited communication with this viewer class, but manages
 * all the error handling, communication, scoring, and general chatter necessary to make
 * the game part of the site.
 * 
 * The main classes are:
 *  HexGameViewer - this class, a canvas for display and mouse handling
 *  HexGameBoard - board representaion and implementation of the game logic
 *  Hexmovespec - representation, parsing and printing of move specifiers
 *  HexPlay - a robot to play the game
 *  HexConstants - static constants shared by all of the above.  
 *  
 *  The primary purpose of the HexGameViewer class is to do the actual
 *  drawing and to mediate the mouse gestures.  All the actual work is 
 *  done in an event loop, rather than in direct reposonse to mouse or
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
 *  5) revert the original "Hex" hierarchy in case eclipse got carried away.
*/
public class HexGameViewer extends commonCanvas 
	implements ViewerProtocol, HexConstants, sgf_names
{	static final long serialVersionUID = 1000;
     // colors
    private Color reviewModeBackground = new Color(220,165,200);
    private Color HighlightColor = new Color(0.2f, 0.95f, 0.75f);
    private Color RingFillColor = new Color(10, 163, 190);
    private Color RingTextColor = Color.black;
    private Color GridColor = Color.black;
    private Color rackBackGroundColor = new Color(225,192,182);
    private Color boardBackgroundColor = new Color(220,165,155);
    private Color vcrButtonColor = new Color(0.7f, 0.7f, 0.75f);

    // images, shared among all instances of the class so loaded only once
    private static Image[] tileImages = null; // tile images
    private static Image[] borders = null;// border tweaks for the images
    private static Image[] textures = null;// background textures
    
    // private state
    private HexGameBoard bb = null; //the board from which we are displaying
    private int CELLSIZE; 	//size of the layout cell
 
    // addRect is a service provided by commonCanvas, which supports a mode
    // to visualize the layout during development.  Look for "show rectangles"
    // in the options menu.
    //public Rectangle fullRect = addRect("fullRect"); //the whole viewer area
    //public Rectangle boardRect = addRect("boardRect"); //the actual board, normally at the left edge
    //public Rectangle chatRect = addRect("chatRect");
    private Rectangle logRect = addRect("logRect"); //the game log, normally off the the right
    private Rectangle stateRect = addRect("stateRect");
    private Rectangle doneRect = addRect("doneRect");
    private Rectangle editRect = addRect("editRect");
    private Rectangle goalRect = addRect("goalRect");
    //private Rectangle repRect = addRect("repRect");	// not needed for hex
    private Rectangle progressRect = addRect("progressRect");
    private Rectangle secondPlayerChipRect = addRect("secondPlayerChipRect");
    private Rectangle firstPlayerChipRect = addRect("firstPlayerChipRect");
	private Rectangle swapRect=addRect("swapRect");

	// private menu items
    private CheckboxMenuItem rotationOption = null;		// rotate the board view
    private boolean doRotation=true;					// current state
    private boolean lastRotation=!doRotation;			// user to trigger background redraw
    

    public void preloadImages()
    {	hexChip.preloadImages(this,ImageDir);
    	if (tileImages == null)
    	{ 	// note that for this to work correctly, the images and masks must be the same size.  
        	// Refer to http://www.andromeda.com/people/ddyer/java/imagedemo/transparent.html
    		
    		// images and textures are static variables, so they're shared by
    		// the entire class and only get loaded once.  Special synchronization
    		// tricks are used to make sure.
    	  tileImages = load_images(ImageDir,TileFileNames,load_images(ImageDir,TileFileNames,"-mask"));
          textures = load_images(ImageDir,TextureNames);
          borders = load_images(ImageDir,BorderFileNames,
        		  		load_images(ImageDir,BorderFileNames,"-mask"));
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
        // use_grid=reviewer;// use this to turn the grid letters off by default

        lockAndLoadImages(theRoot);		// this ends up calling preloadImages
        
        rotationOption = myFrame.addOption("rotate board",true,deferredEvents);
        
        bb = new HexGameBoard(info.getString(exHashtable.GAMETYPE, Hex_INIT));
        doInit(false);

        PerformAndTransmit(reviewOnly?"Edit":"Start P0", false,true);
    }

    /** 
     *  used when starting up or replaying and also when loading a new game 
     *  */
    public void doInit(boolean preserve_history)
    {
        //System.out.println(myplayer.trueName + " doinit");
        super.doInit(preserve_history);				// let commonViewer do it's things
        bb.doInit(bb.gametype);						// initialize the board
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
	 * 
	 * this is a debugging hack to give you an event based on clicking in the time
	 * clock.  You can take whatever action you like, or no action.
	 * */
    public boolean inTimeRect(int eventX, int eventY)
    {
        boolean val = super.inTimeRect(eventX, eventY);
        //if (val && extraactions)
        //{
        //    System.out.println(formHistoryString());
        //}
        return (val);
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
    {
        int ncols = 43; // more cells wide to allow for the aux displays
        int nrows = 23 ;  
        int cellw = width / ncols;
        chatHeight = selectChatHeight(height);
        int cellh = (height-chatHeight) / nrows;
        
        CELLSIZE = Math.max(2,Math.min(cellw, cellh)); //cell size appropriate for the aspect ratio of the canvas

        fullRect.x = 0;			// the whole canvas
        fullRect.y = 0;
        fullRect.width = width;
        fullRect.height = height;

        boardRect.x = 0;		// the main board
        boardRect.y = chatHeight;
        boardRect.width = CELLSIZE * (int)(nrows*1.5);
        boardRect.height = CELLSIZE * (nrows );

        stateRect.x = CELLSIZE/2;
        stateRect.y = boardRect.y+CELLSIZE/2;
        stateRect.height = CELLSIZE/2;
        stateRect.width = boardRect.width-CELLSIZE;
        
        logRect.width = CELLSIZE * 10;
        logRect.x = fullRect.width-logRect.width-CELLSIZE/2;	// the game log
        logRect.y = fullRect.y ;
        logRect.height = chatRect.height;

        chatRect.x = fullRect.x;		// the chat area
        chatRect.y = fullRect.y;
        chatRect.width = fullRect.width-logRect.width-CELLSIZE;
        chatRect.height = chatHeight;

 

		swapRect.x = boardRect.x + CELLSIZE;	//the "swap colors" button which appears briefly
		swapRect.y = boardRect.y+(doRotation?2:14)*CELLSIZE;
		swapRect.width = CELLSIZE * 5;
		swapRect.height = CELLSIZE ;

		// a pool of chips for the first player at the top
        firstPlayerChipRect.x = (boardRect.x + boardRect.width) - 8*CELLSIZE;
        firstPlayerChipRect.y = boardRect.y+CELLSIZE*2;
        firstPlayerChipRect.width = 5*CELLSIZE;
        firstPlayerChipRect.height = 2*CELLSIZE;
        // and for the second player at the bottom
		secondPlayerChipRect.x =firstPlayerChipRect.x;
		secondPlayerChipRect.y = boardRect.y+boardRect.height-firstPlayerChipRect.height-CELLSIZE/2;
		secondPlayerChipRect.width = firstPlayerChipRect.width;
		secondPlayerChipRect.height= firstPlayerChipRect.height;

		//this sets up the "vcr cluster" of forward and back controls.
        SetupVcrRects(CELLSIZE / 2,
            (boardRect.y + boardRect.height) - (3 * CELLSIZE), CELLSIZE * 5,
            3 * CELLSIZE);
        auxSRect.max = 3.0;		// max scale on the aux sliders
        
        goalRect.x = CELLSIZE * 3;		// really just a general message
        goalRect.y = G.Bottom(boardRect)-CELLSIZE*2;
        goalRect.height = CELLSIZE * 2;
        goalRect.width = 20 * CELLSIZE;
        
        progressRect.x = goalRect.x+goalRect.width/6;	// a simple progress bar when the robot is running.
        progressRect.width = goalRect.width/2;
        progressRect.y = goalRect.y;
        progressRect.height = CELLSIZE/2;

  
        {
            commonPlayer pl0 = players[0];
            commonPlayer pl1 = players[1];
            if((pl0!=null)&&(pl1!=null))
            {
            Rectangle p0time = pl0.timeRect;
            Rectangle p1time = pl1.timeRect;
            Rectangle p0anim = pl0.animRect;
            Rectangle p1anim = pl1.animRect;
            Rectangle firstPlayerRect = pl0.nameRect;
            Rectangle secondPlayerRect = pl1.nameRect;
            Rectangle firstPlayerPicRect = pl0.picRect;
            Rectangle secondPlayerPicRect = pl1.picRect;
            
            //first player name
            firstPlayerRect.x = G.Right(boardRect)-2*CELLSIZE;
            firstPlayerRect.y = boardRect.y+CELLSIZE / 2;
            firstPlayerRect.width = CELLSIZE * 6;
            firstPlayerRect.height = (3 * CELLSIZE) / 2;
            // first player portrait
            firstPlayerPicRect.x = firstPlayerRect.x;
            firstPlayerPicRect.y = G.Bottom(firstPlayerRect);
            firstPlayerPicRect.width = CELLSIZE * 6;
            firstPlayerPicRect.height = CELLSIZE * 6;
            // "edit" rectangle, available in reviewers to switch to puzzle mode
            editRect.x = G.Right(boardRect)+CELLSIZE/2;
            editRect.y = G.Bottom(firstPlayerPicRect)+CELLSIZE;
            editRect.width = CELLSIZE*4;
            editRect.height = 3*CELLSIZE/2;
           
     
            //second player name
            secondPlayerRect.x = firstPlayerRect.x;
            secondPlayerRect.y = G.Bottom(boardRect) - firstPlayerRect.height;
            secondPlayerRect.width = firstPlayerRect.width;
            secondPlayerRect.height = firstPlayerRect.height;


            // player 2 portrait
            secondPlayerPicRect.x = secondPlayerRect.x;
            secondPlayerPicRect.height = firstPlayerPicRect.height;
            secondPlayerPicRect.y = secondPlayerRect.y -
                firstPlayerPicRect.height;
            secondPlayerPicRect.width = firstPlayerPicRect.width;
            
            // time dispay for first player
            p0time.x = G.Right(firstPlayerRect);
            p0time.y = firstPlayerRect.y;
            p0time.width = CELLSIZE * 3;
            p0time.height = 3*CELLSIZE/2;
            // first player "i'm alive" anumation ball
            p0anim.x = p0time.x ;
            p0anim.y = firstPlayerPicRect.y;
            p0anim.width = CELLSIZE;
            p0anim.height = CELLSIZE;
            // time dispay for second player
            p1time.x = G.Right(secondPlayerRect);
            p1time.y = secondPlayerRect.y;
            p1time.width = p0time.width;
            p1time.height = p0time.height;
            
            p1anim.x = p1time.x;
            p1anim.y = G.Bottom(secondPlayerPicRect)-p0anim.height;
            p1anim.width = p0anim.width;
            p1anim.height = p0anim.height;
       	
            
            // "done" rectangle, should alway be visible, but only active when a move is complete.
            doneRect.x = editRect.x;
            doneRect.y = secondPlayerPicRect.y-editRect.height-CELLSIZE;
            doneRect.width = editRect.width;
            doneRect.height = editRect.height;
           }}
 
         // not needed for hex, but other games might differ
        //repRect.x = doneRect.x;
        //repRect.width = 5*CELLSIZE;
        //repRect.y = doneRect.y -CELLSIZE;
        //repRect.height = CELLSIZE;

        theChat.setBounds(chatRect.x+x,chatRect.y+y,chatRect.width,chatRect.height);
        theChat.setVisible(true);
        generalRefresh();
    }



	// draw a box of spare chips. For hex it's purely for effect.
    private void DrawChipPool(Graphics gc, Rectangle r, int player, HitPoint highlight,HexGameBoard gb)
    {
        boolean canhit = gb.LegalToHitChips(player) && G.pointInRect(highlight, r);
        if (canhit)
        {
            highlight.hitCode = gb.getPlayerColor(player);
            highlight.arrow = (gb.pickedObject!=null)?StockArt.DownArrow:StockArt.UpArrow;
            highlight.awidth = CELLSIZE;
        }

        if (gc != null)
        { // draw a random pile of chips.  It's just for effect

            int spacex = r.width - CELLSIZE;
            int spacey = r.height - CELLSIZE;
            int nc = 20;							 // draw 20 chips
            Random rand = new Random(4321 + player); // consistant randoms, different for black and white 

            if (canhit)
            {	// draw a highlight background if appropriate
                G.fillRect(gc, HighlightColor, r);
            }

            G.frameRect(gc, Color.black, r);
            hexChip chip = gb.getPlayerChip(player);
            while (nc-- > 0)
            {
                int rx = Math.abs(rand.nextInt()) % spacex;
                int ry = Math.abs(rand.nextInt()) % spacey;
                chip.drawChip(gc,this,CELLSIZE,r.x+CELLSIZE/2+rx,r.y+CELLSIZE/2+ry,null);
             }
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
    {
    	// draw an object being dragged
    	// use the board cell size rather than the window cell size
    	hexChip.getChip(obj).drawChip(g,this,CELLSIZE, xp, yp, null);
    }
    // also related to sprites,
    // default position to display static sprites, typically the "moving object" in replay mode
    //public Point spriteDisplayPoint()
    //{	BoardProtocol b = getBoard();
    //	int celloff = b.cellSize();
    //	return(new Point(G.Right(boardRect)-celloff,G.Bottom(boardRect)-celloff));
    //}  


    /* draw the deep unchangable objects, including those that might be rather expensive
     * to draw.  This background layer is used as a backdrop to the rest of the activity.
     * in our cease, we draw the board and the chips on it. 
     * */
    private void drawFixedElements(Graphics gc, HexGameBoard gb,Rectangle brect)
    { // erase
      gc.setColor(reviewMode() ? reviewModeBackground : boardBackgroundColor);
      //G.fillRect(gc, fullRect);
      G.tileImage(gc,textures[BACKGROUND_TILE_INDEX], 
    		  fullRect.x,fullRect.y, fullRect.width,fullRect.height, this);   
      if(reviewMode())
      {	 
        G.tileImage(gc,textures[BACKGROUND_REVIEW_INDEX],
        		boardRect.x,boardRect.y, boardRect.width,boardRect.height, this);   
      }
       
      // draw a picture of the board. In this version we actually draw just the grid
      // to draw the cells, set gb.Drawing_Style in the board init method.  Create a
      // DrawGridCoord(Graphics gc, Color clt,int xpos, int ypos, int cellsize,String txt)
      // on the board to fine tune the exact positions of the text
      gb.DrawGrid(gc, brect, use_grid, boardBackgroundColor, RingFillColor, RingTextColor,GridColor);

      // draw the tile grid.  The positions are determined by the underlying board
      // object, and the tile itself if carefully crafted to tile the hex board
      // when drawn this way.  For the current Hex graphics, we could use the
      // simpler loop for(HexCell c = b.allCells; c!=null; c=c.next) {}
      // but for more complex graphics with overlapping shadows or stacked
      // objects, this double loop is useful if you need to control the
      // order the objects are drawn in.
       for (int col = 0; col < gb.ncols; col++)
       {
           char thiscol = (char) ('A' + col);
           int lastincol = gb.nInCol[col];

           for (int thisrow0 = lastincol, thisrow = lastincol + gb.firstRowInCol[col];
           		thisrow0 >= 1;
           		thisrow0--, thisrow--) // start at row 1 (0 is the grid) 
           { //where we draw the grid
              int ypos = (brect.y + brect.height) - gb.cellToY(thiscol, thisrow);
              int xpos = brect.x + gb.cellToX(thiscol, thisrow);
              int hidx = lastRotation?HEXTILE_INDEX:HEXTILE_NR_INDEX;
              int bindex = lastRotation ? HEXTILE_BORDER_INDEX : HEXTILE_BORDER_NR_INDEX;
              double scale[] = TILESCALES[hidx];
              adjustScales(scale,null);		// adjust the tile size/position.  This is used only in development
              // to fine tune the board rendering.
              drawImage(gc,tileImages[hidx],scale,xpos,ypos,gb.CELLSIZE,lastRotation?0.80:0.86,0.0);
              
              // decorate the borders with darker and lighter colors.  The border status
              // of cells is precomputed, so each cell has a mask of which borders it needs.
              // in order to make the artwork as simple as possible to maintain, the border
              // pictures are derived directly from the hex cell masters, so they need the
              // same scale and offset factors as the main cell.
              hexCell c =(hexCell)gb.GetBoardCell(thiscol,thisrow);
              if(c.borders!=0)
              {
              for(int dir=0; dir<4;dir++)
              {	// precalculated border cell properties
            	  if((c.borders&(1<<dir))!=0)
            	  {
            		  drawImage(gc,borders[bindex+dir],scale,xpos,ypos,gb.CELLSIZE,lastRotation?0.80:0.86,0.0);
            	  }
              }}
          }
       }
 
    }

   /* draw the board and the chips on it. the gc will normally draw on a background
    * array which contains the slowly changing part of the board. 
    * */
    private void drawBoardElements(Graphics gc, HexGameBoard gb, Rectangle brect, HitPoint highlight)
    {
        //
        // now draw the contents of the board and highlights or ornaments.  We're also
    	// called when not actually drawing, to determine if the mouse is pointing at
    	// something which might allow an action.  Either gc or highlight might be
    	// null, but not both.
        //

        // using closestCell is preferable to G.PointInside(highlight, xpos, ypos, CELLRADIUS)
        // because there will be no gaps or overlaps between cells.
        hexCell closestCell = (highlight==null)?null
        		:(hexCell)gb.closestCell(highlight.x-brect.x,brect.height-(highlight.y-brect.y));
        boolean hitCell = gb.LegalToHitBoard(closestCell);
        if(hitCell)
        { // note what we hit, row, col, and cell
          boolean empty = (closestCell.chip == null);
          highlight.hitCode = empty ? EmptyBoard : BoardLocation;
          highlight.hitObject = closestCell;
          highlight.arrow = empty ? StockArt.DownArrow : StockArt.UpArrow;
          highlight.awidth = CELLSIZE;
          highlight.col = closestCell.col;
          highlight.row = closestCell.row;
        }
        // this enumerates the cells in the board in an arbitrary order.  A more
        // conventional double xy loop might be needed if the graphics overlap and
        // depend on the shadows being cast correctly.
        if (gc != null)
        {
        for(hexCell cell = (hexCell)gb.allCells; cell!=null; cell=(hexCell)cell.next)
          {
            boolean drawhighlight = (hitCell && (cell==closestCell)) 
   				|| gb.isDest(cell) 		// is legal for a "drop" operation
   				|| gb.isSource(cell);	// is legal for a "pick" operation+
            boolean drawSomething = drawhighlight || (cell.chip!=null);
            if(drawSomething)
            {
             hexChip chip = cell.topChip();
         	 int ypos = (brect.y + brect.height) - gb.cellToY(cell);
             int xpos = brect.x + gb.cellToX(cell);
  
             if (drawhighlight)
             { // checking for pointable position
                drawImage(gc, tileImages[SELECTION_INDEX],TILESCALES[SELECTION_INDEX], xpos, ypos, CELLSIZE, 1.0,0.0);
                
             }

             if (chip!=null)
             {
              chip.drawChip(gc,this,CELLSIZE,xpos,ypos,null); 
              }
            }}
        }
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
    {  HexGameBoard disb = (HexGameBoard)disB();
       HexGameBoard gb = (disb==null)?bb:disb;
       int state = gb.getBoardState();
       boolean moving = (getMovingObject()>=0);
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
       DrawChipPool(gc, secondPlayerChipRect, SECOND_PLAYER_INDEX, ourTurnSelect,gb);
       DrawChipPool(gc, firstPlayerChipRect, FIRST_PLAYER_INDEX, ourTurnSelect,gb);

       if (gc != null)
       {
           gc.setFont(standardBoldFont);
       }
       
       // draw the board control buttons 
		if((state==CONFIRM_SWAP_STATE) 
			|| (state==PLAY_OR_SWAP_STATE) 
			|| (state==PUZZLE_STATE))
		{ // make the "swap" button appear if we're in the correct state
			if(G.handleRoundButton(gc, swapRect, buttonSelect, s.get("Swap Colors"),
                HighlightColor, rackBackGroundColor))
			{ buttonSelect.hitCode = SwapButton;
			}
		}

		if (state != PUZZLE_STATE)
        {	// if in any normal "playing" state, there should be a done button
			// we let the board be the ultimate arbiter of if the "done" button
			// is currently active.
            if (G.handleRoundButton(gc, doneRect, 
            		(gb.DoneState() ? buttonSelect : null), s.get("Done"),
                    HighlightColor, rackBackGroundColor))
            {	// always display the done button, but only make it active in
            	// the appropriate states
                buttonSelect.hitCode = HitDoneButton;
            }
            if (allowed_to_edit)
            {	// reviewer is active if there was a game here, and we were a player, 
            	// or all the time in review rooms.
            	// we're allowed to edit the board, so we need an edit button
                    if (G.handleRoundButton(gc, editRect, buttonSelect, s.get("Edit"),
                                HighlightColor, rackBackGroundColor))
                    {
                        buttonSelect.hitCode = HitEditButton;
                    }
            }
        }

 
        drawPlayerStuff(gc,(state==PUZZLE_STATE)?buttonSelect:null,HighlightColor,rackBackGroundColor);
  
 
        if (gc != null)
        {	// draw the avatars
            standardGameMessage(gc,s.get(boardStates[state]),state!=PUZZLE_STATE,gb.whoseTurn,stateRect);
            goalAndProgressMessage(gc,s.get("connect opposite sides with a chain of markers"),progressRect,goalRect);
            //DrawRepRect(gc,gb.Digest(),repRect);	// Not needed for hex
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
     public boolean Execute(commonMove mm,boolean sounds)
    {	
    	 // record some state so the game log will look pretty
        if(bb.getBoardState()==PUZZLE_STATE)
    	{   mm.sliderNumString = "--";
    		switch(mm.op)
        	{
        	case MOVE_PICK: 
        	case MOVE_PICKB: 
        		break;
    		default:
    			mm.linebreak=true;
        	}
    	}
        handleExecute(bb,mm);
		lastDropped = bb.lastDroppedDest;	// this is for the image adjustment logic
		if(sounds) { playSounds((Hexmovespec)mm); }
       return (true);
    }
 void playSounds(Hexmovespec mm)
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
    public commonMove ParseNewMove(StringTokenizer st)
    {
        return (new Hexmovespec(st, -1));
    }
/**
 * prepare to add nmove to the history list, but also edit the history
 * to remove redundant elements, so that indecisiveness by the user doesn't
 * result in a messy game log.
 * This may require that move be merged with an existing history move
 * and discarded.  Return null if nothing should be added to the history
 * One should be very cautious about this, only to remove real pairs that
 * result in a null move.
 * 
 */
    public commonMove EditHistory(commonMove nmove)
    {
        Hexmovespec newmove = (Hexmovespec) nmove;
        Hexmovespec rval = newmove;			// default returned value
        int state = bb.getBoardState();
        int idx = History.size() - 1;
        
       while(idx>=0)
        {	Hexmovespec m = (Hexmovespec) History.elementAt(idx);
        	int start_idx = idx;
            if(m.next!=null) 
            	{ // editing history, don't edit directly
            		switch(newmove.op)
            		{	case MOVE_PICK:
            			case MOVE_DROP:
            				rval = null;
            			default: break;
            		}
            		idx = -1; 
            	}
            else if((m.op==MOVE_RESIGN) && (newmove.op!=MOVE_DONE)&& (newmove.op!=MOVE_RESIGN))
            {	UndoHistoryElement(idx);
            	idx--;
            }
            else {
            switch(newmove.op)
        	{
            case MOVE_SWAP:
            	if(m.op==MOVE_SWAP) 
            		{ UndoHistoryElement(idx);
             		  rval = null;
            		  }
       		  idx=-1;
              break;
        	case MOVE_EDIT:
        	case MOVE_START:
        		if((m.op==MOVE_START)||(m.op==MOVE_EDIT)) 
        			{ UndoHistoryElement(idx);	// start or edit, only need the last one
         			}
        		idx = -1;
        	case MOVE_DONE:
        	default:
        		idx = -1;
        		break;
        	case MOVE_PICK:	
        		if((state!=PUZZLE_STATE) && (m.op==MOVE_DROPB))
        			{ UndoHistoryElement(idx); idx=-1; }
           	case MOVE_DROP:
        		rval = null;		// picks don't appear in the history.  This is probably peculiar to hex
        		idx = -1;
        		break;
        	case MOVE_RESET:
        		rval = null;		// reset is never recorded in the history
        		// fall through
        	case MOVE_RESIGN:		// resign is your whole move
        		switch(m.op)
        		{
        		case MOVE_RESIGN:
        			if(newmove.op==MOVE_RESIGN)
        				{rval = null;		// two resigns toggle
        				UndoHistoryElement(idx);
        				idx = -1;
        				break;
        				}
                default:
             		if((state==RESIGN_STATE)||(state==PUZZLE_STATE)) { idx = -1; break; }

                    UndoHistoryElement(idx);
                	idx--;
                    break;
                case MOVE_DONE: // these stop the scan 
                case MOVE_EDIT:
                case MOVE_START:
                    idx = -1;
                }
        		break;
        	case MOVE_DROPB:
        		if(state!=PUZZLE_STATE)
        		{	if(m.op==MOVE_DROPB) 
        			{ UndoHistoryElement(idx); 
        			}
        		}
        		idx = -1;
        		break;
        	case MOVE_PICKB:
        		switch(m.op)
        		{
        		case MOVE_PICKB:
        		case MOVE_DROPB:
        			if( (newmove.to_col==m.to_col)
            				&& (newmove.to_row==m.to_row))
            			{
            				UndoHistoryElement(idx);
            				if(newmove.op!=m.op) // protect against bounces
            					{ rval = null; }
            			}
        		}
   				idx = -1;
   			 	break;
        		}	// end of switch on new move
            }
            G.Assert(idx!=start_idx,"progress editing history");
            }

        return (rval);
    }
    

/**
 * the preferred mouse gesture style is to let the user "pick up" objects
 * by simply clicking on them, but we also allow him to click and drag. 
 * StartDragging is called when he has done this.
 */
    public void StartDragging(HitPoint hp)
    {
        int mo = bb.movingObjectIndex();
        if (mo==HitNoWhere) // not dragging anything yet, so maybe start
        {
        int hitObject =  hp.hitCode;
 	    switch(hitObject)
	    {
	    default: break;
	    
        case vcrSlider:			// this is a draggable object that the board doesn't know about
             break;
	    case Black_Chip_Pool:
	    	PerformAndTransmit("Pick B");
	    	break;
	    case White_Chip_Pool:
	    	PerformAndTransmit("Pick W");
	    	break;
	    case BoardLocation:
	        hexCell hitCell = (hexCell)hp.hitObject;
	    	PerformAndTransmit("Pickb "+hitCell.col+" "+hitCell.row);
	    	break;
        }

        if (bb.movingObjectIndex() >= 0)
	        {	// if we got something started, inform the mouse handler
	            hp.dragging = true;
	        } 
        }
    }
	private void doDropChip(char col,int row)
	{	int state = bb.getRawBoardState();
		switch(state)
		{
		default: G.Error("Not expecting state "+state);
		case PUZZLE_STATE:
		{
		hexChip mo = bb.pickedObject;
		if(mo==null) { mo=bb.lastPicked; }
		if(mo==null) { mo=bb.getPlayerChip(bb.whoseTurn); }
		PerformAndTransmit("dropb "+mo.colorName+" "+col+" "+row);
		}
		break;
		case CONFIRM_STATE:
		case PLAY_STATE:
		case PLAY_OR_SWAP_STATE:
			hexChip mo=bb.getPlayerChip(bb.whoseTurn);	
			PerformAndTransmit("dropb "+mo.colorName	+ " "+col+" "+row);
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
        int hitCode = hp.hitCode;
        hexCell hitObject = (hexCell)hp.hitObject;
		int state = bb.getRawBoardState();
        switch (hitCode)
        {
        default:
        	if (performStandardButtons(hitCode)) {}
        	else if (performVcrButton(hitCode, hp)) {}	// handle anything in the vcr group
            else
            {
                G.Error("Hit Unknown object " + hitObject);
            }
        	break;
        case BoardLocation:	// we hit an occupied part of the board 
			switch(state)
			{
			default: G.Error("Not expecting drop on filled board in state "+state);
			case CONFIRM_STATE:
			case PLAY_STATE:
			case PLAY_OR_SWAP_STATE:
				if(!bb.isDest(hitObject))
					{
					// note that according to the general theory, this shouldn't
					// ever occur because inappropriate spaces won't be mouse sensitve.
					// this is just defense in depth.
					G.Error("shouldn't hit a chip in state "+state);
					}
				// fall through and pick up the previously dropped piece
			case PUZZLE_STATE:
				PerformAndTransmit("Pickb "+hitObject.col+" "+hitObject.row);
				break;
			}
			break;
			
        case EmptyBoard:
			switch(state)
			{
				default:
					G.Error("Not expecting hit in state "+state);
				case CONFIRM_STATE:
				case PLAY_STATE:
				case PLAY_OR_SWAP_STATE:
				case PUZZLE_STATE:
					doDropChip(hitObject.col,hitObject.row);
					break;
			}
			break;
			
        case Black_Chip_Pool:
        case White_Chip_Pool:
           if(bb.pickedObject!=null) 
			{//if we're dragging a black chip around, drop it.
            	PerformAndTransmit("Drop "+bb.pickedObject.colorName);
			}
           break;
 
        case HitNoWhere:
        	performReset();
            break;
        }

         repaint();
    }


    
    // draw the fixed elements, using the saved background if it is available
    // and believed to be valid.
    public void drawFixedElements(Graphics offGC,boolean complete,HexGameBoard rb)
    {
      	complete |= createAllFixed(fullRect.width, fullRect.height); // create backing bitmaps;
      	Image allFixed = allFixed();
     	if(allFixed==null)
    	{	// no deep background, draw on the immediate background
    		drawFixedElements(offGC, rb,boardRect);	
    	}
    	else
    	{	// draw the fixed background elements, either into the fixed bitmap
        	// or to the immediate bitmap, or to the screen directly.
        	if(complete) 
    	    	 { // redraw the deep background
    	    		Graphics allFixedGC = allFixed.getGraphics();
    	    		G.setClip(allFixedGC,fullRect);
    	            drawFixedElements(allFixedGC, rb,boardRect);
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
    {	HexGameBoard disb = (HexGameBoard)disB();
    	HexGameBoard gb = (disb==null)?bb:disb;
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
      	
      	if(doRotation!=lastRotation)		//if changing the whole orientation of the screen, unusual steps have to be taken
      	{ complete=true;					// for sure, paint everything
      	  lastRotation=doRotation;			// and only do this once
      	  if(doRotation)
      	  {
      	  // 0.95 and 1.0 are more or less magic numbers to match the board to the artwork
          gb.SetDisplayParameters(0.95, 1.0, 0,0,60 , 0, 0,0); // shrink a little and rotate 60 degrees
     	  }
      	  else
      	  {
          // the numbers for the square-on display are slightly ad-hoc, but they look right
          gb.SetDisplayParameters( 0.825, 0.94, 0,0,28.2, 0, 0,0); // shrink a little and rotate 30 degrees
      	  }
      	}
      	gb.SetDisplayRectangle(boardRect);
      	drawFixedElements(offGC,complete,gb);	// draw the board into the deep background
   	
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
 
        if(offScreen!=null)
        	{ // display the completed result if it was drawn into a backing bitmap rather than directly
        	displayClipped(g,fullRect,chatRect,offScreen);
        	}
    }
    
    // return what will be the init type for the game
     public String gameType() 
    	{
    	   // in games which have a randomized start, this method would return
    	   // return(bb.gametype+" "+bb.randomKey); 
    	return(bb.gametype); 
    	}	// this is the subgame "setup" within the master type.
    public String sgfGameType() { return(Hex_SGF); }	// this is the official SGF number assigned to the game

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
    
    
    // interact with the board to initialize a game
    public void performHistoryInitialization(StringTokenizer his)
    {   //the initialization sequence
    	String token = his.nextToken();
    	//
    	// in games which have a randomized start, this is the point where
    	// the randomization is inserted
        // int rk = G.IntToken(his);
    	// bb.doInit(token,rk);
        bb.doInit(token);
    }


    /** handle action events from menus.  Don't do any real work, just note
     * state changes and if necessary set flags for the run loop to pick up.
     * 
     */
    public boolean handleDeferredEvent(Object target,String command)
    {
        boolean handled = super.handleDeferredEvent(target,command);

        if(target==rotationOption)
        {	handled=true;
        	doRotation = rotationOption.getState();
        	resetBounds();
        	repaint();
        }

        return (handled);
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
    public BoardProtocol getBoard()   {    return (bb);   }

    /** this is used by the game controller to supply entertainment strings to the lobby */
    public String gameProgressString()
    {	// this is what the standard method does
    	// return ((reviewer ? s.get("review") : ("" + viewMove)));
    	return(super.gameProgressString());
    }


/** this is used by the scorekeeper to determine who won. Draws are indicated
 * by both players returning false.  Be careful not to let both players return true!
 */
    public boolean WinForPlayer(commonPlayer p)
    { // this is what the standard method does
      // return(getBoard().WinForPlayer(p.index));
      return (super.WinForPlayer(p));
    }

    /** start the robot.  This is used to invoke a robot player.  Mainly this needs 
     * to know the class for the robot and any initialization it requires.
     *  */
    public void startRobot(commonPlayer p,commonPlayer runner)
    {	// this is what the standard method does:
    	// int level = sharedInfo.getInt(sharedInfo.ROBOTLEVEL,0);
    	// RobotProtocol rr = newRobotPlayer();
    	// rr.InitRobot(sharedInfo, getBoard(), null, level);
    	// p.startRobot(rr);
    	super.startRobot(p,runner);
    }
    /** factory method to create a robot */
    public SimpleRobotProtocol newRobotPlayer() 
    {  int level = sharedInfo.getInt(exHashtable.ROBOTLEVEL,0);
       switch(level)
       { default: G.Error("not defined");
       	 case 0:	return(new HexPlay());
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
                bb.doInit(value);
                resetBounds();
             }
            else if (name.equals(comment_property))
            {
                comments += value;
            }
            else if (name.equals(game_property))
            {
                if (!(value.toLowerCase().equals("hex") || value.equals(Hex_SGF)))
                {
                    throw new Error("game type " + value + " is not this game");
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
