package checkerboard;
import java.util.Random;

import online.game.stackCell;


public class CheckerCell extends stackCell<CheckerCell,CheckerChip> implements CheckerConstants
{
	public CheckerChip[] newComponentArray(int n) { return(new CheckerChip[n]); }
	// constructor
	public CheckerCell(char c,int r) 
	{	super(Geometry.Oct,c,r);
		rackLocation = BoardLocation;
	}
	public CheckerCell(Random r) { super(r); }
	
	/** define the base level for stacks as 1.  This is because level 0 is the square itself for this
	 * particular representation of the board.
	 */
	public int stackBaseLevel() { return(1); }

}
