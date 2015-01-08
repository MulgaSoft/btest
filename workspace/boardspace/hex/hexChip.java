package hex;

import java.util.Random;

import lib.OStack;
import online.common.exCanvas;
import online.game.*;

/**
 * this is a specialization of {@link chip} to represent the stones used by hex;
 * and also other tiles, borders and other images that are used to draw the board.
 * 
 * @author ddyer
 *
 */
public class hexChip extends chip implements HexConstants
{
	private int index = 0;
	
	private static Random r = new Random(5312324);	// this gives each chip a unique random value for Digest()
	private static OStack<hexChip>allChips = new OStack<hexChip>(hexChip.class);
	private static boolean imagesLoaded = false;
	public HexId id;
	
	// constructor for the chips on the board, which are the only things that are digestable.
	private hexChip(String na,double[]sc,HexId con)
	{	index = allChips.size();
		scale=sc;
		file = na;
		id = con;
		randomv = r.nextLong();
		allChips.push(this);
	}
	
	// constructor for all the other random artwork.
	private hexChip(String na,double[]sc)
	{	index = allChips.size();
		scale=sc;
		file = na;
		allChips.push(this);
	}
	
	public int chipNumber() { return(index); }
	
	private static double whiteScale[]={0.691,0.450,2.3};
	public static hexChip White = new hexChip("white-stone",whiteScale,HexId.White_Chip_Pool);
	
	private static double blackScale[] = {0.66,0.458,2.099};
	public static hexChip Black = new hexChip("black-stone",blackScale,HexId.Black_Chip_Pool);

	public static hexChip CANONICAL_PIECE[] = { White,Black };

    // indexes into the balls array, usually called the rack
    static final hexChip getChip(int n) { return(CANONICAL_PIECE[n]); }
    
    

    /* plain images with no mask can be noted by naming them -nomask */
    static public hexChip backgroundTile = new hexChip("background-tile-nomask",null);
    static public hexChip backgroundReviewTile = new hexChip("background-review-tile-nomask",null);
   
    static private double hexScale[] = {0.50,0.50,1.76};
    static private double hexScaleNR[] = {0.50,0.50,1.6};
    static public hexChip hexTile = new hexChip("hextile",hexScale);
    static public hexChip hexTileNR = new hexChip("hextile-nr",hexScaleNR);
   
    static public int BorderPairIndex[]={0,1,1,3,3,2};	// this table matches the artwork below to the rotations defined by exitToward(x)
    
    /* fancy borders for the board, which are overlaid on the tiles of the border cells */
    static private double borderScale[] = {0.50,0.50,1.76};
    static public hexChip border[] = 
    	{
    	new hexChip("border-sw",borderScale),
    	new hexChip("border-nw",borderScale),
    	new hexChip("border-se",borderScale),
    	new hexChip("border-ne",borderScale)
   	};
    static public hexChip borderNR[] = 
    	{
    	new hexChip("border-w",borderScale),
    	new hexChip("border-n",borderScale),
    	new hexChip("border-s",borderScale),
    	new hexChip("border-e",borderScale)
   	};
     
   
    /**
     * this is a fairly standard preloadImages method, called from the
     * game initialization.  It loads the images into the stack of
     * chips we've built
     * @param forcan the canvas for which we are loading the images.
     * @param Dir the directory to find the image files.
     */
	public static void preloadImages(exCanvas forcan,String Dir)
	{	if(!imagesLoaded)
		{	
		imagesLoaded = forcan.load_masked_images(Dir,allChips);
		}
	}   
}
