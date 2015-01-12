package tablut;

import java.util.Random;

import online.game.*;

// specialized cell used for the game tablut hnetafl & breakthru
//
public class TabCell extends chipCell<TabCell,TabChip> implements TabConstants
{	
	int sweep_counter;		// the sweep counter for which blob is accurate
	int sweep_score;
	public boolean centerArea = false;
	public boolean flagArea = false;
	public boolean winForGold = false;
	// constructor for cells in the board
	public TabCell(char c,int r) 
	{	super(cell.Geometry.Oct,c,r);
		rackLocation=TabId.BoardLocation;
	};
	TabId rackLocation() { return((TabId)rackLocation); }
	// constructor for dingletons with contents
	public TabCell(Random r,TabChip chipv,TabId loc) { super(r,loc); chip=chipv;  }

}
