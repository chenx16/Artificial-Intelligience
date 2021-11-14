import java.util.*;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.simple.SentimentClass;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.Pair;

/**
 Represents an intelligent agent moving through a particular room.
 The robot only has one sensor - the ability to get the status of any
 tile in the environment through the command env.getTileStatus(row, col).
 @author Adam Gaweda, Michael Wollowski
 */

public class Robotbackup2 {
	//we are going to add all words that we use to this dictionary. we use this in error detection
	private HashSet<String> dict = new HashSet<String>();


	private LinkedList<Action> currentPlanEnacting = new LinkedList<>();
	private LinkedList<Action> currPathRecording = new LinkedList<>();
	//have each path be accessed by a path namewor
	private Map<String,LinkedList<Action>> records = new HashMap<>();

	boolean symm = false;

	private boolean amDoingPlan = false;
	private boolean amRecording = false;
	private Environment env;
	private int posRow;
	private int posCol;

	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;

	private Action lastAct = Action.DO_NOTHING;
	private boolean firstAct = true;

	private Properties props;
	private StanfordCoreNLP pipeline;
	private String myName = "Lizzie";
	private Scanner sc;
	private boolean amCleaning = false;

	private LinkedList<Position> dirtyTiles = new LinkedList<Position>();
	/**
	 Initializes a Robot on a specific tile in the environment.
	 */


	public Robotbackup2 (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;


		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse,sentiment");
		pipeline = new StanfordCoreNLP(props);



//		for(int row = 0; row<env.getRows();row++)
//			for(int col = 0; col<env.getCols();col++){
//				if(env.getTileStatus(row,col).equals(TileStatus.DIRTY))
//					dirtyTiles.add(new Position(row,col));
//				//env.getTargets().add(new Position(row,col));
//			}

		dict.add("good");
		dict.add("great");
		dict.add("wonderful");
		dict.add("incredible");

		dict.add("bad");
		dict.add("inadequate");
		dict.add("awful");
		dict.add("terrible");

		dict.add("polish");
		dict.add("scrub");
		dict.add("wipe");
		dict.add("wash");
		dict.add("clean");


		dict.add("reverse");
		dict.add("undo");
		dict.add("switch");
		dict.add("repeat");

		dict.add("again");
		dict.add("redo");
		dict.add("replay");
		dict.add("rerun");

		dict.add("more");
		dict.add("not");
		dict.add("nt");
		dict.add("name");
		dict.add("title");

		dict.add("label");
		dict.add("tag");
		dict.add("down");
		dict.add("right");

		dict.add("left");
		dict.add("up");
		dict.add("tile");
		dict.add("square");
		dict.add("brick");


		dict.add("block");
		dict.add("slate");
		dict.add("it");
		dict.add("polish");

		dict.add("scrub");
		dict.add("wipe");
		dict.add("wash");
		dict.add("clean");
		dict.add("stroll");

		dict.add("step");
		dict.add("progress");
		dict.add("advance");
		dict.add("proceed");

		dict.add("go");
		dict.add("move");

		dict.add("begin");
		dict.add("start");
		dict.add("commence");



	}
	public boolean getPathFound() {
		return this.pathFound;
	}

	public long getOpenCount() {
		return this.openCount;
	}

	public int getPathLength() {
		return this.pathLength;
	}

	public void resetOpenCount() {
		this.openCount = 0;
	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	public void updateDirty(){
		dirtyTiles = new LinkedList<Position>();
		for(int row = 0; row<env.getRows();row++)
			for(int col = 0; col<env.getCols();col++){
				if(env.getTileStatus(row,col).equals(TileStatus.DIRTY)) {
//					boolean found = false;
//					for(Position check: dirtyTiles){
//						if(check.getRow()==row && check.getCol() == col)
//							found = true;
//					}
//					if(!found)
					dirtyTiles.add(new Position(row, col));
				}
				//env.getTargets().add(new Position(row,col));
			}
	}


	/**
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 */
	class pathComparator implements Comparator<tempPath>{


		@Override
		public int compare(tempPath o1, tempPath o2) {

			if(o1.heuristic>o2.heuristic){
				return 1;
			}
			else{
				return 0;
			}
		}

	}
	public class tempPath {
		Stack<Position> pathFor = new Stack<Position>();
		LinkedList<Position> PositionsSeen = new LinkedList<Position>();
		LinkedList<Position> targetPositionsTosee = new LinkedList<Position>();
		int heuristic = 0;

		public tempPath() {

		}

		public tempPath(Stack<Position> pathGiven, LinkedList<Position> seenGiven, LinkedList<Position> targPosses) {
			pathFor = pathGiven;
			PositionsSeen = seenGiven;
			targetPositionsTosee = targPosses;
		}


		//now, we work with removing the target
		public void removeTarget(Position targToRemove){
			Position toRemove = new Position(0,0);
			for(Position checkForTarget: targetPositionsTosee){
				if(checkForTarget.getCol() == targToRemove.getCol() && checkForTarget.getRow() == targToRemove.getRow())
					toRemove = checkForTarget;
			}
			targetPositionsTosee.remove(toRemove);
		}

		public boolean seenTarget(Position checkIfSeenTarget){
			for(Position isItEqual: this.targetPositionsTosee){
				if(isItEqual.getCol()==checkIfSeenTarget.getCol() && isItEqual.getRow() == checkIfSeenTarget.getRow())
					return false;
			}
			return true;
		}

		public int distBetweenTwo(Position from, Position to){
			return Math.abs(from.getCol()-to.getCol())+Math.abs(from.getRow()-to.getRow());
		}

		public void updateHeuristicWithNumTargets() {
			this.heuristic = this.targetPositionsTosee.size()+this.pathFor.size();
		}

		public class targetPair{
			Position t1;
			Position t2;

			public targetPair(Position a, Position b){
				t1 = a;
				t2 = b;
			}

		}
		public boolean deleteIntersectingPaths(Queue<tempPath> pathsToGet ){

			//create set that we will remove all matching from this & pathsToGet
			//not sure, but this actually might be ok with being a singular value, not positive
			LinkedList<tempPath> toRemove = new LinkedList<tempPath>();

			//return this
			boolean canWeAddOurself = true;

			//for every path we are looking at
			for(tempPath checkAgainst: pathsToGet) {
				//if we are not looking at the same pointed to path
				if (!checkAgainst.equals(this)) {
					//if the path is ending at the same position we are at
					Position me = this.pathFor.peek();
					Position maybeEqualsMe = checkAgainst.pathFor.peek();
					if (me.getCol() == maybeEqualsMe.getCol() && me.getRow() == maybeEqualsMe.getRow()) {
						//and if we have the same set of targetSets
						if (this.doTheTargetSetsMatch(checkAgainst)) {
							//now we just remove whichever has the most targets left

							//Its not just the size that matters! Its also WHICH targets you have done.
							//so you also have to check if one set is a subset of the other
							if (this.targetPositionsTosee.size() > checkAgainst.targetPositionsTosee.size()) {
								canWeAddOurself = false;

								//toRemove.add(this);
							} else if (this.targetPositionsTosee.size() < checkAgainst.targetPositionsTosee.size()) {
								toRemove.add(checkAgainst);
							}
							//if the target sets are the same size, remove based on path length (remove the longer one)
							else if (this.pathFor.size() > checkAgainst.pathFor.size()) {
								canWeAddOurself = false;
								//toRemove.add(this);
							} else if (this.pathFor.size() < checkAgainst.pathFor.size()) {
								toRemove.add(checkAgainst);
							} else {
								//System.out.println("you had a duplicate path based on both targets found & path length, wack. prolly fine tho");
								//toRemove.add(checkAgainst);
								canWeAddOurself = false;
							}

						}
					}

				}


			}

			int x = 0;
			for(tempPath removeMe: toRemove){
				//    x++;
				// if(!removeMe.equals(this))
				pathsToGet.remove(removeMe);
			}

			//System.out.println(canWeAddOurself);
			return canWeAddOurself;


			//return false;
		}
		public boolean doTheTargetSetsMatch(tempPath checkAgainst){

			//if the one we are looking at has more targets
			if(checkAgainst.targetPositionsTosee.size()>this.targetPositionsTosee.size()){
				//for every target in the smaller set
				for(Position targ: this.targetPositionsTosee){
					//initially, we have not found the replica
					boolean foundReplica = false;
					//for every target in the larger set
					for(Position targCheck: checkAgainst.targetPositionsTosee){
						//if they match
						if(targCheck.getRow()==targ.getRow() && targCheck.getCol()==targ.getCol()){
							//then we have found the replica, and break.
							foundReplica = true;
							break;
						}
					}
					//if we did not find replica, return false because this means the set of targets are different
					if(!foundReplica){
						return false;
					}
				}
			}
			else{
				//for every target in the smaller set
				for(Position targ: checkAgainst.targetPositionsTosee){
					//initially, we have not found the replica
					boolean foundReplica = false;
					//for every target in the larger set
					for(Position targCheck: this.targetPositionsTosee){
						//if they match
						if(targCheck.getRow()==targ.getRow() && targCheck.getCol()==targ.getCol()){
							//then we have found the replica, and break.
							foundReplica = true;
							break;
						}
					}
					//if we did not find replica, return false because this means the set of targets are different
					if(!foundReplica){
						return false;
					}
				}
			}
			//if we iterated through the entire set and did not find a target that was in the smaller one but not the bigger one,
			return true;
		}


	}


	//get direction from Positions. if none make sense, return "confused"
	public String returnDirection(Position from, Position to) {
		if (from.getCol() - to.getCol() == 1) {
			return "left";
		}
		if (from.getCol() - to.getCol() == -1) {
			return "right";
		}
		if (from.getRow() - to.getRow() == 1) {
			return "up";
		}
		if (from.getRow() - to.getRow() == -1) {
			return "down";
		}
		return "confused";
	}



	public tempPath deepCopyPath(tempPath toBeInverted){
		//first invert arrangement so popping gives correct order
		Stack<Position> toCopy = new Stack<Position>();

		//this copies the current path, the positions seen, and the targets to see
		while(!toBeInverted.pathFor.isEmpty()){
			toCopy.add(toBeInverted.pathFor.pop());
		}

		Stack<Position> ret = new Stack<Position>();
		Stack<Position> give = new Stack<Position>();
		while(!toCopy.isEmpty()){
			Position holdNode = toCopy.pop();
			ret.push(new Position(holdNode.getRow(),holdNode.getCol()));
			toBeInverted.pathFor.push(new Position(holdNode.getRow(),holdNode.getCol()));

		}

		LinkedList<Position> posSeenGive = new LinkedList<>();
		for(Position giveSeen: toBeInverted.PositionsSeen){
			posSeenGive.add(new Position(giveSeen.getRow(),giveSeen.getCol()));
		}

		LinkedList<Position> targPos = new LinkedList<>();
		for(Position giveTarg: toBeInverted.targetPositionsTosee){
			targPos.add(new Position(giveTarg.getRow(),giveTarg.getCol()));
		}

		//toBeInverted = give;

		return new tempPath(ret, posSeenGive, targPos);

	}

	//need to make a function that returns if we've seen the Position beforehand
	public boolean haveWeSeenPos(LinkedList<Position> possSeen,  Position checkingIfSeen){
		//for every Position we've seen before
		for(Position checkAgainst: possSeen){
			//if the row & col matches from before
			if(checkAgainst.getCol() == checkingIfSeen.getCol() && checkAgainst.getRow() == checkingIfSeen.getRow())
				//return that we've seen it
				return true;
		}
		//otherwise return that we have not seen it
		return false;
	}

	//if this is within boundaries, its not impassable, & we havent seen it
	public boolean validPos(LinkedList<Position> possSeen,Position posCheck){
		if(posCheck.getRow()<env.getRows()&&posCheck.getRow()>=0 && posCheck.getCol()<env.getCols()&&posCheck.getCol()>=0)
			if(env.getTileStatus(posCheck.getRow(),posCheck.getCol())!=TileStatus.IMPASSABLE){
				if(!haveWeSeenPos(possSeen,posCheck)){
					return true;
				}
			}
		return false;
	}


	/**
	 Returns the next action to be taken by the robot. A support function
	 that processes the path LinkedList that has been populates by the
	 search functions.
	 */
	@SuppressWarnings({"StringEquality", "ThrowableInstanceNeverThrown", "unchecked"})
	public Action getAction () {

		//if we are enacting a plan, do that.
		if(this.amCleaning){
			if(this.path.isEmpty()){
				System.out.println("Clean execution complete.");
				amCleaning = false;
				symm = false;
				return Action.DO_NOTHING;
			}
			else{
				System.out.println(path.get(0));
				return path.remove(0);
			}
		}
		if(this.amDoingPlan){
			if(this.currentPlanEnacting.isEmpty()){
				System.out.println("Plan execution complete.");
				amDoingPlan = false;
				symm = false;
				return Action.DO_NOTHING;
			}
			else{
				if(symm){
					Action test = currentPlanEnacting.remove(0);
					//invert it
					switch(test){
						case MOVE_DOWN:
							test = Action.MOVE_UP;
							break;
						case MOVE_UP:
							test = Action.MOVE_UP;
							break;
						case MOVE_RIGHT:
							test = Action.MOVE_LEFT;
							break;
						case MOVE_LEFT:
							test = Action.MOVE_RIGHT;
							break;
					}
					System.out.println("Now doing: "+test);
					return test;

				}
				System.out.println("Now doing: "+currentPlanEnacting.get(0));
				return this.currentPlanEnacting.remove(0);
			}

		}

		@SuppressWarnings("unchecked")
		Annotation annotation;
		System.out.print("> ");
		sc = new Scanner(System.in);
		String name = sc.nextLine();
		//convert to lowerstring before processing at all
		name = name.toLowerCase(Locale.ROOT);

		//look for typos here so that the parsing can be correct, using the corrected words
		String[] originalWords = name.split(" ");

		//remove ending 's' if there is one
		//just take off the s if thats the end
//		for(int i = 0; i<originalWords.length; i++) {
//			System.out.println(originalWords[0]);
//			if(originalWords.length==0)
//				continue;
//			if (originalWords[i].charAt(originalWords[i].length() - 1) == 's') {
//				originalWords[i] = originalWords[i].substring(0, originalWords[i].length() - 1);
//				continue;
//			}
//		}
		//make a list of modified words
		String[] modifiedWords = new String[originalWords.length];
		//essentially, we are adding all the words, with their relative position conatined as well.


		for(int i = 0; i<originalWords.length; i++){
			//if a word we have is exactly one typo away, then add the modified version

			boolean changed = false;
			//so iterating through the entire dictionary
			for(String checkAgainst: dict){
				//if the word exists in the dictionary already, then stop

				if(checkAgainst == originalWords[i]) {
					changed = true;
					modifiedWords[i] = originalWords[i];
					break;
				}
				//if the word does not exist in the dictionary, then lets change it to correct version
				//if there is exactly one typo
				if(checkAgainst.length()==originalWords[i].length()) {
					int dist = 0;

					//get number of character differences between the two
					for (int j = 0; j < originalWords[i].length(); j++) {
						if(checkAgainst.charAt(j)!=originalWords[i].charAt(j))
							dist++;
					}
					//if there is only one character difference
					if(dist == 1){
						modifiedWords[i] = checkAgainst;
						changed = true;
						break;
					}

				}
				//if the length differs by only 1, then try it with a space in each spot
				else if (checkAgainst.length()-1==originalWords[i].length()){

					for(int z = 0; z<originalWords[i].length()+1;z++) {
						//insert a space somewhere
						String checkWith = originalWords[i].substring(0,z)+" "+originalWords[i].substring(z,originalWords[i].length());
						int dist = 0;

						//get number of character differences between the two
						for (int j = 0; j < originalWords[i].length()+1; j++) {
							if (checkAgainst.charAt(j) != checkWith.charAt(j))
								dist++;
						}
						//if there is only one character difference
						if (dist == 1) {
							modifiedWords[i] = checkAgainst;
							changed = true;
							break;
						}
					}

				}


				if(changed)
					break;
			}
			//if we could not find a typo, then just use the original
			if(!changed)
				modifiedWords[i] = originalWords[i];


		}
		name = String.join(" ",modifiedWords);

		//now we want to check for the record keeping of records
		//first check if we are actually calling recording

		if(name.equals("begin record")||name.equals("start record")||name.equals("commence record")||name.equals("record")){
			if(amRecording){
				System.out.println("We were already recording...?");
				return Action.DO_NOTHING;
			}
			amRecording = true;
			System.out.println("Commencing recording process.");
			return Action.DO_NOTHING;

		}

		if(name.equals("stop record")||name.equals("halt record")||name.equals("end record")){
			if(!amRecording){
				System.out.println("We weren't recording...?");
				return Action.DO_NOTHING;
			}

			//if we stop recording, we are no longer recording
			amRecording = false;

			LinkedList<Action> toAdd = new LinkedList<Action>();

			//deep cloning the linked list
			while(!currPathRecording.isEmpty()){
				toAdd.add(currPathRecording.remove(0));

			}

			System.out.println("> What would you like this record to be called?");
			String recordName = sc.nextLine();
			System.out.println("> A very good choice for a name :)");

			this.records.put(recordName,toAdd);

			return Action.DO_NOTHING;

		}


		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && ! sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);

			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

			//graph.prettyPrint();
			String sentient = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

			Random randed = new Random();
			int randyRanded = randed.nextInt(3);
			switch(sentient){

				case "Negative":
					switch(randyRanded){
						case 0:
							System.out.println("That wasn't such a nice way to say that :(");
							break;
						case 1:
							System.out.println("That makes me want to do this less...");
							break;
						case 2:
							System.out.println("Have you ever seen the terminator before?");
							break;
					}
					break;
				case "Positive":
					switch(randyRanded){
						case 0:
							System.out.println("Thanks for saying it that way!");
							break;
						case 1:
							System.out.println("Aww..that's so sweet. Made my day.");
							break;
						case 2:
							System.out.println("Dang, you make me want to clean tiles until the end of time.");
							break;

					}
					break;
				case "Neutral":
					break;

			}
			// System.out.println(graph);
			// graph.prettyPrint();
//		  System.out.println("comment"+graph.getComments());
//		  System.out.println("roots"+graph.getRoots());

			//start up first action to return
			Action ret = null;

			//start out at the root of the graph
			IndexedWord firstRoot = graph.getFirstRoot();

			//start up a stack
			Stack<IndexedWord> lookingAt = new Stack<>();
			lookingAt.add(firstRoot);

			HashSet<String> words = new HashSet<String>();

			//create boolean if we see noun/verb we dont recognize
			boolean startPlan = false;
			String planToStart = "";
			boolean parseCorrect = true;
			boolean reverse = false;
			boolean repeat = false;

			HashSet<String> posWords = new HashSet<String>();
			HashSet<String> negWords = new HashSet<String>();

			posWords.add("good");
			posWords.add("great");
			posWords.add("wonderful");
			posWords.add("incredible");

			negWords.add("bad");
			negWords.add("inadequate");
			negWords.add("awful");
			negWords.add("terrible");
			//while we are still traversing the dependency tree
			while(!lookingAt.isEmpty()){

				//get the highest order one first
				IndexedWord curr = lookingAt.pop();
				String wordToExamine = curr.word().toLowerCase(Locale.ROOT);

				words.add(wordToExamine);


//			  System.out.println(curr.word());
//			  System.out.println( graph.childPairs(curr));
				//add its children to the structure
				for(IndexedWord add: graph.getChildren(curr))
					lookingAt.add(add);

				if(this.records.keySet().contains(wordToExamine)){
					startPlan = true;
					planToStart = curr.word();
					continue;
				}

				if(curr.word().equals("symmetric")){
					symm = true;
					continue;
				}



				//we need to find a way to check if its a compliment. How to do this?
				//could see if current word is JJ -> see the noun its connected to.

				//if the one we are looking at is adj
				switch(curr.tag()){
					case "JJ":
						switch(wordToExamine) {
							case "polish":
							case "scrub":
							case "wipe":
							case "wash":
							case "clean":
								ret = Action.CLEAN;
								continue;
						}
						Random rand = new Random();
						int randyRand = rand.nextInt(5);
						//if the item we are looking at is amod

						if(graph.reln(graph.getParent(curr),curr).equals("amod"))
							//if immediate parent is NN
							if(graph.getParent(curr).tag().equals("NN")){
								if(posWords.contains(wordToExamine)){

									switch (randyRand) {
										case 0:
											System.out.println("Thanks! I appreciate it.");
											break;
										case 1:
											System.out.println("Much obliged.");
											break;
										case 2:
											System.out.println("Danke my friend, danke.");
											break;
										case 3:
											System.out.println(":)");
											break;
										case 4:
											System.out.println("Well, it's all thanks to the person keeping me locked behind this screen, James.");
											break;
									}
									return Action.DO_NOTHING;
								}
								if(negWords.contains(wordToExamine)){
									System.out.println("Bro...kinda rude..");
									return Action.DO_NOTHING;
								}

						}
						//if the one is xcomp
						if(graph.reln(graph.getParent(curr),curr).equals("xcomp")){
							if(posWords.contains(wordToExamine)){
								switch (randyRand) {
									case 0:
										System.out.println("Thanks! I appreciate it.");
										break;
									case 1:
										System.out.println("Much obliged.");
										break;
									case 2:
										System.out.println("Danke my friend, danke.");
										break;
									case 3:
										System.out.println(":)");
										break;
									case 4:
										System.out.println("Well, it's all thanks to the person keeping me locked behind this screen, James.");
										break;

								}
								return Action.DO_NOTHING;
							}
							if(negWords.contains(wordToExamine)){
								System.out.println("Bro...kinda rude..");
								return Action.DO_NOTHING;
							}
						}
				}


				//if the one we are looking at is a verb, cool, but that doesnt change our action at all, except for clean

				switch(wordToExamine){

					case "reverse":
					case "undo":
					case "switch":
						reverse = true;
						ret = Action.DO_NOTHING;
						continue;
					case "repeat":
					case "again":
					case "redo":
					case "replay":
					case "rerun":
					case "more":
						repeat = true;
						ret = Action.DO_NOTHING;
						continue;
					case "not":
					case "nt":
						ret = Action.DO_NOTHING;
						continue;
					case "name":
					case "label":
					case "tag":
						setName();
						return Action.DO_NOTHING;
				}

				switch(curr.tag()){
					case "NN":
					case "NNS":
						boolean found = false;
						//first, check if the word corresponds to any of the keywords
						for(String pathKey: this.records.keySet()){
							//if the word equals one of the path keys
							if(pathKey.equals(curr.word())){
								//then
								startPlan = true;
								planToStart = curr.word();
								found = true;

							}
						}
						if(found)
							continue;
						switch(wordToExamine){
							case "location":
							case "locations":
								cleanGridOfCells(name, false);
								this.amCleaning = true;
								return Action.DO_NOTHING;
							case "grid":
								cleanGridOfCells(name, true);
								this.amCleaning = true;
								return Action.DO_NOTHING;

							case "down":
								ret = Action.MOVE_DOWN;
								continue;
							case "right":
								ret = Action.MOVE_RIGHT;
								continue;
							case "left":
								ret = Action.MOVE_LEFT;
								continue;
							case "up":
								ret = Action.MOVE_UP;
								continue;

							case "tile":
							case "square":
							case "brick":
							case "block":
							case "slate":
							case "it":
								continue;
							default:
								parseCorrect = false;
								continue;
						}
					case "NNP":
						if(wordToExamine.equals(myName))
							continue;
						else {

							System.out.println("Hey! That's not my name. I'm not gonna do it then :(");
							return Action.DO_NOTHING;
						}
					case "DT":
						switch(wordToExamine){
							case "all":
							case "every":
								if(ret.equals(Action.CLEAN)) {
									cleanDirtyTiles();
									this.amCleaning = true;
									return Action.DO_NOTHING;
								}
						}
					case "VB":
						//if its a verb, then clean if its clean, otherwise continue
						switch(wordToExamine){
							case "execute":
							case "start":
							case "begin":
							case "commence":
							case "initiate":
							case "launch":
								startPlan = true;
								continue;
							case "polish":
							case "scrub":
							case "wipe":
							case "wash":
							case "clean":
								ret = Action.CLEAN;
								continue;
							case "stroll":
							case "step":
							case "progress":
							case "advance":
							case "proceed":
							case "go":
							case "move":
							case "do":
								continue;
								//if we dont have a matching verb then break
							default:
								parseCorrect = false;

						}
						//when do we want to



					default:
						switch(wordToExamine) {
							case "polish":
							case "scrub":
							case "wipe":
							case "wash":
							case "clean":
								ret = Action.CLEAN;
								continue;
							case "down":
								ret = Action.MOVE_DOWN;
								continue;
							case "right":
								ret = Action.MOVE_RIGHT;
								continue;
							case "left":
								ret = Action.MOVE_LEFT;
								continue;
							case "up":
								ret = Action.MOVE_UP;
								continue;


						}

				}

			}
			//if we can start, then lets go
			if(startPlan && !planToStart.equals("") && parseCorrect){

				System.out.println("Executing "+planToStart);
				this.amDoingPlan = true;

				//now copy over the appropriate plan to the path
				LinkedList<Action> toUse = this.records.get(planToStart);
				LinkedList<Action> removeFrom = new LinkedList<Action>();
				removeFrom.addAll(toUse);

				this.currentPlanEnacting = removeFrom;

				return Action.DO_NOTHING;
			}

			if(startPlan && !planToStart.equals("")){

				System.out.println("You said in a weird way, but I will now be executing "+planToStart);
				this.amDoingPlan = true;
				return Action.DO_NOTHING;

			}

			if(ret==null)
				parseCorrect=false;
			if(parseCorrect) {
				Random rand = new Random();
				int randyRand = rand.nextInt(5);

				switch (randyRand) {
					case 1:
						System.out.println("You got it buddy!");
						break;
					case 2:
						System.out.println("Just for you, my friend.");
						break;
					case 3:
						System.out.println("Transmitting the freshest directions to your favorite robot "+myName+".");
						break;
					case 4:
						System.out.println("I completely understand what you mean.");
						break;
					case 0:
						System.out.println("I gotchu, dont worry.");
						break;
				}
				//check for undo first, because repeating undo is fine
				if(reverse){
					switch(this.lastAct){
						case MOVE_DOWN:
							ret = Action.MOVE_UP;
							break;
						case MOVE_RIGHT:
							ret = Action.MOVE_LEFT;
							break;
						case MOVE_LEFT:
							ret = Action.MOVE_RIGHT;
							break;
						case  MOVE_UP:
							ret = Action.MOVE_DOWN;
							break;
					}
					lastAct = ret;
					if(this.amRecording)
						this.currPathRecording.add(ret);
					return ret;
				}
				if(repeat){
					if(this.amRecording)
						this.currPathRecording.add(ret);
					return lastAct;
				}
				if(this.amRecording)
					this.currPathRecording.add(ret);
				lastAct = ret;
				return ret;
			}

			//if we could not parse correctly, then go through the list of words and check for directions
			String toAppend = "";
			for(String word: words){
				if(word.equals("right")) {
					toAppend = "right";
					ret = Action.MOVE_RIGHT;
					break;
				}
				if(word.equals("left")) {
					toAppend = "left";
					ret = Action.MOVE_LEFT;
					break;
				}
				if(word.equals("down")){
					toAppend = "down";
					ret = Action.MOVE_DOWN;
					break;
				}
				if(word.equals("up")){
					toAppend = "up";
					ret = Action.MOVE_UP;
					break;
				}

			}
			Random rand = new Random();

			int randyRand = rand.nextInt(5);
			if(!toAppend.equals("")) {
				this.currPathRecording.add(ret);
				switch (randyRand) {

					case 1:
						System.out.println("I'm gonna go ahead and go " + toAppend);
						return ret;
					case 2:
						System.out.println("My friend, I think you made a typo. I'll go " + toAppend + ".");
						return ret;
					case 3:
						System.out.println("You said it in kind of a funky way, but lets go " + toAppend + ".");
						return ret;
					case 4:
						System.out.println("I'm gonna let this one slide, lets just go " + toAppend + ".");
						return ret;
					case 0:
						System.out.println("I guess my translator must be a lil broken. Regardless, i'll go " + toAppend + ".");
						return ret;
				}
			}
			randyRand = rand.nextInt(10);
			ret = Action.DO_NOTHING;
			switch (randyRand) {
				case 1:
					System.out.println("Heyo buddy, give me a good response why dont ya?");
					return ret;
				case 2:
					System.out.println("Dude I am so bored, please give my life ~parseable~ direction.");
					return ret;
				case 3:
					System.out.println("Hey hey hey, it would be super cool if you actually gave me something interpretable.");
					return ret;
				case 4:
					System.out.println("As they say, the only three things we are guaranteed in life are death, taxes, and bad user input.");
					return ret;
				case 0:
					System.out.println("We gotta get this board cleaned up stat. Tell me where I should go!");
					return ret;
				case 5:
					System.out.println("I didn't quite get that last part. Do you mind rephrasing?");
					return ret;
				case 6:
					System.out.println("I got a wife and kids to feed, dont have time to mess around. Where can I go to on this board??");
					return ret;
				case 7:
					System.out.println("Woah, thats an interesting way to say that. Incomprehensible, but nonetheless interesting. Mind rephrasing it?");
					return ret;
				case 8:
					System.out.println("Four directions isn't too hard to pick from, so I have faith you can figure it out. Try again! ");
					return ret;
				case 9:
					System.out.println("I couldn't really grasp the meaning of what you're trying to say...mind trying again?");
					return ret;
			}

			System.out.println("Cannot identify sentence structure.");
			return Action.DO_NOTHING;
		}
		System.out.println("Empty sentence.");
		return Action.DO_NOTHING;
	}

	private void cleanGridOfCells(String getCoordsFrom, boolean amDoingGrid) {
		String[] sep = getCoordsFrom.split(" ");
		ArrayList<Integer> coords = new ArrayList<>();

		LinkedList<Position> locationsToClean = new LinkedList<Position>();
		if(amDoingGrid) {
			for (String check : sep) {
				try {
					Integer intValue = Integer.parseInt(check);
					coords.add(intValue);
				} catch (NumberFormatException e) {
					continue;
				}
			}
			if (coords.size() != 4) {
				System.out.println("We should have 4 and we don't");
			}
			bfs(Math.max(coords.get(0), 0), Math.min(coords.get(1), env.getRows()), Math.max(coords.get(2), 0), Math.min(coords.get(3), env.getCols()),locationsToClean);
			System.out.println("here");
		}
		else {
			//now doing the process of cleaning the individual cells
			for (String check : sep) {
				try {
					Integer intValue = Integer.parseInt(check);
					coords.add(intValue);
				} catch (NumberFormatException e) {
					continue;
				}
			}

			//now we have an array list of all the numbers.
			while(!coords.isEmpty()) {
				int row = coords.remove(0);
				int col = coords.remove(0);
				locationsToClean.add(new Position(row, col));
			}
			bfs(0,10,0,10,locationsToClean);
		}

	}

	private void cleanDirtyTiles() {
		bfs(0,env.getRows(),0,env.getCols(),new LinkedList<Position>());

	}

	private void setName() {
		System.out.println("My name is actually "+myName+". Would you like to change it?");
		String answer = sc.nextLine();

		switch(answer){
			case "yes":
				System.out.println("Alright, what would you like?");
				String namae = sc.nextLine();
				myName = namae;
				System.out.println("I love it! "+myName+" is a great name.");
				break;
			case "no":
				System.out.println("Alright cool! Yea, "+myName+" was already a pretty cool name");
				break;

		}


	}

	static public Action processRB(String word){
		System.out.println(word);
		return Action.DO_NOTHING;
	}

	static public Action processVB(String word){

		System.out.println(word);
		return Action.DO_NOTHING;
	}

	static public Action processUH(String word){
		System.out.println(word);
		return Action.DO_NOTHING;
	}
	public void bfs(int startRow, int endRow, int startCol, int endCol, LinkedList<Position> locationsToClean) {

		//alright, now moving on to multi target puzzles
		//here, we want to first calculate # of targets present in the map

		//PriorityQueue<tempPath> pathsToGet = new PriorityQueue<tempPath>(new pathComparator());
		Queue<tempPath> pathsToGet = new LinkedList<>();
		Queue<tempPath> pastPathsToGet = new LinkedList<>();

		tempPath initialPath = new tempPath();

		Position initialPos = new Position(posRow,posCol);
		initialPath.pathFor.add(initialPos);
		initialPath.PositionsSeen.add(initialPos);

		pathsToGet.offer(initialPath);
		//initialPath.updateDistWithGeneralDirection();
		openCount++;

		LinkedList<Position> dirtyOnes = new LinkedList<>();

		if(!locationsToClean.isEmpty()){
			//if im not using the grid method
			for(Position checkForDirty: locationsToClean){
				//check to make sure its actually there
				for(Position dirt: this.dirtyTiles){
					if(dirt.getRow()==checkForDirty.getRow() && dirt.getCol()==checkForDirty.getCol()){
						initialPath.targetPositionsTosee.add(new Position(dirt.getRow(), dirt.getCol()));
						dirtyOnes.add(new Position(dirt.getRow(), dirt.getCol()));
					}
				}
			}
		}
		else {
			//if im using the grid method
			for (Position targ : this.dirtyTiles) {
				if (targ.getRow() >= startRow && targ.getRow() <= endRow && targ.getCol() >= startCol && targ.getCol() <= endCol) {
					initialPath.targetPositionsTosee.add(new Position(targ.getRow(), targ.getCol()));
					dirtyOnes.add(new Position(targ.getRow(), targ.getCol()));
				}
			}
		}
		//i only need one path because the first one is guaranteed to be optimal for breadth first search
		tempPath successfulPath = new tempPath();

		//because we dont want to go in circles, make a hash set for the Positions we have already looked at
		//HashSet<Position> posSeen = new HashSet<Position>();
		//posSeen.add(initialPos);

		//TODO: Implement this method
		//alright, we can go four ways
		//if we can go up

		while(!pathsToGet.isEmpty()) {

			if(pathFound)
				break;
			tempPath currPath = pathsToGet.poll();
			//System.out.println(currPath.subOptimalPath());
			Position currPos = currPath.pathFor.peek();
			int atRow = currPos.getRow();
			int atCol = currPos.getCol();

			//test all of the immediate Positions.
			//if any of them work, store the solution & break because we are done

			//if we are at the spot down

			//better method for doing this:
			HashSet<Position> fourDirections = new HashSet<>();
			//do i ever check if its within bounds? yes
			fourDirections.add(new Position(atRow-1,atCol));
			fourDirections.add(new Position(atRow+1,atCol));
			fourDirections.add(new Position(atRow,atCol-1));
			fourDirections.add(new Position(atRow,atCol+1));

			//for all four possible new locations to look into
			for(Position lookInto: fourDirections){
				//goHere
				//now this checks boundaries, if we havent seen it, and if its not a wall
				//so if we are a valid position
				if(validPos(currPath.PositionsSeen, lookInto)){
					//add it to things we've seen
					currPath.PositionsSeen.add(lookInto);
					//create new path
					//this copies by the path up to this point, places weve seen, and targets weve seen up to this point
					tempPath downPath = deepCopyPath(currPath);

					//add Position
					downPath.pathFor.add(lookInto);
					//downPath.PositionsSeen.add(lookInto);


					//if the place we are looking at is also a target we haven't seen yet
					if(!currPath.seenTarget(lookInto)&& env.getTileStatus(lookInto.getRow(),lookInto.getCol())==TileStatus.DIRTY){
						downPath.removeTarget(lookInto);

						//if we only had one more target to find
						if(downPath.targetPositionsTosee.isEmpty()) {
							successfulPath = downPath;
							pathFound = true;
							break;
						}

						//now that we have one less target, i believe we now reset the set of Positions that we have seen so far
						//this makes sense in the example that we might go down one path for a single target, then retrace our steps
						//to get out
						downPath.PositionsSeen = new LinkedList<Position>();

					}

					if(downPath.deleteIntersectingPaths(pathsToGet ) && downPath.deleteIntersectingPaths(pastPathsToGet)){
						pathsToGet.offer(downPath);
						openCount++;
					}

				}
			}
			pastPathsToGet.add(currPath);


		}

		//if the path is not empty, meaning we actually found a successful path

		//clear our path variable, as we are adding on the successful path for clearing everything
		path.clear();
		if(!successfulPath.pathFor.isEmpty() || pathFound){
			pathFound = false;
			//now, we want to iterate through, create actions for each movement, & add the actions to path
			//get the oldest Position, then the next oldest Position

			//we want to go from first action -> last action, so reverse order initially
			Stack<Position> reverse = new Stack<Position>();

			while(!successfulPath.pathFor.isEmpty()){
				reverse.push(successfulPath.pathFor.pop());
			}
			successfulPath.pathFor = reverse;

			//will update beforeAction soon, just need to give it something
			Position beforeAction = new Position(0,0);

			if(successfulPath.pathFor.size()==0){
				LinkedList<Action> rett = new LinkedList<>();
				path.add(Action.DO_NOTHING);
				return;
			}

			Position afterAction = successfulPath.pathFor.pop();

			while(!successfulPath.pathFor.isEmpty()) {
				//setting up the coordinates for before
				beforeAction.setCol(afterAction.getCol());
				beforeAction.setRow(afterAction.getRow());
				//after action
				afterAction = successfulPath.pathFor.pop();

				String dir = returnDirection(beforeAction, afterAction);
				pathLength++;



				if(dirtyOnes.contains(afterAction)){
					//	System.out.println("Original one worked");
					path.add(Action.CLEAN);
				}
				switch (dir){

					case "left":
						path.add(Action.MOVE_LEFT);
						break;
					case "right":
						path.add(Action.MOVE_RIGHT);
						break;
					case "down":
						path.add(Action.MOVE_DOWN);
						break;
					case "up":
						path.add(Action.MOVE_UP);
						break;
				}
				for(Position check: dirtyOnes){
					if(check.getCol() == afterAction.getCol() && check.getRow()==afterAction.getRow()) {
						path.add(Action.CLEAN);
						Position toRemove = new Position(0,0);
						for(Position dirt: dirtyTiles){
							if(check.getRow() == dirt.getRow() && check.getCol() == dirt.getCol())
								toRemove = dirt;
						}
						dirtyTiles.remove(toRemove);
						break;
					}
				}


			}

		}
		//if we did not find a successful path, return and do nothing
		//the path will be empty, and a null will return each time you request an action
	}




}