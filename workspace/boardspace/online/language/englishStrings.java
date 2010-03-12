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
        put("#1 has been muted, by a consensus of the players",
            "#1 has been muted, by a consensus of the players");
        put("welcome", "Welcome to the BoardSpace Lobby, version #1"); //version number follows
        put("website", "#1 BoardSpace"); //pretty name for the web site
        put("enterMessage", "#1 has entered the lobby"); //somebody entered
        put("maybeBad",
            "#1 is using client version #2, which may be incompatible"); //name is using client xx
        put("shouldbeOK", "#1 is using client version #2, which should be ok"); //different but ok version
        put("in #1", "in #1"); //followed by a session number
        put("Move ##1", "Move ##1"); //move number for games in lobby
        put("News", "News"); //header for news lines
        put("Lobby", "Lobby"); //the name of the lobby
        put("Error", "Error"); //the name for errors
        put("launchChat", "Launching chat room in Session #1"); //launching a chat room
        put("launchspectator", "Launching spectator in Session #1"); //session id follows
        put("launchGame", "Launching game in session #1"); //launching a game
        put("launchReview", "Launching review room in session #1");
        put("Session #1: state unknown.", "Session #1: state unknown.");
        put("Session #1 is idle.  ", "Session #1 is idle.  ");
        put("Session #1 is being launched.  ", "Session #1 is being launched.  ");
        put("Session #1 is active.  ", "Session #1 is active.  ");
        put("Session #1 is private. ", "Session #1 is private. ");
        put("Session #1 is displaying the map of players real locations",
            "Session #1 is displaying the map of players real locations");
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
        put("Review Saved Games", "Review Room");
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
        put("#1 has quit and become a spectator",
            "#1 has quit and become a spectator");
        put("#1 killed due to #2", "#1 killed due to #2");
        put("quitting", "#1 has left the room");
        put("#1 quit", "#1 quit"); //quit (not deliberately)
        put("Welcome to Game client, version #1",
            "Welcome to BoardSpace client, version #1");
        put("play", "play for #1"); //play color for xx
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
        put("Zertz", "Zèrtz");
        put("Zertz+11", "Zèrtz+11");
        put("Zertz+24", "Zèrtz+24");
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
        put("Trax_variation","Standard Trax");
        put("make 5 in a row to capture a ring and win",
        		"make 5 in a row to capture a ring and win");
        
        put("Zertz_variation","standard Zèrtz");
        put("Zertz+11_variation","Zèrtz with 11 extra rings");
        put("Zertz+24_variation","Zèrtz with 24 extra rings");
        put("LOA_variation","play 8x8 LOA");
        put("LOAP_variation","play the LOA program contest variation");
        put("Plateau_variation","standard Plateau");
        put("Yinsh_variation","standard Yinsh");
        put("Yinsh-Blitz_variation","blitz Yinsh - the first ring wins");
        put("Dvonn_variation","standard Dvonn");
        put("Tamsk_variation","standard Tamsk");
        put("Hex_variation","11x11 Hex");
        put("Hex-15_variation","15x15 Hex");
        put("Hex-19_variation","19x19 Hex");
        put("Punct_variation","standard Punct");
        put("TBA1_variation","game to be announced");
        put("TBA2_variation","game to be announced");
        put("TBA3_variation","game to be announced");
       
        // trax strings
        put("Tile Size","Tile Size");
        put("Place a tile","Place a tile");
        put("make any loop, or a line which spans 8 rows or columns, in your color",
        		"make any loop, or a line which spans 8 rows or columns, in your color");
        
        // punct strings
        put("Punct","Pünct");
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
        put("GobbletM","GobbletM");
        put("Gobblet_variation","Play with peeking allowed");
        put("GobbletM_variation","Memory counts, no peeking");
        put("You must move the gobblet you have picked up",
        		"You must move the gobblet you have picked up");
         put("review","review");

        // frogs strings
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
   	    
   	    put("Zertz+xx","Zèrtz Extreme");
   	    put("Zertz+xx_variation","Zèrtz with custom board layout");
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
        
        // frogs-m
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
        put("Knockabout_variation","Standard Knockabout");
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
        
        // palago
        put("Palago_variation","Standard Palago");
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
        put("Spangles_variation","Standard Spangles");
        
        put("Spangles","Spangles");
  
        // micropul
        put("Micropul","Micropul");
        put("Micropul_variation","Standard Micropul");
         put("maximize the number of tiles in your reserve",
        		"maximize the number of tiles in your reserve");
        put("Place a Chip, or a Jewel, or a chip from the Store",
        		"Place a Chip, or a Jewel, or a chip from the Store");

        //Medina
        put("Medina","Medina");
        put("Medina_variation","Standard Medina");
        
        //Che
        put("Che","Che");
        put("Che_variation","Standard Che");
        
        put("Place a GIPF piece on a starting point","Place a GIPF piece on a starting point");
        put("Designate GIPF pieces to capture, or place a GIPF chip","Designate GIPF pieces to capture, or place a GIPF chip");
        put("Designate GIPF pieces to capture, or place a chip","Designate GIPF pieces to capture, or place a chip");

        // Mutton
        put("Mutton", "Mutton");
        put("Mutton_variation", "Standard Mutton");
        put("Shoot", "Shoot");
        put("Pass", "Pass");
        put("Rage", "RAGE!");
        put("Hide", "Hide");
        put("Eat", "Eat");
        put("Play Farmer", "Play Farmer");
        put("Wolves_Hidden", "Wolves Hidden");
        put("Wolf player hiding", "Wolf player must choose 4 sheep to be wolves.");
        put("Wolf player eating", "Wolf player must choose a sheep to eat.");
        put("Wolf moving suspects", "Wolf player must move all suspect sheep away from the carcass.");
        put("Farmer select", "Farmer can select a sheep to shoot, or pass.");
        put("Farmer moving scared", "Farmer must move all suspect sheep away from the shooting victim.");
        put("Wolf no valid meals", "Wolf player has no valid meals; Must pass.");
        put("Farmer configuring board", "Farmer can set up board and set winning condition.");
        put("Mutton Goal", "Farmer must shoot all 4 wolves.");
     }
}