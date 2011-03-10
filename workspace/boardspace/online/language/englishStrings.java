package online.language;

import online.common.internationalStrings;


//
// available font families: 
//  serif (timesroman)
//  sansserif (helvetica)
//  monospaced (courier)
//
public class englishStrings extends internationalStrings
{
    static
    {
        put("fontfamily", "sansserif");

        // lobby messages
        put("launchfailed","The other player didn't start properly");
        put("#1 invites #2 to join room #3", "#1 invites #2 to join room #3");
        put("#1 invites #2 to play #3 in room #4","#1 invites #2 to play #3 in room #4");
        put("#1 has been muted, by a consensus of the players",
            "#1 has been muted, by a consensus of the players");
        put("welcome", "Welcome to the BoardSpace Lobby, version #1"); //version number follows
        put("website", "#1 BoardSpace"); //pretty name for the web site.. #1 is something like "Game Room" or "Lobby"
        put("enterMessage", "#1 has entered the lobby"); //somebody entered
        put("in #1", "in #1"); //followed by a session number
        put("Move ##1", "Move ##1"); //move number for games in lobby
        put("News", "News"); //header for news lines
        put("Lobby", "Lobby"); //the name of the lobby
        put("Error", "Error"); //the name for errors
        put("launchChat", "Launching chat room in Room #1"); //launching a chat room
        put("launchspectator", "Launching spectator in Room #1"); //session id follows
        put("launchGame", "Launching game in Room #1"); //launching a game
        put("launchReview", "Launching review room in Room #1");
        put("Session #1: state unknown.", "Room #1: state unknown.");
        put("Session #1 is idle.  ", "Room #1 is idle.  ");
        put("Session #1 is being launched.  ", "Room #1 is being launched.  ");
        put("Session #1 is active.  ", "Room #1 is active.  ");
        put("Session #1 is private. ", "Room #1 is private. ");
        put("Session #1 is displaying the map of players real locations",
            "Room #1 is displaying the map of players real locations");
        put("Invite", "Invite #1 into room #2");
        put("Accept chat from #1", "Accept chat from #1");
        put("Ignore chat from #1", "Ignore chat from #1");
        put("Play #1", "Play #1"); //followed by robot name
        put("Rejoin #1", "Rejoin #1"); //rejoin a game you were playing
        put("Rejoin", "Rejoin"); //rejoin a game you were playing
        put("noserver", "Lobby is not connected to the server."); //status message
        put("stillwaiting", "still launching... please wait."); //repeated hit on launch
        put("noresponse1", "The server is not responding.");
        put("noresponse2",
            "It may be down, or a firewall may be preventing the connection.");
        put("nolobby", "Sorry, lobby has refused entry.  It may be full.");
        put("timeoutWarning", "  Lobby will shutdown in 1 minute (idle)."); //warning	
        put("Lobby shutdown...", "Lobby shutdown..."); //shutting down
        put("Active in", "Active in"); //header for users column
        put("Waiting in", "Waiting in"); //header for users column
        put("your name", "your name"); //your name in users, when not known
        put("(unknown)", "(unknown)"); //name of another player not yet known
        put("announceOption", "Announce New Players"); //the "announce new players" menu option
        put("Join", "Join"); //label for the join token
        put("Invite players to this room", "Invite players to this room");

        /* game state messages for games scroll area */
        put("State unknown.", "State unknown."); //when game state is not yyet known
        put("Launching...", "Launching..."); //game being launched
        put("Spectate", "Spectate"); //spectate in a game (verb)
        put("Spectator", "Spectator"); //spectator (singular noun)
        put("Idle.", "Idle."); //game that is not in use
        put("Players", "Players"); //generic players in a game (plural noun)
        put("player", "player"); //generic players in a game (singular noun)
        put("Spectators", "Spectators"); //generic spectators in a game (plural noun)
        put("(unoccupied)", "(unoccupied)"); //empty/available slot in a game
        put("Start", "Start"); //start a game button

        /* game description messages */
        put("and", " and "); //"and" for a, b and c, including spaces
        put("No players.", "No players."); //there are no players in this game
        put("Player is ", "Player is "); //the single player name follows
        put("Players are: ", "Players are: "); //the list of player names follows
        put("No spectators.", "No spectators."); //similarly for spectators
        put("Spectator is: ", "Spectator is: "); //
        put("Spectators are: ", "Spectators are: "); //
        put("loaderr", "Error while trying to load: "); //can find classes we need
        put("Connection error", "Connection error"); //problem with the connection
        put("Reconnecting", " ...Reconnecting..."); //attempting to reconnect
        put("connecting", "Connecting to "); //connecting to web site	
        put("reconok", "..reconnection succeeded"); //ok, we did it
        put("nolaunch", "The server rejected a launch request, message is ");
        put("Time is ", "Time is "); //followed by hh:mm
        put("savedmsg", "Game saved as #1"); //announce saved game
        put("guest", "guest"); //the name for guest players

        //lobby messages
        put("Please Join Me", "Please Join Me"); // goes with "join only if invited" and "tournament mode"
        put("Join only if Invited", "Join only if invited"); //seen in lobby game rooms
        put("Tournament Game", "Tournament Game"); //seen in lobby game rooms
        put("No challenges from #1", "No invitations from #1"); //seen in lobby player list
        put("Allow challenges from #1", "Allow invitations from #1"); //seen in lobby player list
        put("Unranked Room", "Unranked Room"); // room for unranked games.
        put("Games do not affect your Ranking",
            "Games do not affect your Ranking"); //subheader for the room
        put("No Challenges", "No Invitations to Play");
        put("automute", "#1 has been automatically muted");
        put("No Chat", "No Chat");
        put("Map of Player Locations", "Map Room");
        put("Game Room", "Game Room");
        put("Review Saved Games", "Review Room");	// appears as the frame name in review rooms
        put("Anyone can play in this room", "Anyone can play in this room");
        put("Master Room", "Master Room");
        put("Master ranked players can play", "Masters can play in this room");
        put("Review Room", "Review Room"); //for the chat room banner
        put("Members", "Members"); //people in a chat room

        //used by chat applet
        put("Type your message here.", "Type your message here."); //initial contents of the message line
        put("Message:", "To ALL:"); //label for the message line
        put("MessageTo:","To #1:");	//label for message to one user
        put("From #1","From #1");	// label for messages from a single user
        put("Send", "Send"); //label for the button to send the text
        put("User ", "User "); //default name for unnamed client

        //used by the common frame
        put("Options", "Options");
        put("Actions", "Actions");
        put("Sound", "Sound");

        // review rooms
        put("Show SGF Text", "Show SGF Text"); //in action menu of review rooms
        put("Select Game", "Select Game"); //menu label in review rooms
        put("game selector", "game selector"); // title for the game selector frame
        put("Match words:", "Match words:"); //used in the game selector
        put("reading directory...", "reading directory..."); //waiting for a directory to be read
        put("Error reading directory", "Error reading directory");
        put("Error reading file", "Error reading file");
        put("reading Zip directory...", "reading Zip directory...");
        put("Loading game file", "Loading game file"); //selecting a review game

        //used by the game client
        put("You have been disconnected: #1", "You have been disconnected: #1");
        put("Calling server...", "Calling server...");
        put("Wait for opponents to arrive.", "Wait for opponent to arrive.");
        put("player ", "player ");
        put("mouse tracking disabled", "mouse tracking disabled");
        put("the game has resumed", "the game has resumed");
        put("Take Over Playing", "Take Over Playing"); //menu item, take over playing a color
        put("Become a Spectator", "Become a Spectator");
        put("Let The Robot Play", "Let The Robot Play");
        put("(vacancy)", "(vacancy)"); //player who has quit
        put("Game in progress...", "Game in progress...");
        put("rankingsfailed", "Updating the rankings failed");

        //hints
        put("Moves are coming in", "Moves are coming in"); //moves arrive while in review mode	

        //menu options
        put("Messages", "Messages"); //name of menu
        put("Track Mice", "Track Mice");

        put("See Own Hints", "See Own Hints");
        put("See Opponent Hints", "See Opponent Hints");
        put("See Player Comments", "See Player Comments");
        put("See Spectator Comments", "See Spectator Comments");
        put("Joint Review", "Joint Review");
 // TODO: fix joint review strings in other languages
        put("#1 requests Joint review",
            "#1 requests Joint review.  Please select \"Joint Review\" in the messages menu");
        put("#1 starts Joint review","#1 starts Joint review");
        put("Timeout in 1 minute.", "Timeout in 1 minute.");
        put("Game timed out.  Session has ended.",
            "Game timed out.  Session has ended.");
        put("Chat Room", "Chat Room"); //for the chat room banner
        put("private room", "private room");
        put("This is now a private room", "This is now a private room");
        put("This is now a public room", "This is now a public room");
        put("Colorblind", "Colorblind");
        put("Please respect the 15 minute time limit",
            "Master games have a 15 minute time limit");
        put("Game", "Game"); //name for hints from the game
        put("Joining as a spectator", "#1 has joined as a spectator");
        put("enters the room", "#1 has entered the room"); //new member of a chat
        put("#1 is taking over for #2", "#1 is taking over for #2");
        put("#1 is taking over playing #2 for #3",
            "#1 is taking over playing #2 for #3");
        put("#1 deliberately quit", "#1 deliberately quit");
        put("#1 has quit and become a spectator",	// quit as a player.  Not in use right now.
            "#1 has quit and become a spectator");
        put("#1 killed due to #2", "#1 killed due to #2");
        put("quitting", "#1 has left the room");
        put("#1 quit", "#1 quit"); //quit (not deliberately)
        put("Welcome to Game client, version #1",
            "Welcome to BoardSpace client, version #1");
        put("play", "play for #1"); //play for a player who has quit.  #1 is a player name
        put("Selecting game: #1", "Selecting game: #1"); //loading a review game
        put("Hint", "Hint"); //the name for hints

        put("Move a ball or change a Ring", "Move a ball or change a Ring");
        put("Add a ball, remove a Ring", "Add a ball, remove a Ring");
        put("Add a ball", "Add a ball");
        put("Remove a Ring", "Remove a Ring");
        put("Capture a ball", "Capture a ball");
        put("Continue Capturing", "Continue Capturing");
        put("Finished Capturing, Click Done", "Finished Capturing, Click Done");
        put("Click Done", "Click Done");
        put("Game Over", "Game Over");
        put("WAIT", "WAIT");
        put("Resign", "Resign the game");
        put("Zertz", "Z�rtz");
        put("Zertz+11", "Z�rtz+11");
        put("Zertz+24", "Z�rtz+24");
        put("Player #1", "Player #1");
        put("Resign State", "Click Done to confirm your resignation");
        put("LOA", "Lines of Action");
        put("Plateau", "Plateau");

        // loa strings
        put("Select the #1 piece to move", "Select the #1 piece to move");
        put("White", "White");
        put("Black", "Black");
        put("Select the destination for the selected piece",
            "Select the destination for the selected piece");
        put("Black Pieces", "Black Pieces");
        put("White Pieces", "White Pieces");
        put("show grid", "show grid");

        put("Done", "Done");
        put("Edit", "Edit");

        put("LIMBO", "LIMBO");
        put("3w 4g 5b or 2 of each wins", "3w 3g 5b or 2 of each wins");
        put("4w 5g 6b or 3 of each wins", "4w 5g 6b or 3 of each wins");
        put("1 choice", "1 choice");
        put("#1 choices", "#1 choices");
        put("Start #1", "Start #1");
        put("Yinsh", "Yinsh");
        put("Dvonn", "Dvonn");

        // plateau strings
        put("Move stacks around any way you want to",
            "Move stacks around any way you want to");
        put("Build an initial stack of 2 to be placed",
            "Build an initial stack of 2 to be placed");
        put("Place your stack on the edge of the board",
            "Place your stack on the edge of the board");
        put("Onboard a piece, Move a stack, or Exchange",
            "Onboard a piece, Move a stack, or Exchange");
        put("Exchange Prisoners", "Exchange Prisoners");
        put("Move A Stack", "Move A Stack");
        put("Drop your piece onto the board or any of your stacks",
            "Drop your piece onto the board or any of your stacks");
        put("Wait", "Wait");
        put("Drop your piece onto the board or any stack",
            "Drop your piece onto the board or any stack");
        put("Click Done to Finish the move", "Click Done to Finish the move");
        put("Drop the moving stack back on the rack",
            "Drop the moving stack back on the rack");
        put("Pick up and Continue your move", "Pick up and Continue your move");
        put("Shuffle pieces to the exchange area, Click Done to start the exchange",
            "Shuffle pieces to the exchange area, Click Done to start the exchange");
        put("Click Done to complete the exchange",
            "Click Done to complete the exchange");
        put("Start White", "Start White");
        put("Start Black", "Start Black");
        put("Place prisoners here for exhange",
            "Place prisoners here for exchange");
        put("Prisoners captured by Black are held here",
            "Prisoners captured by Black are held here");
        put("Prisoners captured by White are held here",
            "Prisoners captured by White are held here");
        put("Move the flipped stack", "Move the flipped stack");
        put("The game record is not available during the game",
            "The game record is not available during the game");

        put("Select Robot", "Select Robot");

        // yinsh strings
        put("make 5 in a row to capture a ring, 3 rings win",
            "make 5 in a row to capture a ring, 3 rings win");
        put("Place a Ring on the board", "Place a Ring on the board");
        put("Pick a ring to place a chip and move",
            "Pick a ring to place a chip and move");
        put("Drop the ring", "Drop the ring");
        put("Click DONE to confirm this placement",
            "Click DONE to confirm this placement");
        put("Select a row of 5 to remove", "Select a row of 5 to remove");
        put("Click DONE to confirm this move", "Click DONE to confirm this move");
        put("Click DONE to confirm removing these markers",
            "Click DONE to confirm removing these markers");
        put("Select a ring to remove", "Select a ring to remove");
        put("Click DONE to confirm removing this ring",
            "Click DONE to confirm removing this ring");
        put("perspective","perspective");
        //
        // hex strings
        //
        put("Hex","Hex");
        put("Swap Colors","Swap Colors");
        put("connect opposite sides with a chain of markers",
        	"connect opposite sides with a chain of markers");
        put("Click Done to confirm this move",
       		"Click Done to confirm this move");
        put("Click on Done to confirm your resignation", 
       		"Click on Done to confirm your resignation");
        put("Place a marker on any empty cell",	
       		"Place a marker on any empty cell");
        put("Place a marker on any empty cell, or Swap Colors",
       		"Place a marker on any empty cell, or Swap Colors");
        put("Click Done to confirm swapping colors with your opponent",
       		"Click Done to confirm swapping colors with your opponent");
        
        // names of game variations for the lobby
        put("Yinsh-Blitz","Yinsh-Blitz");
        put("Hex-15","Hex-15");
        put("Hex-19","Hex-19");
        put("Trax","Trax");
        put("Trax_variation","standard Trax");
        put("make 5 in a row to capture a ring and win",
        		"make 5 in a row to capture a ring and win");
        
        put("Zertz_variation","standard Z�rtz");
        put("Zertz+11_variation","Z�rtz with 11 extra rings");
        put("Zertz+24_variation","Z�rtz with 24 extra rings");
        put("LOA_variation","standard 8x8 Lines of Action");
        put("LOAP_variation","play the LOA program contest variation");
        put("Plateau_variation","standard Plateau");
        put("Yinsh_variation","standard Yinsh");
        put("Yinsh-Blitz_variation","blitz Yinsh - the first ring wins");
        put("Dvonn_variation","standard Dvonn");
        put("Tamsk_variation","standard Tamsk");
        put("Hex_variation","11x11 Hex");
        put("Hex-15_variation","15x15 Hex");
        put("Hex-19_variation","19x19 Hex");
        put("Punct_variation","standard P�nct");
        put("TBA1_variation","game to be announced");
        put("TBA2_variation","game to be announced");
        put("TBA3_variation","game to be announced");
       
        // trax strings
        put("Tile Size","Tile Size");
        put("Place a tile","Place a tile");
        put("make any loop, or a line which spans 8 rows or columns, in your color",
        		"make any loop, or a line which spans 8 rows or columns, in your color");
        
        // punct strings
        put("Punct","P�nct");
        put("Click on the placed piece to rotate it, or Click Done",
        		"Click on the placed piece to rotate it, or Click Done");
        put("Place a piece on the board, or move a piece already on the board",
        		"Place a piece on the board, or move a piece already on the board");
        put("punctgoal","connect opposite sides of the board, or play your last piece while controlling more of the center");
        put("#1 left","#1 left");
        put("#1 center","#1 center");
     
        // gobblet strings
        put("make 4 in a row in any direction","make 4 in a row in any direction");
        put("Place a gobblet on the board, or move a gobblet","Place a gobblet on the board, or move a gobblet");
        put("Gobblet","Gobblet");
        put("Gobbletm","GobbletM");
        put("Gobblet_variation","Play with peeking allowed");
        put("Gobbletm_variation","Memory counts, no peeking");
        put("You must move the gobblet you have picked up",
        		"You must move the gobblet you have picked up");
         put("review","review");

        // hive strings
        put("Hive","Hive");
        put("Hive_variation","standard Hive");
        put("Place a tile on the board","Place a tile on the board");
        put("surround your opponents queen bee","surround your opponents queen bee");
        put("Place your queen on the board","Place your queen on the board");
        put("Place a tile on the board, or move a tile","Place a tile on the board, or move a tile");
        put("You have no legal moves.  Click on Done to pass",
                "You have no legal moves.  Click on Done to pass");
        put("Click on Done to end the game as a Draw",
        	"Click on Done to end the game as a Draw");
        put("Hive-L","Hive-L");
        put("Hive-L_variation","Hive with LadyBug added");
        put("Hive-LM","Hive-LM");
        put("Hive-LM_variation","Hive with LadyBug and Mosquito");

        put("#1 repetitions","#1 repetitions");
        // for plateau
        put("Click on Done to complete the capture",
        		"Click on Done to complete the capture");
        // game lobby
        put("The game is a draw","The game is a draw");
        put("Game won by #1","Game won by #1");
        put("You can only resign when it is your move",
        		"You can only resign when it is your move");
        put("Jan #1#2#3","#1 Jan, #2:#3 GMT");
        put("Feb #1#2#3","#1 Feb, #2:#3 GMT");
        put("Mar #1#2#3","#1 Mar, #2:#3 GMT");
        put("Apr #1#2#3","#1 Apr, #2:#3 GMT");
        put("May #1#2#3","#1 May, #2:#3 GMT");
        put("Jun #1#2#3","#1 Jun, #2:#3 GMT");
        put("Jul #1#2#3","#1 Jul, #2:#3 GMT");
        put("Aug #1#2#3","#1 Aug, #2:#3 GMT");
        put("Sep #1#2#3","#1 Sep, #2:#3 GMT");
        put("Oct #1#2#3","#1 Oct, #2:#3 GMT");
        put("Nov #1#2#3","#1 Nov, #2:#3 GMT");
        put("Dec #1#2#3","#1 Dec, #2:#3 GMT");
        
        // exxit strings
        put("Exxit","Exxit");
        put("Exxit-Beginner","Exxit-Beginner");
        put("Exxit-Blitz","Exxit-Blitz");
        put("Exxit-Pro","Exxit-Expert");
        put("Exxit_variation","play 39 tile Exxit");
        put("Exxit-Blitz_variation","play 29 tile Exxit");
        put("Exxit-Beginner_variation","play 19 tile Exxit");
        put("Exxit-Pro_variation","play expert Exxit");
        
        put("Rearrange things any way you want to", 
        	"Rearrange things any way you want to");
        put("Place a piece on the board","Place a piece on the board");
        put("Exchange an off-board piece for a new tile","Exchange an off-board piece for a new tile");
        put("Click on Done to confirm the Exchange","Click on Done to confirm the Exchange");
        put("Place a piece on the board, or Exchange an off-board piece for a new tile",
        		"Place a piece on the board, or Exchange an off-board piece for a new tile");
        put("Click on Done to end the game","Click on Done to end the game");
        put("Make a Dance move","Make a Dance move");
        put("Click on Done to confirm this Dance","Click on Done to confirm this Dance");
        put("Drop a tile to enlarge the board","Drop a tile to enlarge the board");
			  put("expand the board with your color tiles","expand the board with your color tiles");

        // system error
        put("Memory is low","Memory is low");
        
        // new strings for tablut
        put("Rearrange the gold fleet","Rearrange the gold fleet");
        put("Rearrange the silver fleet","Rearrange the silver fleet");
        put("Make your first move, or swap fleets","Make your first move, or swap fleets");
        put("Click Done to confirm swapping fleets with your opponent",
        		"Click Done to confirm swapping fleets with your opponent");
        put("Move one ship","Move one ship");
        put("Flagship must Reach the corner to win","Flagship must Reach the corner to win");
        put("Flagship wins at any edge square","Flagship wins at any edge square");
        put("Flagship can participate in captures","Flagship can participate in captures");
    	put("Flagship can not capture","Flagship can not capture");
   	 	put("Only the flagship can occupy the center square","Only the flagship can occupy the center square");
   	    put("center square is not special","center square is not special");
   	    put("Flagship must be surrounded on all 4 sides","Flagship must be surrounded on all 4 sides");
   	    put("Flagship is captured normally","Flagship is captured normally");
   	    put("Tablut-7","Tablut 7x7");
   	    put("Tablut-9","Tablut 9x9");
   	    put("Tablut-11","Tablut-11x11");
   	    put("Tablut-7_variation","Tablut on a 7x7 board");
   	    put("Tablut-9_variation","Tablut on a 9x9 board");
   	    put("Tablut-11_variation","Tablut on a 11x11 board");
   	    put("gold: escape the flagship  silver: capture gold flagship",
   	    		"gold: escape the flagship  silver: capture the gold flagship");
   	    
   	    put("Zertz+xx","Z�rtz Extreme");
   	    put("Zertz+xx_variation","Z�rtz with custom board layout");
   	    put("Make the first move, or swap and move second",
   	    		"Make the first move, or swap and move second");
   	    put("Click on Done to confirm swapping","Click on Done to confirm swapping");
   	    put("Position the starting rings","Position the starting rings");
        put("Swap","Swap");
        put("stack six or capture six","stack six or capture six");
        
        
        // stacking games
        put("Tablut","Tablut");
        put("Stacking Games","Stacking Games");
        put("Truchet_variation","standard Truchet");
        put("TumblingDown_variation","standard 8x8 TumblingDown");
        put("Truchet","Truchet");
        
        //dipole
         put("Dipole","Dipole (original)");
        put("Dipole-s","Dipole");
       // put("Dipole-10","Dipole 10x10");
        //put("Dipole-10_variation","Dipole on a 10x10 board");
        put("Dipole-s_variation","Dipole 8x8 with a symmetric starting position");
        put("Dipole_variation","Dipole 8x8 with an asymmetric starting position");
        put("Eliminate all your opponent's pieces","Eliminate all your opponent's pieces");
        put("Select the stack to move from","Select the stack to move from");
        put("You have no moves available. Click on Done to pass.","You have no moves available. Click on Done to pass.");
        //tumblingdown
        put("Capture your opponent's tallest king stack","Capture your opponent's tallest king stack");
        put("Pick the stack to move","Pick the stack to move");
        put("TumblingDown","TumblingDown");
        //truchet
        put("Occupy 3 enemy bases","Occupy 3 enemy bases");
        put("Flip a tile, or Move Split or Merge a stack",
        		"Flip a tile, or Move Split or Merge a stack");
        put("Move, Split or Merge a stack","Move, Split or Merge a stack");
        put("Split or Merge this stack, or click Done",
        		"Split or Merge this stack, or click Done");
        put("Split this stack, or click Done",
        		"Split this stack, or click Done");
        put("Merge this stack, or click Done","Merge this stack, or click Done");
        put("Complete the split","Complete the split");
        put("Complete the merge","Complete the merge");
        put("Complete a split or merge","Complete a split or merge");
        put("Continue merging or click Done","Continue merging or click Done");
        
        // hive-m
        put("Hive-M","Hive-M");
        put("Hive-M_variation","Hive with Mosquito added");
        // fanorona
        put("Fanorona","Fanorona");
        put("Fanorona_variation","standard Fanorona");
        put("Click on the line to be captured","Click on the line to be captured");
        put("Make an addional capturing move, or click on Done", 
        		"Make an additional capturing move, or click on Done");
        put("Make a capturing move","Make a capturing move");
        put("Move a piece one space","Move a piece one space");
        put("Capture all of your opponent's pieces","Capture all of your opponent's pieces");
        
        // forgotten for exxit
        put("use Wooden tiles","use Wooden tiles");
        // volcano
        put("Capture the best combinations of all 5 colors","Capture the best combinations of colors and sizes");
        put("Move a volcano cap","Move a volcano cap");
        put("Volcano", "Volcano");
        put("Volcano_variation","standard 5x5 Volcano");
        put("Volcano-r","randomized Volcano");
        put("Volcano-r_variation","Volcano with randomized positions");
        
        put("Volcano-h","Hex Volcano");
        put("Volcano-h_variation","Volcano on a Hexagonal grid");
       
        put("Volcano-hr","randomized Hex Volcano");
        put("Volcano-hr_variation"," Hex Volcano with randomized positions");
        put("Click on Done to end the game (you lose)","Click on Done to end the game (you lose)");
        
        // traboulet
        put("Traboulet","Traboulet");
        put("Traboulet_variation","standard Traboulet");
        put("Capture 7 red balls or all of your opponent's balls",
        "Capture 7 red balls or all of your opponent's balls");
        put("Make an addional move, or click on Done",
        		"Make an additional move, or click on Done");
        
        // dvonn
        put("Move a ring","Move a ring");
        put("Place a ring on the board","Place a ring on the board");
        put("Click Done to confirm this placement","Click Done to confirm this placement");
        put("Capture the most chips under your color stack","Capture the most chips under your color stack");
        put("Stack Height","Stack Height");
        
        // restartable games
        put("Restart","Restart");
        // tzaar
        put("Gipf Games","Gipf Games");
        put("Tzaar_variation","standard Tzaar");
        put("Tzaar","Tzaar");
        
        put("Closing...","Closing...");
        put("Played on #1","Played on #1");

        put("...plus #1 more","...plus #1 more");
         
        // tzaar
        put("Tzaar-random_variation","Tzaar - random starting position");
        put("Tzaar-standard_variation","Tzaar - standard starting position");
        put("Tzaar-custom_variation","Tzaar - custom starting position");
        put("Tzaar-custom","Tzaar - custom");
        put("Tzaar-random","Tzaar - random");
        put("Tzaar-standard","Tzaar - standard");
        put("Capture a stack","Capture a stack");
        put("Combine two stacks or capture another stack",
        		"Combine two stacks or capture another stack");
        
        // qyshinsu
        put("Place a stone on the board, or remove a stone from the board",
        "Place a stone on the board, or remove a stone from the board");
        put("Return to the Way","Return to the Way");
        put("Qyshinsu","Qyshinsu");
        put("Qyshinsu_variation","The mystery of the way");
        
        // knockabout
        put("Knock 5 of your opponent's dice into the gutter",
            "Knock 5 of your opponent's dice into the gutter");
        put("Knockabout_variation","standard Knockabout");
        put("Move a piece","Move a piece");
        put("Knockabout","Knockabout");
        		
        
        // gipf
        put("Place a chip on a starting point","Place a chip on a starting point");
        put("Slide the chip toward the center","Slide the chip toward the center");
        put("Designate a row to capture before your turn","Designate a row to capture before your turn");
        put("Click on Done to finish all captures","Click on Done to finish all captures");
        put("Designate a row to capture","Designate a row to capture");
        put("Capture most of your opponent's pieces","Capture most of your opponent's pieces");
        put("Gipf_variation","Gipf Basic (no Gipf pieces)");
        put("Gipf-standard_variation","Gipf Standard (with Gipf pieces)");
        put("Gipf", "Gipf Basic");
        put("Gipf-standard","Gipf Standard");
        put("Click on Gipf pieces to change their capture status",
        		"Click on Gipf pieces to change their capture status");
        put("Place a GIPF piece on a starting point","Place a GIPF piece on a starting point");
        put("Designate GIPF pieces to capture, or place a GIPF chip","Designate GIPF pieces to capture, or place a GIPF chip");
        put("Designate GIPF pieces to capture, or place a chip","Designate GIPF pieces to capture, or place a chip");
        
        // palago
        put("Palago_variation","standard Palago");
        put("Palago","Palago");
        put("select tile color","select tile color");
        put("Aqua","Aqua");
        put("Red","Red");
        put("Blue","Blue");
        put("Place a second tile adjacent to the first",
        		"Place a second tile adjacent to the first");
        put("Form a closed shape in your color","Form a closed shape in your color");
        
        // santorini
        put("Santorini","Santorini");
        put("Santorini_variation","Santorini (without Gods)");
        put("Move a Man up to level 3","Move a Man up to level 3");
        put("Build adjacent to the man you moved","Build adjacent to the man you moved");
        put("Place your first man on the board","Place your first man on the board");
        put("Place your second man on the board","Place your second man on the board");
        put("Move a Man","Move a Man");
        
        put("Pass","Pass");
        put("Placing Standard Pieces","Placing Standard Pieces");
        put("Placing Gipf Pieces","Placing Gipf Pieces");
        put("Gipf-tournament_variation","Gipf Expert (unlimited Gipf pieces)");
        put("Gipf-tournament","Gipf Expert");
        // "spangles"
        put("Tile Pattern Games","Tile Pattern Games");
        put("form a larger triangle with your color at the tips","form a larger triangle with your color at the tips");
        put("Spangles_variation","standard Spangles");
        
        put("Spangles","Spangles");
  
        // micropul
        put("Micropul","Micropul");
        put("Micropul_variation","standard Micropul");
         put("maximize the number of tiles in your reserve",
        		"maximize the number of tiles in your reserve");
        put("Place a Chip, or a Jewel, or a chip from the Store",
        		"Place a Chip, or a Jewel, or a chip from the Store");

        //Medina
        put("Medina","Medina");
        put("Medina_variation","standard Medina");
        put("Place the first meeple on the board","Place the first meeple on the board");
        put("Place a second piece on the board","Place a second piece on the board");
        put("Final Scores:","Final Scores:");
        put("You must place a dome","You must place a dome");
        put("build an ideal city","build an ideal city");
        
        // cannon
        put("Cannon","Cannon");
        put("Capture your opponent's town","Capture your opponent's town");
        put("Place your town in your first row","Place your town in your first row");
        put("Move a soldier, or fire a cannon","Move a soldier, or fire a cannon");
        put("Cannon_variation","standard Cannon");
        
        // warp 6
        put("Warp6","Warp 6");
        put("Warp6_variation","standard Warp 6");
        put("Move a ship to the board","Move a ship to the board");
        put("Move 6 ships to the center","Move 6 ships to the center");
        put("Move a ship","Move a ship");
        
        // Triad
        put("capture all of one opponentr's color","capture all of one opponentr's color");
        put("Capture a group of opponents","Capture a group of opponents");
        put("Drop bunny chip","Place one of the Bunny player's chips");
        put("Triad","Triad");
        put("Triad_variation","standard Triad");
         
        //Che
        put("Che","Che");
        put("Che_variation","standard Che");
        put("Place a tile on any empty cell","Place a tile on any empty cell");
        put("Place a tile adjacent to those already on the board",
        		"Place a tile adjacent to those already on the board");
        put("Place a second tile adjacent to those already on the board",
        		"Place a second tile adjacent to those already on the board");
        
        // trax
        put("Illegal - this move would form an illegal pattern of lines",
        		"Illegal - this move would form an illegal pattern of lines");
        
        // mutton
        put("Capturing Games","Capturing Games");
        put("Mutton","Mutton");
        put("Mutton-shotgun","shotgun Mutton");
        put("Mutton_variation","standard Mutton");
        put("Mutton-shotgun_variation","Mutton - farmer uses a shotgun");
        put("Farmer configuring board","Farmer configuring board");
        put("Play Farmer","Play Farmer");
        put("Hide","Hide");
        put("Wolf player hiding","Wolf player hiding");
        put("Wolf moving suspects","Wolf moving suspects");
        put("Eat","Eat");
        put("Wolf player eating","Wolf player eating");
        put("Farmer select","Farmer select");
        put("Shoot","Shoot");
        put("Farmer moving scared","Farmer moving scared");
        
        // octiles
        put("Octiles","Octiles");
        put("Octiles_variation","standard Octiles");
        put("Place the next tile on the board, where a runner can move over it",
        		"Place the next tile on the board, where a runner can move over it");
        put("Move a runner","Move a runner");
        put("Place a tile on the board, where a runner can leave a home space",
        		"Place a tile on the board, where a runner can leave a home space");
        put("Move a runner from a home space","Move a runner from a home space");
        put("Move your runners to the opposite side","Move your runners to the opposite side");
        
        // army of frogs
        put("ArmyOfFrogs","Army of Frogs");
        put("ArmyOfFrogs_variation","standard Army of Frogs");
        put("Place a Frog on the board","Place a Frog on the board");
        put("Move a Frog","Move a Frog");
        put("Connect all your frogs (at least 7) in one cluster",
        		"Connect all your frogs (at least 7) in one cluster");
        
        // variation to game
        put("Frogs","Army of Frogs");
        put("Loa","Lines of Action");
         put("No Recent Games","No Recent Games");
        
        // xiangqi
        put("Traditional Pieces","Traditional Pieces");
        put("Checkmate your opponent's general","Checkmate your opponent's general");
        put("Illegal move due to repetition - try something else",
        		"Illegal move due to repetition - try something else");
        put("Xiangqi","Xiangqi");
        put("Xiangqi_variation","standard Xiangqi");
        put("You have been offered a draw - Accept or Decline" ,
        		"You have been offered a draw - Accept or Decline" );
        put("Offer a Draw","Offer a Draw");
        put("Accept Draw","Accept Draw");
        put("Decline Draw","Decline Draw");
        put("Escape from check","Escape from check");
        
        put("Click Done to offer a draw","Click Done to offer a draw");
        put("Click Done to accept a draw","Click Done to accept a draw");
        put("Click Done to decline a draw","Click Done to decline a draw");
        
        // lobby
        put("select the type of room","select the type of room");
        put("select the game to play","select the game to play");
        put("select the game variation","select the game variation");
        
        put("Dipole_family","Dipole");
        put("Gipf_family","Gipf");
   		put("Zertz_family","Z�rtz");
   		put("Yinsh_family","Yinsh");
     	put("Dvonn_family","Dvonn");
        put("Tzaar_family","Tzaar");
        put("Punct_family","P�nct");
        put("Che_family","Che");
        put("Micropul_family","Micropul");
        put("Palago_family","Palago");
        put("Spangles_family","Spangles");
        put("Trax_family","Trax");
        put("Truchet_family","Truchet");
        put("TumblingDown_family","Tumbling Down");
        put("Cannon_family","Cannon");
        put("Fanorona_family","Fanorona");
        put("Knockabout_family","Knockabout");
        put("Traboulet_family","Traboulet");
        put("Triad_family","Triad");
        put("Xiangqi_family","Xiangqi");
        put("Octiles_family","Octiles");
        put("Warp6_family","Warp 6");
        put("ArmyOfFrogs_family","Army Of Frogs");
        put("LOA_family","Lines of Action");
        put("Hive_family","Hive");
        
        put("Racing Games","Racing Games");
        put("Connection Games","Connection Games");
        put("Plateau_family","Plateau");
        put("Hex_family","Hex");
        put("Gobblet_family","Gobblet");
        put("Exxit_family","Exxit");
        put("Tablut_family","Tablut");
        put("Volcano_family","Volcano");
        put("Qyshinsu_family","Qyshinsu");
        put("Santorini_family","Santorini");
        put("Medina_family","Medina");
        put("Mutton_family","Mutton");
        put("Tajii","Tajii");
        put("Tajii_family","Tajii");
        
        put("LOAP","LOAP");
        put("Tumblingdown","Tumbling Down");
        // misc
        put("All Games","All Games");
        // breaking away
        put("BreakingAway","Breaking Away");
        put("BreakingAway_variation","standard Breaking Away");
        put("BreakingAway_family","Breaking Away");
        put("Adjust","Adjust");
        put("Move a Cyclist","Move a Cyclist");
        put("Score the most points at the sprint and finish lines",
        		"Score the most points at the sprint and finish lines");
        put("Adjust your starting movements","Adjust your starting movements");
        put("Wait for other players to adjust","Wait for other players to adjust");
        put("The next sprint bonus is #1","The next sprint bonus is #1");
        put("#1 riders have crossed the second sprint line.",
        		"#1 riders have crossed the second sprint line.");
        put("#1 riders have crossed the first sprint line.",
        		"#1 riders have crossed the first sprint line.");
        put("#1 riders have finished","#1 riders have finished");
        put("The next finish bonus is #1","The next finish bonus is #1");
        put("Rider Size","Rider Size");
        put("Confirm Dropping this rider","Confirm Dropping this rider");
        put("Drop this rider from the Race","Drop this rider from the Race");
        put("Animate","Animate");
        
        // container
        put("Container","Container");
        put("Container-First","Container - Original");
        put("Euro Games","Euro Games");
        put("Container-First_variation","Container with the original rules");
        
        put("Auction","Auction");
        put("Maximize your profits","Maximize your profits");
        put("Bank","Bank");
        put("Loan for #1 from #2", "Loan for #1 from #2");
        put("Container_family","Container");
        put("Container_variation","Container with second shipment rules");
        put("Take your first action","Take your first action");
        put("Click Done to confirm this action","Click Done to confirm this action");
        put("Reprice the goods in your factory storage","Reprice the goods in your factory storage");
        put("Take your second action","Take your second action");
        put("Warehouse","Warehouse");
        put("Unsold Warehouses","Unsold Warehouses");
        put("#1 Ship","#1 Ship");
        put("#1 Machine","#1 Machine");
        put("#1 #2 Containers","#1 #2 Containers");
        put("Loan Card","Loan Card");
        put("Let someone else make the loan","Let someone else make the loan");
        put("I will make the loan","I will make the loan");
        put("fund a loan from a player or the bank","fund a loan from a player or the bank");
        put("Load the goods you will produce","Load the goods you will produce");
        put("set bid amount","set bid amount");
        put("Offer a price for the goods on the ship","Offer a price for the goods on the ship");
        put("Load the your warehouse from the seller","Load the your warehouse from the seller");
        put("Reprice the goods in your warehouse storage","Reprice the goods in your warehouse storage");
        put("Load your ship from the Dock","Load your ship from the Dock");
        put("Load more goods from the Dock","Load more goods from the Dock");
        put("Buy the goods for $#1","Buy the goods for $#1");
        put("Accept $#1 from #2","Accept $#1 from #2");
        put("Bid $ #1","Bid $ #1");
        put("Requesting Bids","Requesting Bids");
        put("you may increase your bid","you may increase your bid");
        put("Wait for the other players to decide","Wait for the other players to decide");
        put("accept the bid, or pay the amount yourself","accept the bid, or pay the amount yourself");
        put("trade a second container for a gold container","trade a second container for a gold container");
        put("You must take out a loan to pay interest","You must take out a loan to pay interest");
        put("Requesting Loan","Requesting Loan");
        put("No, let the bank fund it","No, let the bank fund it");
        put("Accept the loan from the bank", "Accept the loan from the bank");
        put("Yes, I will fund the loan","Yes, I will fund the loan");
        put("Place your bid to fund the loan","Place your bid to fund the loan");
        put("Decline the loan","Decline the loan");
        put("Accept this loan financing or not","Accept this loan financing or not");
        put("Choose the penalty for non-payment of interest","Choose the penalty for non-payment of interest");
        put("Loan for #1 from the bank","Loan for #1 from the bank");
        put("Spot Price #1","Spot Price #1");
        put("click Done to finish this Auction","click Done to finish this Auction");
        put("load a luxury container","load a luxury container");
        
        // for stats
        put("Cash/Score","Cash/Score");
        put("Machines","Machines");
        put("Warehouses","Warehouses");
        put("Shipping","Shipping");
        put("Island","Island");
        put("Loans","Loans");
        // arimaa
        put("Move a Rabbit to the last row","Move a Rabbit to the last row");
        put("Reverse View","Reverse View");
        put("Move a piece, step #1","Move a piece, step #1");
        put("Complete the push, step #1","Complete the push, step #1");
        put("Complete the pull, step #1","Complete the pull, step #1");
        put("Place all your pieces in your two home rows","Place all your pieces in your two home rows");
        put("Arimaa_family","Arimaa");
        put("Arimaa","Arimaa");
        put("Arimaa_variation","Standard Arimaa");
        put("Place the Rabbits","Place the Rabbits");
        put("Illegal due to repetition, try something else or resign","Illegal due to repetition, try something else or resign");
        put("Complete a push or pull, step #1","Complete a push or pull, step #1");
        
        // crossfire
        put("Crossfire_family","Crossfire");
        put("Crossfire","Crossfire");
        put("Crossfire_variation","standard Crossfire");
        put("own all the stacks","own all the stacks");
        put("#1 prisoners","#1 prisoners");
        put("#1 reserves","#1 reserves");
        
        // game controls 
        put("Rotate the board 180 degrees","Rotate the board 180 degrees");
        put("Spread out the stacks","Spread out the stacks");
        put("Adjust the chip spacing in stacks","Adjust the chip spacing in stacks");
        put("Switch to a more overhead view of the board","Switch to a more overhead view of the board");
        put("Switch to a more oblique view of the board","Switch to a more oblique view of the board");
    }
}