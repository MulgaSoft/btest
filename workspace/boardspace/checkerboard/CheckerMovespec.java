package checkerboard;

import online.game.*;
import online.common.*;
import java.util.*;

import lib.G;


public class CheckerMovespec extends commonMove implements CheckerConstants
{
    static exHashtable D = new exHashtable(true);
    static final int MOVE_PICK = 204; // pick a chip from a pool
    static final int MOVE_DROP = 205; // drop a chip
    static final int MOVE_PICKB = 206; // pick from the board
    static final int MOVE_DROPB = 207; // drop on the board
    static final int MOVE_RACK_BOARD = 209;	// move from rack to board
    static final int MOVE_BOARD_BOARD = 210;	// move board to board
	

    static
    {
        // these int values must be unique in the dictionary
    	addStandardMoves(D);
        D.putInt("Pick", MOVE_PICK);
        D.putInt("Pickb", MOVE_PICKB);
        D.putInt("Drop", MOVE_DROP);
        D.putInt("Dropb", MOVE_DROPB);
 		D.putInt("Move",MOVE_BOARD_BOARD);
		D.putInt("OnBoard",MOVE_RACK_BOARD);
   }

    CheckerId source; // where from/to
	char from_col; //for from-to moves, the source column
	int from_row; // for from-to moves, the source row
    char to_col; // for from-to moves, the destination column
    int to_row; // for from-to moves, the destination row
    CheckerState state;	// the state of the move before state, for UNDO
    
    public CheckerMovespec() // default constructor
    {
    }
    public CheckerMovespec(int opc, int pl)	// constructor for simple moves
    {
    	player = pl;
    	op = opc;
    }
    /* constructor */
    public CheckerMovespec(String str, int p)
    {
        parse(new StringTokenizer(str), p);
    }

    /* contructor */
    public CheckerMovespec(StringTokenizer ss, int p)
    {
        parse(ss, p);
    }
    
    /**
     * This is used to check for equivalent moves "as specified" not "as executed", so
     * it should only compare those elements that are specified when the move is created. 
     */
    public boolean Same_Move_P(commonMove oth)
    {
    	CheckerMovespec other = (CheckerMovespec) oth;

        return ((op == other.op) 
				&& (source == other.source)
				&& (to_row == other.to_row) 
				&& (to_col == other.to_col)
				&& (from_row == other.from_row)
				&& (from_col == other.from_col)
				&& (player == other.player));
    }

    public void Copy_Slots(CheckerMovespec to)
    {	super.Copy_Slots(to);
        to.player = player;
        to.to_col = to_col;
        to.to_row = to_row;
        to.from_col = from_col;
        to.from_row = from_row;
        to.state = state;
        to.source = source;
    }

    public commonMove Copy(commonMove to)
    {
    	CheckerMovespec yto = (to == null) ? new CheckerMovespec() : (CheckerMovespec) to;

        // we need yto to be a CheckerMovespec at compile time so it will trigger call to the 
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
        
        case MOVE_RACK_BOARD:	// a robot move from the rack to the board
            op = opcode;
            source = CheckerId.get(msg.nextToken());	// white rack or black rack
            from_col = '@';						// always
            from_row = G.IntToken(msg);			// index into the rack
 	        to_col = G.CharToken(msg);			// destination cell col
	        to_row = G.IntToken(msg);  			// destination cell row
	        break;
	        
        case MOVE_BOARD_BOARD:			// robot move from board to board
            op = opcode;
            source = CheckerId.BoardLocation;		
            from_col = G.CharToken(msg);	//from col,row
            from_row = G.IntToken(msg);
 	        to_col = G.CharToken(msg);		//to col row
	        to_row = G.IntToken(msg);
	        break;
	        
        case MOVE_DROPB:
	       op = opcode;
	       source = CheckerId.BoardLocation;
	       to_col = G.CharToken(msg);
	       to_row = G.IntToken(msg);
	       break;

		case MOVE_PICKB:
            op = opcode;
            source = CheckerId.BoardLocation;
            from_col = G.CharToken(msg);
            from_row = G.IntToken(msg);

            break;

        case MOVE_PICK:
            op = opcode;
            source = CheckerId.get(msg.nextToken());
            from_col = '@';
            from_row = G.IntToken(msg);
            break;
            
        case MOVE_DROP:
            op = opcode;
            source =CheckerId.get(msg.nextToken());
            to_col = '@';
            to_row = G.IntToken(msg);
            break;

        case MOVE_START:
            op = opcode;
            player = D.getInt(msg.nextToken());

            break;

        case MOVE_RESET:
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
            return ("@"+from_col + from_row);

		case MOVE_DROPB:
            return (to_col + " " + to_row);

        case MOVE_DROP:
        case MOVE_PICK:
            return (source.shortName);
        case MOVE_RACK_BOARD:
        	return(source.shortName+"@ "+to_col + " " + to_row);
        case MOVE_BOARD_BOARD:
        	return("@"+from_col + from_row+" "+to_col + " " + to_row);
        case MOVE_DONE:
        case MOVE_RESET:
            return ("");

        case MOVE_EDIT:
        case MOVE_START:
        case MOVE_RESIGN:
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
        // adding the move index as a prefix provides numbers
        // for the game record and also helps navigate in joint
        // review mode
        switch (op)
        {
        default:
            G.Error("moveString Not implemented: " + op);

        case MOVE_PICKB:
	        return (ind+D.findUnique(op) + " " + from_col + " " + from_row);

		case MOVE_DROPB:
	        return (ind+D.findUnique(op) + " " + to_col + " " + to_row);

		case MOVE_RACK_BOARD:
			return(ind+D.findUnique(op) + " " +source.shortName+ " "+from_row
					+ " " + to_col + " " + to_row);
		case MOVE_BOARD_BOARD:
			return(ind+D.findUnique(op) + " " + from_col + " " + from_row
					+ " " + to_col + " " + to_row);
        case MOVE_PICK:
            return (ind+D.findUnique(op) + " "+source.shortName+ " "+from_row);

        case MOVE_DROP:
             return (ind+D.findUnique(op) + " "+source.shortName+ " "+to_row);

        case MOVE_START:
            return (ind+"Start P" + player);

        case MOVE_EDIT:
        case MOVE_RESIGN:
        case MOVE_DONE:
        case MOVE_RESET:
            return (ind+D.findUnique(op));
        }
    }

    /* standard java method, so we can read moves easily while debugging */
    //public String toString()
    //{
    //    return ("P" + player + "[" + moveString() + "]");
    //}
}
