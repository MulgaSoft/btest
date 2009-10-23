package checkerboard;
import online.game.stackCell;


public class CheckerCell extends stackCell
{
	
	// constructor
	public CheckerCell(char c,int r) 
	{	super(Oct_Geometry,c,r);
	}
	public CheckerCell() { super(); }
	
	// these are just type casts so the routine callers don't have to do it.
	public CheckerChip topChip() { return((CheckerChip)getTopChip()); }
	public CheckerChip chipAtIndex(int n) { return((CheckerChip)getChipAtIndex(n)); }
	public CheckerChip removeChip()
	{	return((CheckerChip)removeTopChip());
	}
	

}
