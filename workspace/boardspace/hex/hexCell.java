package hex;

import online.game.*;
/**
 * specialized cell used for the game hex, not for all games using a hex board.
 * <p>
 * the game hex needs only a single object on each cell, or empty.
 *  @see {@link chipCell} 
 *  @see {@link stackCell}
 * 
 * @author ddyer
 *
 */
public class hexCell extends chipCell<hexCell,hexChip> implements HexConstants
{	
	hexblob blob;			// the blob which contains this cell
	hexCell nextInBlob;		// a link to the next cell in this blob
	int sweep_counter;		// the sweep counter for which blob is accurate
	int borders = -1;		// bitmask of possible borders

	public hexCell() { super(); }		// construct a cell not on the board
	public hexCell(char c,int r) 		// construct a cell on the board
	{	super(cell.Geometry.Hex,c,r);
	};
	
	/** sameCell is called at various times as a consistency check
	 * 
	 * @param other
	 * @return true if this cell is in the same location as other (but presumably on a different board)
	 */
	public boolean sameCell(hexCell other)
	{	return(super.sameCell(other)
				// check the values of any variables that define "sameness"
				// && (moveClaimed==other.moveClaimed)
			); 
	}
	/** copyFrom is called when cloning boards
	 * 
	 */
	public void copyFrom(hexCell ot)
	{	//hexCell other = (hexCell)ot;
		// copy any variables that need copying
		super.copyFrom(ot);
	}
	
	// constructor a cell not on the board, with a chip.  Used to construct the pool chips
	public hexCell(hexChip cont)
	{	super();
		chip = cont;
		onBoard=false;
	}
}