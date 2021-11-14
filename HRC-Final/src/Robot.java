import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
 */

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private boolean toCleanOrNotToClean;
	private String[][] policyForSelf; // = new String[env.getRows()][env.getCols()];
	private boolean recomputeValueMatrix = true;
	public Position placeToClean = new Position(-100, -100);

	private int colD[] = { 0, 1, 0, -1 };
	private int rowD[] = { -1, 0, 1, 0 };
	private Properties props;
	private StanfordCoreNLP pipeline;
	private Action prevAct = Action.DO_NOTHING;
	private Scanner sc;
	private String myname;
	private String[] actPrepends;
	private String[] clarifications;
	private String[] praise = { "No problem!", "Thank you! I've tried my best!", "Glad to privide service for you!",
			"Happy to work with you!", "Thanks, nice to meet you!" };
	private String[] reponses = { "Got it.", "Roger that.", "10-4.", "Ja ja.", "OK!" };
	private String[] spellingerrors = { "rite", "wright", "write", "mov", "muv", "op", "doen", "dawn", "cleen", "ledr",
			"lft" };
	private String[] positiveFeedback = { "Thanks a million.", "I truly appreciate you", "Grateful for your support" };
	private String[] negativeFeedback = { "My Apologies. It’s All My Fault. Developers have done their best.",
			"Sorry, my Bad. Developers have stayed up late everyday.",
			"I was wrong. Developers are on their way. Can you forgive me?" };
	private boolean ifNaming = false;
	private boolean isRecording;
	private boolean isExecuting;
	private boolean isNamingPlan;
	public boolean isAutoCleaning;
	private Queue<Action> path;
	private HashMap<String, LinkedList<Action>> recordedP;
	private LinkedList<Action> currentP;
	private LinkedList<Action> currentcombiningP;
	private Iterator<Action> pathIterator;
	private boolean isCleanCoor;
	private boolean isCleanRect;
	private boolean isResponding;
	private boolean isCombining;
	private int id;
	public String color;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol, int id) {
		this.id = id;
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		this.color = "black";
		this.myname = null;
		this.path = new LinkedList<Action>();
		this.isResponding = false;
		this.isRecording = false;
		this.isExecuting = false;
		this.isNamingPlan = false;
		this.isAutoCleaning = false;
		this.isCleanCoor = false;
		this.isCleanRect = false;
		this.isCombining = false;
		this.actPrepends = new String[5];
		this.clarifications = new String[10];
		this.currentP = new LinkedList<Action>();
		this.currentcombiningP = new LinkedList<Action>();
		this.recordedP = new HashMap<>();
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse,sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public LinkedList<Position> getTargets() {
		LinkedList<Position> targets = env.getTargets();
		Tile[][] map = this.env.getTiles();
		for (int row = 0; row < this.env.getRows(); row++) {
			for (int col = 0; col < this.env.getCols(); col++) {
				Tile tile = map[row][col];
				if (tile.getStatus() == TileStatus.DIRTY)
					targets.add(new Position(row, col));
			}
		}
		return targets;
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

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action valueInterationAction() {

		if (env.getTileStatus(placeToClean.row, placeToClean.col).equals(TileStatus.CLEAN))
			// then recompute value matrix
			recomputeValueMatrix = true;

		if (recomputeValueMatrix) {
			double[][] mat = env.computeValueMatrix(posRow, posCol, this);
			policyForSelf = env.retPolicy(mat);
			recomputeValueMatrix = false;

		}
		System.out.println(" ");

		if (env.getTileStatus(this.posRow, this.posCol) == (TileStatus.DIRTY)) {

			if (!env.haveWeAssignedThis(this.posRow, this.posCol)) {
				if (this.posRow == this.placeToClean.getRow() && this.posCol == this.placeToClean.getCol()) {
					recomputeValueMatrix = true;
				} else
					recomputeValueMatrix = false;
			}
			return Action.CLEAN;
		}

		String ret = policyForSelf[this.posRow][this.posCol];
		Action returnMe = Action.DO_NOTHING;

		Position goingInto = new Position(0, 0);

		switch (ret) {
		case "up":
			returnMe = Action.MOVE_UP;
			goingInto = new Position(posRow - 1, posCol);
			break;
		case "down":
			returnMe = Action.MOVE_DOWN;
			goingInto = new Position(posRow + 1, posCol);
			break;
		case "right":
			returnMe = Action.MOVE_RIGHT;
			goingInto = new Position(posRow, posCol + 1);
			break;
		case "left":
			returnMe = Action.MOVE_LEFT;
			goingInto = new Position(posRow, posCol - 1);
			break;
		}
		Random randy = new Random();
		int choice = randy.nextInt(10);

		if (choice == 0) {
			switch (ret) {
			case "up":
			case "down":
				goingInto = new Position(posRow, posCol - 1);
				returnMe = Action.MOVE_LEFT;
				break;
			case "left":
			case "right":
				goingInto = new Position(posRow - 1, posCol);
				returnMe = Action.MOVE_UP;
				break;
			}
		}
		if (choice == 1) {
			switch (ret) {
			case "up":
			case "down":
				goingInto = new Position(posRow, posCol + 1);
				returnMe = Action.MOVE_RIGHT;
				break;
			case "left":
			case "right":
				goingInto = new Position(posRow + 1, posCol);
				returnMe = Action.MOVE_DOWN;
				break;
			}
		}

		for (Robot check : env.currentRobotPositions.keySet()) {

			if (!check.equals(this)) {
				if (check.posRow == goingInto.getRow() && check.posCol == goingInto.getCol()) {
					return Action.DO_NOTHING;
				}
			}
		}
		return returnMe;

	}

	// handling the random and collision of 11 and 13 in last milestone
	public Action HandlingRandomandCollision(Action action) {
		String ret = "";
		if (action == Action.MOVE_RIGHT) {
			ret = "right";
		} else if (action == Action.MOVE_LEFT) {
			ret = "left";
		} else if (action == Action.MOVE_UP) {
			ret = "up";
		} else if (action == Action.MOVE_DOWN) {
			ret = "down";
		}
		Action returnMe = Action.DO_NOTHING;
		Position goingInto = new Position(0, 0);

		switch (ret) {
		case "up":
			returnMe = Action.MOVE_UP;
			goingInto = new Position(posRow - 1, posCol);
			break;
		case "down":
			returnMe = Action.MOVE_DOWN;
			goingInto = new Position(posRow + 1, posCol);
			break;
		case "right":
			returnMe = Action.MOVE_RIGHT;
			goingInto = new Position(posRow, posCol + 1);
			break;
		case "left":
			returnMe = Action.MOVE_LEFT;
			goingInto = new Position(posRow, posCol - 1);
			break;
		}
		Random randy = new Random();
		int choice = randy.nextInt(10);

		if (choice == 0) {
			switch (ret) {
			case "up":
			case "down":
				goingInto = new Position(posRow, posCol - 1);
				returnMe = Action.MOVE_LEFT;
				break;
			case "left":
			case "right":
				goingInto = new Position(posRow - 1, posCol);
				returnMe = Action.MOVE_UP;
				break;
			}
		}
		if (choice == 1) {
			switch (ret) {
			case "up":
			case "down":
				goingInto = new Position(posRow, posCol + 1);
				returnMe = Action.MOVE_RIGHT;
				break;
			case "left":
			case "right":
				goingInto = new Position(posRow + 1, posCol);
				returnMe = Action.MOVE_DOWN;
				break;
			}
		}

		for (Robot check : env.currentRobotPositions.keySet()) {

			if (!check.equals(this)) {
				if (check.posRow == goingInto.getRow() && check.posCol == goingInto.getCol()) {
					return Action.DO_NOTHING;
				}
			}
		}
		return returnMe;
	}

	public void updateDirty() {
		// TODO Auto-generated method stub

	}

	public LinkedList<Position> coordinatesToTargets(String s) {
		// (1,2),(3,4)
		LinkedList<Position> l = new LinkedList<Position>();
		int r = 0;
		int c = 0;
		boolean create = false;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ',' && s.charAt(i) != '(' && s.charAt(i) != ')') {
				if (create) {
					l.add(new Position(r, c));
					create = false;
				}
				if (s.charAt(i - 1) == '(') {
					r = Character.getNumericValue(s.charAt(i));
				} else if (s.charAt(i - 1) == ',') {
					c = Character.getNumericValue(s.charAt(i));
					create = true;
				}
			}
		}
		l.add(new Position(r, c));
		return l;
	}

	public LinkedList<Position> rectsToTargets(String s) {
		// (1,2),(3,4)
		Tile[][] map = this.env.getTiles();
		if (s.length() != 11) {
			System.out.println("Entered wrong coordinates");
		}
		LinkedList<Position> l = new LinkedList<Position>();
		LinkedList<Position> ts = new LinkedList<Position>();
		int r = 0;
		int c = 0;
		boolean create = false;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ',' && s.charAt(i) != '(' && s.charAt(i) != ')') {
				if (create) {
					l.add(new Position(r, c));
					create = false;
				}
				if (s.charAt(i - 1) == '(') {
					r = Character.getNumericValue(s.charAt(i));
				} else if (s.charAt(i - 1) == ',') {
					c = Character.getNumericValue(s.charAt(i));
					create = true;
				}
			}
		}
		l.add(new Position(r, c));
		for (int i = Math.min(l.get(0).row, l.get(1).row); i < Math.max(l.get(0).row, l.get(1).row) + 1; i++) {
			for (int j = Math.min(l.get(0).col, l.get(1).col); j < Math.max(l.get(0).col, l.get(1).col) + 1; j++) {
				Tile tile = map[i][j];
				if (tile.getStatus() == TileStatus.DIRTY)
					ts.add(new Position(i, j));
			}
		}
		return ts;
	}

	public void updateactPrepend() {
		this.actPrepends[0] = "I think you want me to ";
		this.actPrepends[1] = this.myname + " is going to ";
		this.actPrepends[2] = "Ok, " + this.myname + " will ";
		this.actPrepends[3] = "Sure, " + this.myname + " is going to ";
		this.actPrepends[4] = "Got it! " + this.myname + " will immediately ";

	}

	public void updateactClarification() {
		this.clarifications[0] = "I’m sorry but I’m not sure I understand. Could you say it in another way for "
				+ this.myname + "?";
		this.clarifications[1] = "I didn't quite get that. Can you clarify that for " + this.myname + "?";
		this.clarifications[2] = "Sorry, could you rephrase that for " + this.myname + "?";
		this.clarifications[3] = "I didn't catch that, could you please try one more time?";
		this.clarifications[4] = "Sorry, could you elaborate on that?";
		this.clarifications[5] = "I didn't understand that. Please try something else.";
		this.clarifications[6] = "Sorry, what do you want " + this.myname + " to do?";
		this.clarifications[7] = "Sorry, could you be more specific?";
		this.clarifications[8] = "Sorry, " + this.myname
				+ " is confused about what you said, could you please try again?";
		this.clarifications[9] = "I'm not sure what you want me to do, could you make it more clear?";
	}

	private void checkForExecutePlan(String input) {
		String plan;
		if (input.contains("execute plan ")) {
			plan = input.substring(13, input.length());

			System.out.println("Executing plan " + plan);
//			if (this.recordedP.isEmpty() || this.recordedP.get(plan) == null) {
//				System.out.println("Plan is not recorded");
//				return;
//			}
			if (this.env.plans.isEmpty() || this.env.getPlan(plan) == null) {
				System.out.println("Plan is not recorded");
				return;
			}
//			this.currentP = this.recordedP.get(plan);
			this.currentP = this.env.getPlan(plan);
			if (this.currentP == null) {
				System.out.println("No path found");
			}

			this.isExecuting = true;
			this.pathIterator = this.currentP.iterator();
		} else if (input.contains("execute symmetric plan ")) {
			plan = input.substring(23, input.length());

			System.out.println("Executing symmetric plan " + plan);
//			if (this.recordedP.isEmpty() || this.recordedP.get(plan) == null) {
//				System.out.println("Plan is not recorded");
//				return;
//			}
			if (this.env.plans.isEmpty() || this.env.getPlan(plan) == null) {
				System.out.println("Plan is not recorded");
				return;
			}
//			this.currentP = this.symmatric(this.recordedP.get(plan));
			this.currentP = this.symmatric(this.env.getPlan(plan));
//			System.out.println("heloooooooooooooo" + this.currentP.size());
			if (this.currentP == null) {
				System.out.println("No path found");
			}

			this.isExecuting = true;
			this.pathIterator = this.currentP.iterator();
		}
	}

	public void extractCombine(String s) {
		String plan1 = "";
		String plan2 = "";
		int l = s.length();
		for (int i = 12; i < l; i++) {
			if ((i + 3) < s.length() && s.charAt(i + 1) == 'a' && s.charAt(i + 2) == 'n' && s.charAt(i + 3) == 'd') {
				plan1 = s.substring(13, i);
				plan2 = s.substring(i + 5, l);
				break;
			}
		}
//		System.out.println("plan1" + plan1);
//		System.out.println("plan2" + plan2);

//		LinkedList<Action> arr1 = this.recordedP.get(plan1);

		LinkedList<Action> arr1 = this.env.getPlan(plan1);
//		System.out.println("111size" + arr1.size());
//		System.out.println("plan1" + (arr1.getFirst() == Action.MOVE_RIGHT));

//		LinkedList<Action> arr2 = this.recordedP.get(plan2);

		LinkedList<Action> arr2 = this.env.getPlan(plan2);
//		System.out.println("222size" + arr2.size());
//		System.out.println("plan2" + (arr2.get(0) == Action.MOVE_DOWN));
//		arr1.addAll(arr2);
		arr1.add(arr2.getFirst());
//		System.out.println("size" + arr1.size());
		this.currentcombiningP = arr1;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */

	public Action getAction() {
		if (this.env.inRecording && this.id != this.env.recordingRobot)
			return Action.DO_NOTHING;

		Annotation annotation;
		this.isResponding = false;

		if (this.myname == null && !this.ifNaming) {
			System.out.println("Hello! I am your private cleaner, would you please give me a name?");
		}
		if (this.isExecuting) {
			if (this.pathIterator.hasNext()) {
				return this.pathIterator.next();
			} else {
				this.isExecuting = false;
			}
		}
		if (isNamingPlan) {
			System.out.println("Please Enter Your Plan Name:");

		}
		if (isAutoCleaning) {
			if (!this.path.isEmpty()) {
				return this.path.poll();
			} else {
				this.isCleanCoor = false;
				this.isCleanRect = false;
				this.isAutoCleaning = false;
			}
		}
		String n;
		if (myname == null) {
			n = color;
		} else
			n = myname;
		System.out.print(n + "> ");
		sc = new Scanner(System.in);
		String name = sc.nextLine();
//	    System.out.println(name);
		name = name.toLowerCase();
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		int sentiment = this.getSentimentResult(sentences);
		// System.out.println(sentiment);
		if (sentiment == 1) {
			System.out.println(getRandom(this.positiveFeedback));
			this.isResponding = true;
		} else if (sentiment == -1) {
			System.out.println(getRandom(this.negativeFeedback));
			this.isResponding = true;
		}

		if (this.myname == null && !this.ifNaming) {
			this.ifNaming = true;
			this.myname = name;
			System.out.println(getRandom(this.reponses));
			System.out.println("Got the name!");
			updateactPrepend();
			updateactClarification();
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		if (isCleanCoor) {
			System.out.println("Automated Clean Coordinates if dirty");
			//bfsM(this.coordinatesToTargets(name));
			this.isAutoCleaning = true;
			return Action.DO_NOTHING;
		}
		if (isCleanRect) {
			System.out.println("Automated Clean dirty tile in the rectangle");
			LinkedList<Position> s = this.coordinatesToTargets(name);
			//bfsM(this.rectsToTargets(name));
			this.isAutoCleaning = true;
			return Action.DO_NOTHING;
		}
		if ((name.contains("begin record") || name.contains("start record")) && !this.isRecording) {
			this.isRecording = true;
			this.env.setRecord(this.id);
			this.currentP = new LinkedList<Action>();
			System.out.println("Recording started");
			return Action.DO_NOTHING;
		}
		if (name.contains("auto clean")) {
			System.out.println("Automated Cleaning started");
			//bfsM(this.getTargets());
			this.isAutoCleaning = true;
			return Action.DO_NOTHING;
		}
		if (name.contains("clean coordinates")) {
			System.out.println("Please enter coordinates in the form: (a,b),(c,d)");
			this.isCleanCoor = true;
			return Action.DO_NOTHING;
		}
		if (name.contains("clean rectangle")) {
			System.out.println(
					"Please enter coordinates in the form: (upperleft x,upperleft y),(lowerright x,lowerright y)");
			this.isCleanRect = true;
			return Action.DO_NOTHING;
		}
		if (name.contains("combine plan") && this.isCombining == false) {
			System.out.println("Combining two plans for you.");
			this.currentcombiningP = new LinkedList<Action>();
			extractCombine(name);
			this.isNamingPlan = true;
			this.isCombining = true;
			return Action.DO_NOTHING;
		}
		if (this.isRecording) {
			System.out.println("Recording path");
			if (prevAct != null) {
				this.currentP.add(this.prevAct);
			}
			if (name.contains("end record") || name.contains("finish record")) {
				this.isRecording = false;
				this.isNamingPlan = true;
				this.currentP.remove(0);
				System.out.println("Recording finished");
				return Action.DO_NOTHING;
			}

		}
		if (this.isNamingPlan) {
			if (this.isCombining) {
				Plan p = new Plan(name, currentcombiningP);
				this.env.plans.add(p);
				// this.recordedP.put(name, currentcombiningP);
				System.out.println("Named the combined plan " + name);
				this.isCombining = false;
				this.isNamingPlan = false;
			} else {
				Plan p = new Plan(name, currentP);
				this.env.plans.add(p);
				// this.recordedP.put(name, currentP);
				System.out.println("Named the plan " + name);
				this.isNamingPlan = false;
				this.env.setnonRecord();
			}
			return Action.DO_NOTHING;
		}

		this.checkForExecutePlan(name);

		// execute paths
		if (this.isExecuting) {
			if (this.pathIterator.hasNext()) {
				return this.pathIterator.next();
			} else {
				this.isExecuting = false;
			}
		}
		if (name.contains("not") || name.contains("don't") || name.contains("no")) {
			System.out.println(getRandom(this.reponses));
			System.out.println("Sure, do nothing.");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}

		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//			graph.prettyPrint();
			IndexedWord root = graph.getFirstRoot();
			List<Pair<GrammaticalRelation, IndexedWord>> pairs = graph.childPairs(root);
			String type = root.tag();
			String rootString = root.toString();
			// System.out.println("Root: " + rootString);
			// System.out.println("Pairs: " + pairs.toString());
			switch (type) {
			case "VB":
				return HandlingRandomandCollision(processVB(graph, root));
			case "VBP":
				return HandlingRandomandCollision(processVB(graph, root));
			case "JJR": // root is more, more up
				return HandlingRandomandCollision(processVB(graph, root));
			case "NN": // root is more, more up
				return HandlingRandomandCollision(processVB(graph, root));
			default:
				return HandlingRandomandCollision(processSingleWord(name, root, graph));
			}

		}
		System.out.println("Empty sentence.");
		if (!this.isResponding) {
			System.out.println(getRandom(this.clarifications));
		}
		return HandlingRandomandCollision(Action.DO_NOTHING);
	}

	private Action processSingleWord(String name, IndexedWord root, SemanticGraph graph) {
		// System.out.println("Processing Single Word");
		String todo = root.originalText().toLowerCase();
		todo = spellingErrorChecking(todo);

		if (todo.equalsIgnoreCase("again") || todo.equalsIgnoreCase("repeat")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			// System.out.println(this.prevAct);
			return this.prevAct;
		} else if (name.contains("clean") || todo.equalsIgnoreCase("clean")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("clean.");
			this.prevAct = Action.CLEAN;
			return Action.CLEAN;
		} else if (todo.equalsIgnoreCase("undo")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("Yes, undo.");
			return negateMoves();
		} else if (ifAskName(graph, root)) {
			System.out.println(getRandom(this.reponses));
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		} else if (ifPraise(graph, root)) {
			System.out.println(getRandom(this.reponses));
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		} else
			return basicMoves(todo);

	}

	public Action processVB(SemanticGraph graph, IndexedWord root) {
		// if verb close to right, then go right
		// if verb close to left, then go left
		// if verb close to up, then go up
		// if verb close to down, then go down
		// if verb close to clean, then go clean (In "please clean" command, clean is a
		// verb)

//		System.out.println("Command: " + root.toString());
		String todo = root.originalText().toLowerCase();
		// filtering pick up
		todo = spellingErrorChecking(todo);
//		System.out.println(todo);
		if (todo.equalsIgnoreCase("pick")) {
			System.out.println("No, pick isn't valid.");
			if (!this.isResponding) {
				System.out.println(getRandom(this.clarifications));
			}
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		// please clean
		else if (todo.equalsIgnoreCase("clean")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("clean.");
			this.prevAct = Action.CLEAN;
			return Action.CLEAN;
		}
		// undo
		else if (todo.equalsIgnoreCase("undo")) {
			System.out.println("Yes, undo.(VB)");
			return negateMoves();
		} else if (ifAskName(graph, root)) {
			System.out.println(getRandom(this.reponses));
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		} else if (ifPraise(graph, root)) {
			System.out.println(getRandom(this.reponses));
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> pairs = graph.childPairs(root);
		// System.out.println(pairs.toString());

		for (Pair<GrammaticalRelation, IndexedWord> pair : pairs) {

			String word = pair.second.originalText().toLowerCase();
			// List<Pair<GrammaticalRelation, IndexedWord>> pair = graph.childPairs(word);
			// System.out.println(pair.toString());
			word = spellingErrorChecking(word);
			if (word.equalsIgnoreCase("left")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				System.out.println("move left");
				this.prevAct = Action.MOVE_LEFT;
				return Action.MOVE_LEFT;
			} else if (word.equalsIgnoreCase("right")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				System.out.println("move right");
				this.prevAct = Action.MOVE_RIGHT;
				return Action.MOVE_RIGHT;
			} else if (word.equalsIgnoreCase("up")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				System.out.println("move up");
				this.prevAct = Action.MOVE_UP;
				return Action.MOVE_UP;
			} else if (word.equalsIgnoreCase("down")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				System.out.println("move down");
				this.prevAct = Action.MOVE_DOWN;
				return Action.MOVE_DOWN;
			} else if (todo.equalsIgnoreCase("clean")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				System.out.println("clean");
				this.prevAct = Action.CLEAN;
				return Action.CLEAN;
			} else if (word.equalsIgnoreCase("again")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				// System.out.println(this.prevAct);
				return this.prevAct;
			} else if (word.equalsIgnoreCase("back")) {
				System.out.println(getRandom(this.reponses));
				System.out.print(getRandom(this.actPrepends));
				System.out.println("Yes, going back");
				return negateMoves();
			}

			List<Pair<GrammaticalRelation, IndexedWord>> innerpair = graph.childPairs(pair.second);
			// System.out.println("innerpair" + innerpair);
			if (innerpair.size() != 0) {
				for (Pair<GrammaticalRelation, IndexedWord> p : innerpair) {
					String inword = p.second.originalText().toLowerCase();
					inword = spellingErrorChecking(inword);
					if (inword.equalsIgnoreCase("left")) {
						System.out.println(getRandom(this.reponses));
						System.out.print(getRandom(this.actPrepends));
						System.out.println("go left");
						this.prevAct = Action.MOVE_LEFT;
						return Action.MOVE_LEFT;
					} else if (inword.equalsIgnoreCase("right")) {
						System.out.println(getRandom(this.reponses));
						System.out.print(getRandom(this.actPrepends));
						System.out.println("go right");
						this.prevAct = Action.MOVE_RIGHT;
						return Action.MOVE_RIGHT;
					} else if (inword.equalsIgnoreCase("up")) {
						System.out.println(getRandom(this.reponses));
						System.out.print(getRandom(this.actPrepends));
						System.out.println("go up");
						this.prevAct = Action.MOVE_UP;
						return Action.MOVE_UP;
					} else if (inword.equalsIgnoreCase("down")) {
						System.out.println(getRandom(this.reponses));
						System.out.print(getRandom(this.actPrepends));
						System.out.println("go down");
						this.prevAct = Action.MOVE_DOWN;
						return Action.MOVE_DOWN;
					} else if (todo.equalsIgnoreCase("clean")) {
						System.out.println(getRandom(this.reponses));
						System.out.print(getRandom(this.actPrepends));
						System.out.println("clean.");
						this.prevAct = Action.CLEAN;
						return Action.CLEAN;
					}
				}
			}

		}
		if (!this.isResponding) {
			System.out.println(getRandom(this.clarifications));
		}
		// System.out.println("No, invalid VB, doing nothing");
		this.prevAct = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action basicMoves(String todo) {
		todo = spellingErrorChecking(todo);
		// System.out.println("basic moving" + todo);
		if (todo.equalsIgnoreCase("left")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move left");
			this.prevAct = Action.MOVE_LEFT;
			return Action.MOVE_LEFT;
		} else if (todo.equalsIgnoreCase("right")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move right");
			this.prevAct = Action.MOVE_RIGHT;
			return Action.MOVE_RIGHT;
		} else if (todo.equalsIgnoreCase("up")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move up");
			this.prevAct = Action.MOVE_UP;
			return Action.MOVE_UP;
		} else if (todo.equalsIgnoreCase("down")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move down");
			this.prevAct = Action.MOVE_DOWN;
			return Action.MOVE_DOWN;
		} else {
			if (!this.isResponding) {
				System.out.println(getRandom(this.clarifications));
			}
			// System.out.println("Type is invalid, doing nothing");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
	}

	public LinkedList<Action> symmatric(LinkedList<Action> actions) {
		LinkedList<Action> toreturn = new LinkedList<Action>();
		for (int i = 0; i < actions.size(); i++) {
			Action curr = actions.get(i);
			if (curr.equals(Action.MOVE_RIGHT)) {
				toreturn.add(i, Action.MOVE_LEFT);
			} else if (curr.equals(Action.MOVE_LEFT)) {
				toreturn.add(i, Action.MOVE_RIGHT);
			} else if (curr.equals(Action.MOVE_UP)) {
				toreturn.add(i, Action.MOVE_DOWN);
			} else if (curr.equals(Action.MOVE_DOWN)) {
				toreturn.add(i, Action.MOVE_UP);
			} else if (curr.equals(Action.DO_NOTHING)) {
				toreturn.add(i, Action.DO_NOTHING);
			} else if (curr.equals(Action.CLEAN)) {
				toreturn.add(i, Action.DO_NOTHING);
			}
		}
		return toreturn;
	}

	public Action negateMoves() {
		// System.out.println("negating move");
		System.out.println(getRandom(this.reponses));
		if (this.prevAct.equals(Action.MOVE_RIGHT)) {
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move left");
			this.prevAct = Action.MOVE_LEFT;
			return Action.MOVE_LEFT;
		} else if (this.prevAct.equals(Action.MOVE_LEFT)) {
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move right");
			this.prevAct = Action.MOVE_RIGHT;
			return Action.MOVE_RIGHT;
		} else if (this.prevAct.equals(Action.MOVE_DOWN)) {
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move up");
			this.prevAct = Action.MOVE_UP;
			return Action.MOVE_UP;
		} else if (this.prevAct.equals(Action.MOVE_UP)) {
			System.out.print(getRandom(this.actPrepends));
			System.out.println("move down");
			this.prevAct = Action.MOVE_DOWN;

			return Action.MOVE_DOWN;
		} else {
			System.out.println("No action before, " + this.myname + " doing nothing");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
	}

	private boolean ifAskName(SemanticGraph graph, IndexedWord root) {
		String word = root.originalText().toLowerCase();
		// Who are you?
		// What is your name?
		if (root.tag().equals("WP")) {
			List<Pair<GrammaticalRelation, IndexedWord>> pair = graph.childPairs(root);
			for (Pair<GrammaticalRelation, IndexedWord> p : pair) {
				IndexedWord w = p.second;
				word = w.originalText().toLowerCase();

				if (word.equalsIgnoreCase("name") || word.equalsIgnoreCase("you")) {
					System.out.println("Hello! My name is " + this.myname + ", and I am a intelligent cleaner!!");
					return true;
				}
			}
		}
		return false;
	}

	private boolean ifPraise(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText().toLowerCase();
		if (word.equalsIgnoreCase("good") || word.equalsIgnoreCase("nice") || word.equalsIgnoreCase("well")
				|| word.equalsIgnoreCase("like") || word.equalsIgnoreCase("love")) {
			System.out.println(getRandom(this.praise));
			return true;
		}
		// good job, well done, nice work
		List<Pair<GrammaticalRelation, IndexedWord>> pair = dependencies.childPairs(root);
		for (Pair<GrammaticalRelation, IndexedWord> p : pair) {
			IndexedWord w = p.second;
			word = w.originalText().toLowerCase();
			if (word.equalsIgnoreCase("good") || word.equalsIgnoreCase("nice") || word.equalsIgnoreCase("well")
					|| word.equalsIgnoreCase("like") || word.equalsIgnoreCase("love")) {
				System.out.println(getRandom(this.praise));
				return true;
			}
		}
		return false;
	}

	private String spellingErrorChecking(String s) {
		for (String check : this.spellingerrors) {
			if (s.equals(check)) {
				switch (s) {
				case "rite":
					return "right";
				case "wright":
					return "right";
				case "write":
					return "right";
				case "mov":
					return "move";
				case "op":
					return "up";
				case "dawn":
					return "down";
				case "doen":
					return "down";
				case "cleen":
					return "clean";
				case "ledr":
					return "left";
				case "lft":
					return "left";

				}

			}

		}
		return s;
	}

	private static String getRandom(String[] array) {
		int randomarray = new Random().nextInt(array.length);
		return array[randomarray];
	}

	public boolean containsState(LinkedList<State> states, int row, int col) {
		for (State s : states) {
			if (s.row == row && s.col == col) {
				return true;
			}
		}
		return false;
	}

	public boolean containsPosition(LinkedList<Position> positions, int row, int col) {
		for (Position p : positions) {
			if (p.row == row && p.col == col) {
				return true;
			}
		}
		return false;
	}

	public LinkedList<Position> removesPosition(LinkedList<Position> positions, int row, int col) {
		for (int i = 0; i < positions.size(); i++) {
			Position p = positions.get(i);
			if (p.row == row && p.col == col) {
				positions.remove(i);
				return positions;
			}
		}
		return positions;

	}

	public boolean containsState(LinkedList<State> states, State check) {
		for (State s : states) {
			if (s.row == check.row && s.col == check.col && this.sameTargets(s.targets, check.targets)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsState(Queue<State> states, State check) {
		for (State s : states) {
			if (s.row == check.row && s.col == check.col && this.sameTargets(s.targets, check.targets)) {
				return true;
			}
		}
		return false;
	}

	public boolean sameTargets(LinkedList<Position> targets1, LinkedList<Position> targets2) {
		int l = targets1.size();
		if (l != targets2.size())
			return false;

		int k = 0;
		for (Position p1 : targets1) {
			for (Position p2 : targets2) {
				if (p1.row == p2.row && p1.col == p2.col) {
					k++;
					break;
				}
			}
		}
		if (k == l)
			return true;
		return false;
	}

	public void bfsM(LinkedList<Position> targets) {
		// TODO: Implement this method
		// LinkedList<Action> actions = new LinkedList<>();
		// contains, if it contains, remove
		Queue<State> open = new LinkedList<>();
		LinkedList<State> closed = new LinkedList<>();
		open.add(new State(posRow, posCol, new LinkedList<Action>(), targets));

		while (!open.isEmpty()) {
			State cell = open.poll();
			// System.out.println(cell.row + "," + cell.col + "; " + " ");
			// TileStatus status = this.env.getTileStatus(cell.row, cell.col);
			closed.add(cell);

			if (containsPosition(cell.targets, cell.row, cell.col)) {
				LinkedList<Position> newtargets = removesPosition(cell.targets, cell.row, cell.col);
				cell.setTargets(newtargets);
				cell.actions.add(Action.CLEAN);
			}

			if (cell.targets.size() == 0) {
				this.path = cell.getActions();
				return;
			}

			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];

				if (!env.validPos(row, col)) {
					continue;
				}
				LinkedList<Position> newtargets = (LinkedList<Position>) cell.targets.clone();
				LinkedList<Action> as = (LinkedList<Action>) cell.getActions().clone();
				State nxt = new State(row, col, as, newtargets);
				this.addAction(nxt.actions, i);
				if (containsPosition(newtargets, row, col)) {
					nxt.actions.add(Action.CLEAN);
					newtargets = removesPosition(newtargets, row, col);
					nxt.setTargets(newtargets);
					// this.addAction(nxt.actions, i);
				}

				if (nxt.targets.size() == 0) {
					this.path = nxt.getActions();
					return;
				}

				if (!containsState(closed, nxt) && !containsState(open, nxt)) {
					// this.addAction(nxt.actions, i);
					open.add(nxt);
				}

			}

		}

		if (open.isEmpty())
			return;

	}

	public void addAction(LinkedList<Action> as, int i) {
		if (i == 0) {
			as.add(Action.MOVE_UP);

		} else if (i == 1) {
			as.add(Action.MOVE_RIGHT);

		} else if (i == 2) {
			as.add(Action.MOVE_DOWN);

		} else {
			as.add(Action.MOVE_LEFT);

		}
	}

	public class State {
		private int row;
		private int col;
		private LinkedList<Action> actions;
		LinkedList<Position> targets;

		public State(int row, int col, LinkedList<Action> actions) {
			this.row = row;
			this.col = col;
			this.actions = actions;
		}

		public State(int row, int col, LinkedList<Action> actions, LinkedList<Position> targets) {
			this.row = row;
			this.col = col;
			this.actions = actions;
			this.targets = targets;
		}

		public LinkedList<Action> getActions() {
			return actions;
		}

		public void setActions(LinkedList<Action> actions) {
			this.actions = actions;
		}

		public void setTargets(LinkedList<Position> targets) {
			this.targets = targets;
		}
	}

	public int getSentimentResult(List<CoreMap> sentences) {

		for (CoreMap sentence : sentences) {
			String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
			System.out.println(sentiment);
			if (sentiment.contains("positive") || sentiment.contains("Positive"))
				return 1;
			else if (sentiment.contains("negative") || sentiment.contains("Negative"))
				return -1;

		}

		return 0;

	}

}