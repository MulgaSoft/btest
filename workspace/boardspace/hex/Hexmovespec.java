package hex;

import online.common.*;


import java.util.*;

import lib.G;

import online.game.*;

public class Hexmovespec extends commonMove implements HexConstants
{	// this is the dictionary of move names
    static exHashtable D = new exHashtable(true);
    static final int MOVE_PICK = 204; // pick a chip from a pool
    static final int MOVE_DROP = 205; // drop a chip
    static final int MOVE_PICKB = 206; // pick from the board
    static final int MOVE_DROPB = 207; // drop on the board
 
    static
    {	// load the dictionary
        // these int values must be unique in the dictionary
    	addStandardMoves(D);	// this adds "start" "done" "edit" "reset" and so on.
        D.putInt("Pick", MOVE_PICK);
        D.putInt("Pickb", MOVE_PICKB);
        D.putInt("Drop", MOVE_DROP);
        D.putInt("Dropb", MOVE_DROPB);
  }
    //
    // adding these makes the move specs use Same_Move_P instead of == in hash tables
    //needed when doing chi square testing of random move generation, but possibly
    //hazardous to keep in generally.
    //public int hashCode()
    //{
    //	return(to_row<<12+to_col<<18+player<<24+op<<25);
    //}
    //public boolean equals(Object a)
    //{
    //	return( (a instanceof commonMove) && Same_Move_P((commonMove)a)); 
    //}
    //
    // variables to identify the move
    HexId source; // where from/to
    char to_col; // for from-to moves, the destination column
    int to_row; // for from-to moves, the destination row
    //
    // variables for use by the robot
    HexState state;	// the state of the move before state, for robot UNDO
    
    public Hexmovespec()
    {
    } // default constructor

    /* constructor */
    public Hexmovespec(String str, int p)
    {
        parse(new StringTokenizer(str), p);
    }
    /** constructor for robot moves.  Having this "binary" constor is dramatically faster
     * than the standard constructor which parses strings
     */
    public Hexmovespec(int opc,char col,int row,HexId what,int who)
    {
    	op = opc;
    	source = what;
    	to_col = col;
    	to_row = row;
    	player = who;
    }
    /* contructor */
    public Hexmovespec(StringTokenizer ss, int p)
    {
        parse(ss, p);
    }

    /**
     * This is used to check for equivalent moves "as specified" not "as executed", so
     * it should only compare those elements that are specified when the move is created. 
     */
    public boolean Same_Move_P(commonMove oth)
    {
        Hexmovespec other = (Hexmovespec) oth;

        return ((op == other.op) 
				&& (source == other.source)
				&& (to_row == other.to_row) 
				&& (to_col == other.to_col)
				&& (player == other.player));
    }

    public void Copy_Slots(Hexmovespec to)
    {	super.Copy_Slots(to);
        to.to_col = to_col;
        to.to_row = to_row;
        to.state = state;
        to.source = source;
    }

    public commonMove Copy(commonMove to)
    {
        Hexmovespec yto = (to == null) ? new Hexmovespec() : (Hexmovespec) to;

        // we need yto to be a Hexmovespec at compile time so it will trigger call to the 
        // local version of Copy_Slots
        Copy_Slots(yto);

        return (yto);
    }

    /* parse a string into the state of this move.  Remember that we're just parsing, we can't
     * refer to the state of the board or the game.  This parser follows the recommended practice
     * of keeping it very simple.  A move spec is just a sequence of tokens parsed by calling
     * nextToken
     * @param msg a string tokenizer containing the move spec
     * @param the player index for whom the move will be.
     * */
    private void parse(StringTokenizer msg, int p)
    {
        String cmd = msg.nextToken();
        player = p;

        if (Character.isDigit(cmd.charAt(0)))
        { // if the move starts with a digit, assume it is a sequence number
            index = G.IntToken(cmd);
            cmd = msg.nextToken();
        }

        int opcode = D.getInt(cmd, MOVE_UNKNOWN);
        op = opcode;
        switch (opcode)
        {
        default:
            G.Error("Cant parse " + cmd);
            break;
        case MOVE_DROPB:
				source = HexId.get(msg.nextToken());	// B or W
	            to_col = G.CharToken(msg);
	            to_row = G.IntToken(msg);

	            break;

		case MOVE_PICKB:
            source = HexId.BoardLocation;
            to_col = G.CharToken(msg);
            to_row = G.IntToken(msg);

            break;

        case MOVE_DROP:
        case MOVE_PICK:
            source = HexId.get(msg.nextToken());

            break;

        case MOVE_START:
            player = D.getInt(msg.nextToken());

            break;

        case MOVE_RESET:
        case MOVE_SWAP:
        case MOVE_EDIT:
        case MOVE_DONE:
        case MOVE_RESIGN:

            break;
        }
    }

    /** construct an abbreviated move string, mainly for use in the game log.  These
     * don't have to be parseable, they're intended only to help humans understand
     * the game record.
     * */
    public String shortMoveString()
    {
        switch (op)
        {
        case MOVE_PICKB:
            return (D.findUnique(op) +" " + to_col + " " + to_row);

		case MOVE_DROPB:
            return (to_col + " " + to_row);

        case MOVE_DROP:
        case MOVE_PICK:
            return (D.findUnique(op) + " "+source.shortName);

        case MOVE_DONE:
        case MOVE_RESET:
            return ("");

        case MOVE_EDIT:
        case MOVE_START:
        case MOVE_RESIGN:
        case MOVE_SWAP:
            return (D.findUnique(op));

        default:
            G.Error("shortMoveString Not implemented: " + op);

            return ("bad");
        }
    }

    /** construct a move string for this move.  These are the inverse of what are accepted
    by the constructors, and only secondarily human readable */
    public String moveString()
    {
        String ind = "";

        if (index >= 0)
        {
            ind += (index + " ");
        }
        // adding the move index as a prefix provides numnbers
        // for the game record and also helps navigate in joint
        // review mode
        switch (op)
        {
        default:
            G.Error("moveString Not implemented: " + op);
            return(null);	// not used
        case MOVE_PICKB:
	        return (ind+D.findUnique(op) +" " + to_col + " " + to_row);

		case MOVE_DROPB:
	        return (ind+D.findUnique(op) + " "+source.shortName+" " + to_col + " " + to_row);

        case MOVE_DROP:
        case MOVE_PICK:
            return (ind+D.findUnique(op) + " "+source.shortName);

        case MOVE_START:
            return (ind+"Start P" + player);

        case MOVE_EDIT:
        case MOVE_RESIGN:
        case MOVE_DONE:
        case MOVE_RESET:
        case MOVE_SWAP:
            return (ind+D.findUnique(op));
        }
    }
    /**
     *  longMoveString is used for sgf format records and can contain other information
     * intended to be ignored in the normal course of play, for example human-readable
     * information
     */
    public String longMoveString()
    {	String str = moveString();
    	return(str);
    }
    /** standard java method, so we can read moves easily while debugging */
    public String toString()
    {	return super.toString();
        //return ("P" + player + "[" + moveString() + "]");
    }
}
