package hex;

import java.awt.Image;
import java.util.Random;
import online.common.exCanvas;
import online.game.*;
public class hexChip extends chip
{
	private int index = 0;
	public char colorName;
	// constructor
	private hexChip(int i,Image im,String na,double[]sc,char con,int ran)
	{	index = i;
		scale=sc;
		image=im;
		file = na;
		colorName = con;
		randomv = ran;
	}
	public int chipNumber() { return(index); }
	
    static final double[][] SCALES=
    {   {0.65,0.48,2.66},	// white stone
    	{0.64,0.48,2.48}		// black stone
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
	public static void preloadImages(exCanvas forcan,String Dir)
	{	if(CANONICAL_PIECE==null)
		{
		Random rv = new Random(5312324);
		int nColors = ImageNames.length;
        Image IM[]=forcan.load_masked_images(Dir,ImageNames);
        hexChip CC[] = new hexChip[nColors];
        for(int i=0;i<nColors;i++) 
        	{CC[i]=new hexChip(i,IM[i],ImageNames[i],SCALES[i],chipColor[i],rv.nextInt()); 
        	}
        CANONICAL_PIECE = CC;
        Black = CC[1];
        White = CC[0];
        check_digests(CC);
		}
	}   
}
