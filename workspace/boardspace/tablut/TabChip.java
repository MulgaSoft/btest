package tablut;

import java.awt.Image;
import java.util.Random;

import online.common.exCanvas;
import online.game.*;
public class TabChip extends chip {
	int index = 0;
	int colorType = -1;
	char chipName;
	static final int GoldColor = 200;
	static final int SilverColor = 201;
	
	private TabChip(int idx,String fil,Image im,long rv,double sc[])
	{	index = idx;
		file = fil;
		image = im;
		randomv = rv;
		scale = sc;
	}
	static final char Silver = 'S'; // a white stone
	static final char Gold = 'G'; // a black stone
	static final char GoldF = 'F';
	 
	static public TabChip getChip(int n) 
		{ return(PROTOTYPE_CHIPS[n]); }

    static TabChip GoldFlag=null;
    static TabChip SilverShip=null;
    static TabChip GoldShip=null;
    static TabChip HexTile=null;
    static TabChip HexTile_Gold=null;
    static TabChip HexTile_Gold2=null;
    static TabChip Selection=null;
    
    //ad hoc scale factors to fit the stones to the board
    static final double[][] SCALES=
    {   {0.515,0.57,2.01}	// silver
    	,{0.508,0.564,1.9}	// gold
    	,{0.54,0.65,2.46}	// gold flagship
    	,{0.50,0.55,2.44}	// main tile
    	,{0.50,0.52,1.2}	// selection
    	,{0.50,0.55,2.44}	// hex gold tile
    	,{0.50,0.55,2.44}	// hex gold2 tile
    };	
    static final String[] ImageNames = 
    {   "silver" 
        ,"gold"
        ,"gold-flagship"
        ,"tile"
        ,"selection"
        ,"tile-gold"
        ,"tile-gold2"
    };
    static TabChip[]PROTOTYPE_CHIPS=null;
	// call from the viewer's preloadImages
	public static void preloadImages(exCanvas forcan,String ImageDir)
	{ if(PROTOTYPE_CHIPS==null)
	  {
	   int nColors = ImageNames.length;
       Image IM[]=forcan.load_masked_images(ImageDir,ImageNames);
       TabChip CC[] = new TabChip[nColors];
       Random rv = new Random(4354865);		// an arbitrary number, just change it
       for(int i=0;i<nColors;i++) 
       	{
       	CC[i]=new TabChip(i,ImageNames[i],IM[i],rv.nextLong(),SCALES[i]); 
       	}
       PROTOTYPE_CHIPS = CC;
       SilverShip = CC[0];
       SilverShip.colorType = SilverColor;
       SilverShip.chipName = Silver;
       GoldShip = CC[1];
       GoldFlag = CC[2];
       GoldShip.colorType = GoldColor;
       GoldShip.chipName = Gold;
       GoldFlag.colorType = GoldColor;
       GoldFlag.chipName = GoldF;
       
       HexTile = CC[3];
       Selection = CC[4];
       HexTile_Gold = CC[5];
       HexTile_Gold2 = CC[6];
       check_digests(CC);
	 }
	}
    
}
