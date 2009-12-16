package checkerboard;
import online.game.stackCell;


public class CheckerCell extends stackCell implements CheckerConstants
{
	
	// constructor
	public CheckerCell(char c,int r) 
	{	super(Oct_Geometry,c,r);
		rackLocation = BoardLocation;
	}
	public CheckerCell() { super(); }
	
	// these are just type casts so the routine callers don't have to do it.
	public CheckerChip topChip() { return((CheckerChip)getTopChip()); }
	public CheckerChip chipAtIndex(int n) { return((CheckerChip)getChipAtIndex(n)); }
	public CheckerChip removeChip()
	{	return((CheckerChip)removeTopChip());
	}
	/** define the base level for stacks as 1.  This is because level 0 is the square itself for this
	 * particular representation of the board.
	 */
	public int stackBaseLevel() { return(1); }
	public CheckerCell exitTo(int dir) { return((CheckerCell)exitToward(dir)); }

}
