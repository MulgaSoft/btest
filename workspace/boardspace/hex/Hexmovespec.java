package hex;

import online.common.*;

import java.util.*;
import online.game.*;

public class Hexmovespec extends commonMove implements HexConstants
{
    static exHashtable D = new exHashtable(true);

    static
    {
        // these int values must be unique in the dictionary
    	addStandardMoves(D);
        D.putInt("Pick", MOVE_PICK);
        D.putInt("Pickb", MOVE_PICKB);
        D.putInt("Drop", MOVE_DROP);
        D.putInt("Dropb", MOVE_DROPB);
        D.putInt("W", White_Chip_Pool);
        D.putInt("B", Black_Chip_Pool);
		D.putInt("Swap",MOVE_SWAP);
  }

    int source; // where from/to
	int object;	// object being picked/dropped
    char to_col; // for from-to moves, the destination column
    int to_row; // for from-to moves, the destination row
    int state;	// the state of the move before state, for UNDO
    
    public Hexmovespec()
    {
    } // default constructor

    /* constructor */
    public Hexmovespec(String str, int p)
    {
        parse(new StringTokenizer(str), p);
    }

    /* contructor */
    public Hexmovespec(StringTokenizer ss, int p)
    {
        parse(ss, p);
    }

    public boolean Same_Move_P(commonMove oth)
    {
        Hexmovespec other = (Hexmovespec) oth;

        return ((op == other.op) 
				&& (source == other.source)
				&& (object == other.object)
				&& (state == other.state)
				&& (to_row == other.to_row) 
				&& (to_col == other.to_col)
				&& (player == other.player));
    }

    public void Copy_Slots(Hexmovespec to)
    {	super.Copy_Slots(to);
 		to.object = object;
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
     * refer to the state of the board or the game.
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

        switch (opcode)
        {
        default:
            G.Error("Cant parse " + cmd);

        case MOVE_DROPB:
	           op = opcode;
	            source = EmptyBoard;
				object = D.getInt(msg.nextToken());	// B or W
	            to_col = G.CharToken(msg);
	            to_row = G.IntToken(msg);

	            break;

		case MOVE_PICKB:
            op = opcode;
            source = BoardLocation;
            to_col = G.CharToken(msg);
            to_row = G.IntToken(msg);

            break;

        case MOVE_DROP:
        case MOVE_PICK:
            op = opcode;
            source = D.getInt(msg.nextToken());

            break;

        case MOVE_START:
            op = opcode;
            player = D.getInt(msg.nextToken());

            break;

        case MOVE_RESET:
        case MOVE_SWAP:
        case MOVE_EDIT:
        case MOVE_DONE:
        case MOVE_RESIGN:
            op = opcode; // simple commands

            break;
        }
    }

    /* construct a move string for this move.  These are the inverse of what are accepted
    by the constructors, and are also human readable */
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
            return (D.findUnique(op) + " "+D.findUnique(source));

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

    /* construct a move string for this move.  These are the inverse of what are accepted
    by the constructors, and are also human readable */
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

        case MOVE_PICKB:
	        return (ind+D.findUnique(op) +" " + to_col + " " + to_row);

		case MOVE_DROPB:
	        return (ind+D.findUnique(op) + " "+D.findUnique(object)+" " + to_col + " " + to_row);

        case MOVE_DROP:
        case MOVE_PICK:
            return (ind+D.findUnique(op) + " "+D.findUnique(source));

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
    /* longMoveString is used for sgf format records and can contain other information
     * intended to be ignored in the normal course of play, for example human-readable
     * information
     */
    public String longMoveString()
    {	String str = moveString();
    	return(str);
    }
    /* standard java method, so we can read moves easily while debugging */
    public String toString()
    {
        return ("P" + player + "[" + moveString() + "]");
    }
}
