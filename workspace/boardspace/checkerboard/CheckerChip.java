package checkerboard;

import java.awt.Image;
import java.util.Random;

import online.common.exCanvas;
import online.game.chip;
/*
 * generic "playing piece class, provides canonical playing pieces, 
 * image artwork, scales, and digests.  For our purposes, the squares
 * on the board are pieces too, so there are four of them.
 * 
 */
public class CheckerChip extends chip
{	
	private int colorIndex;
	private String name = "";
	
	public int chipNumber() { return(colorIndex); }

	static final int FIRST_TILE_INDEX = 0;
    static final int N_STANDARD_TILES = 2;
    static final int N_STANDARD_CHIPS = 1;
    static final int BLANK_CHIP_INDEX = 0;
    static final int FIRST_CHIP_INDEX = N_STANDARD_TILES;
    static final int BLACK_CHIP_INDEX = FIRST_CHIP_INDEX+1;
    static final int WHITE_CHIP_INDEX = FIRST_CHIP_INDEX;

	private CheckerChip(String na,int pla,Image im,int rv,double scl[])
	{	name = na;
		colorIndex=pla;
		image = im;
		randomv = rv;
		scale = scl;
	}
	public String toString()
	{	return("<"+ name+" #"+colorIndex+">");
	}
	public String contentsString() 
	{ return(name); 
	}
		
	// note, do not make these private, as some optimization failure
	// tries to access them from outside.
    static private CheckerChip CANONICAL_PIECE[] = null;	// created by preload_images
    static private double SCALES[][] =
    {	{0.5,0.5,0.98},		// light square
    	{0.5,0.5,0.98},		// dark square
    	{0.527,0.430,1.38},	// white chip
    	{0.500,0.402,1.38},	// dark chip
    };
     
 
	public static CheckerChip getTile(int color)
	{	return(CANONICAL_PIECE[FIRST_TILE_INDEX+color]);
	}
	public static CheckerChip getChip(int color)
	{	return(CANONICAL_PIECE[FIRST_CHIP_INDEX+color]);
	}
	public static CheckerChip getChip(int pl,int color)
	{
		return(CANONICAL_PIECE[FIRST_CHIP_INDEX+(pl*N_STANDARD_CHIPS)+color]);
	}
  /* pre load images and create the canonical pieces
   * 
   */
 
   static final String[] ImageNames = 
       {"light-tile","dark-tile",  "white-chip-np","black-chip-np"};
 
	// call from the viewer's preloadImages
	public static void preloadImages(exCanvas forcan,String ImageDir)
	{	if(CANONICAL_PIECE==null)
		{
		int nColors = ImageNames.length;
        Image IM[]=forcan.load_masked_images(ImageDir,ImageNames);
        CheckerChip CC[] = new CheckerChip[nColors];
        Random rv = new Random(343535);		// an arbitrary number, just change it
        for(int i=0;i<nColors;i++) 
        	{
        	CC[i]=new CheckerChip(ImageNames[i],i-FIRST_CHIP_INDEX,IM[i],rv.nextInt(),SCALES[i]); 
        	}
        CANONICAL_PIECE = CC;
        check_digests(CC);
		}
	}


}
