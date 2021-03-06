package checkerboard;

import java.util.Random;
import online.game.stackCell;


public class CheckerCell extends stackCell<CheckerCell,CheckerChip> implements CheckerConstants
{
	public CheckerChip[] newComponentArray(int n) { return(new CheckerChip[n]); }
	// constructor
	public CheckerCell(char c,int r) 
	{	super(Geometry.Oct,c,r);
		rackLocation = CheckerId.BoardLocation;
	}
	/** upcast the cell id to our local type */
	public CheckerId rackLocation() { return((CheckerId)rackLocation); }
	
	public CheckerCell(Random r) { super(r); }
	
	/** define the base level for stacks as 1.  This is because level 0 is the square itself for this
	 * particular representation of the board.
	 */
	public int stackBaseLevel() { return(1); }
	/**
	 * wrap this method if the cell holds any additional state important to the game.
	 * This method is called, without a random sequence, to digest the cell in it's usual role.
	 * this method can be defined as G.Error("don't call") if you don't use it or don't
	 * want to trouble to implement it separately.
	 */
	public long Digest() { return(super.Digest()); }
	/**
	 * wrap this method if the cell holds any additional state important to the game.
	 * This method is called, with a random sequence, to digest the cell in unusual
	 * roles, or when the diest of contents is complex.
	 */
	public long Digest(Random r) { return(super.Digest(r)); }
	
	/** copyFrom is called when cloning boards
	 * 
	 */
	public void copyFrom(CheckerCell ot)
	{	//CheckerCell other = (CheckerCell)ot;
		// copy any variables that need copying
		super.copyFrom(ot);
	}
	/**
	 * reset back to the same state as when newly created.  This is used
	 * when reinitializing a board.
	 */
	public void reInit()
	{	super.reInit();
	}

}
