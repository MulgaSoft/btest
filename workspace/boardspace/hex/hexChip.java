package hex;

import java.awt.Image;
import java.util.Random;
import online.common.exCanvas;
import online.game.*;

/**
 * this is a specialization of {@link chip} to represent the stones used by hex
 * 
 * @author ddyer
 *
 */
public class hexChip extends chip
{
	private int index = 0;
	public char colorName;
	// constructor
	private hexChip(int i,Image im,String na,double[]sc,char con,long ran)
	{	index = i;
		scale=sc;
		image=im;
		file = na;
		colorName = con;
		randomv = ran;
	}
	public int chipNumber() { return(index); }
	
    static final double[][] SCALES=
    {   {0.691,0.450,2.3},	// white stone
    	{0.66,0.458,2.099}		// black stone
    };
    //
    // basic image strategy is to use jpg format because it is compact.
    // .. but since jpg doesn't support transparency, we have to create
    // composite images wiht transparency from two matching images.
    // the masks are used to give images soft edges and shadows
    //
    static final String[] ImageNames = 
        {   "white-stone", 
            "black-stone"
        };
	// call from the viewer's preloadImages
    static hexChip CANONICAL_PIECE[] = null;
    static hexChip Black = null;
    static hexChip White = null;
    // indexes into the balls array, usually called the rack
    static final char[] chipColor = { 'W', 'B' };
    static final hexChip getChip(int n) { return(CANONICAL_PIECE[n]); }
    /**
     * this is a fairly standard preloadImages method, called from the
     * game initialization.  It loads the images (all two of them) into
     * a static array of hexChip which are used by all instances of the
     * game.
     * @param forcan the canvas for which we are loading the images.
     * @param Dir the directory to find the image files.
     */
	public static void preloadImages(exCanvas forcan,String Dir)
	{	if(CANONICAL_PIECE==null)
		{
		Random rv = new Random(5312324);
		int nColors = ImageNames.length;
		// load the main images, their masks, and composite the mains with the masks
		// to make transparent images that are actually used.
        Image IM[]=forcan.load_masked_images(Dir,ImageNames);
        hexChip CC[] = new hexChip[nColors];
        for(int i=0;i<nColors;i++) 
        	{CC[i]=new hexChip(i,IM[i],ImageNames[i],SCALES[i],chipColor[i],rv.nextLong()); 
        	}
        CANONICAL_PIECE = CC;
        Black = CC[1];
        White = CC[0];
        check_digests(CC);	// verify that the chips have different digests
		}
	}   
}
