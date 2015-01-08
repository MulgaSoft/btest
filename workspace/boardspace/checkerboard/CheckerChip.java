package checkerboard;

import java.util.Random;

import lib.OStack;
import online.common.exCanvas;
import online.game.chip;
/*
 * generic "playing piece class, provides canonical playing pieces, 
 * image artwork, scales, and digests.  For our purposes, the squares
 * on the board are pieces too.
 * 
 */
public class CheckerChip extends chip implements CheckerConstants
{	
	private static Random r = new Random(343535);	// this gives each chip a unique random value for Digest()
	private static OStack<CheckerChip>allChips = new OStack<CheckerChip>(CheckerChip.class);
	private static boolean imagesLoaded = false;

	private int chipIndex;
	public int chipNumber() { return(chipIndex); }
	public static CheckerChip getChipNumber(int id)
	{	return(allChips.elementAt(id));
	}

	public CheckerId id = null;

	// constructor for chips not expected to be part of the UI
	private CheckerChip(String na,double scl[])
	{	file = na;
		chipIndex=allChips.size();
		randomv = r.nextLong();
		scale = scl;
		allChips.push(this);
	}
	// constructor for chips expected to be part of the UI
	private CheckerChip(String na,double scl[],CheckerId uid)
	{	this(na,scl);
		id = uid;
	}
	
	public String toString()
	{	return("<"+ id+" #"+chipIndex+">");
	}
	public String contentsString() 
	{ return(id.shortName); 
	}
	
	static private double SCALES[][] =
		    {	{0.5,0.5,0.98},		// light square
		    	{0.5,0.5,0.98},		// dark square
		    	{0.527,0.430,1.38},	// white chip
		    	{0.500,0.402,1.38},	// dark chip
		    };
	
	static private CheckerChip tiles[] =
		{
		new CheckerChip("light-tile",SCALES[0]),
    	new CheckerChip("dark-tile",SCALES[1]),
		};
	
	static private CheckerChip chips[] = 
		{
		new CheckerChip("white-chip-np",SCALES[2],CheckerId.White_Chip_Pool),
    	new CheckerChip("black-chip-np",SCALES[3],CheckerId.Black_Chip_Pool),
		};

 
	public static CheckerChip getTile(int color)
	{	return(tiles[color]);
	}
	public static CheckerChip getChip(int color)
	{	return(chips[color]);
	}

	
	public static CheckerChip backgroundTile = new CheckerChip( "background-tile-nomask",null);
	public static CheckerChip backgroundReviewTile = new CheckerChip( "background-review-tile-nomask",null);
	public static CheckerChip liftIcon = new CheckerChip( "lift-icon-nomask",null);
	

 
	// call from the viewer's preloadImages
	public static void preloadImages(exCanvas forcan,String ImageDir)
	{	if(!imagesLoaded)
		{
		imagesLoaded = forcan.load_masked_images(ImageDir,allChips);
		}
	}


}
