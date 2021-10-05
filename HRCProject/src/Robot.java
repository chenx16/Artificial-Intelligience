import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
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

	private Properties props;
	private StanfordCoreNLP pipeline;
	private Action prevAct = Action.DO_NOTHING;
	private Scanner sc;
	private String[] actionPrepends = { 
			"I think you want me to ", 
			"I am going to ", 
			"Ok, I will ", 
			"Sure, I am going to ", 
			"Got it! I will immediately " };

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;

		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		pipeline = new StanfordCoreNLP(props);

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
	public Action getAction() {
		Annotation annotation;
		System.out.print("> ");
		sc = new Scanner(System.in);
		String name = sc.nextLine();
//	    System.out.println(name);
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (name.contains("not")) {
			System.out.println("Sure, do nothing.");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			graph.prettyPrint();
			IndexedWord root = graph.getFirstRoot();
			List<Pair<GrammaticalRelation, IndexedWord>> pairs = graph.childPairs(root);
			String type = root.tag();
			String rootString = root.toString();
			System.out.println("Root: " + rootString);
			// System.out.println("Pairs: " + pairs.toString());
			/*
			 * if (pairs.size() == 0) { String todo = root.originalText().toLowerCase();
			 * switch (todo) { case "right": { this.prevAct = Action.MOVE_RIGHT; return
			 * Action.MOVE_RIGHT; } case "left": { this.prevAct = Action.MOVE_LEFT; return
			 * Action.MOVE_LEFT; } case "up": { this.prevAct = Action.MOVE_UP; return
			 * Action.MOVE_UP; } case "down": { this.prevAct = Action.MOVE_DOWN; return
			 * Action.MOVE_DOWN; } case "clean": { this.prevAct = Action.CLEAN; return
			 * Action.CLEAN; } case "again": { return this.prevAct; } case "moves": { return
			 * Action.DO_NOTHING; } default: { this.prevAct = Action.DO_NOTHING; return
			 * Action.DO_NOTHING; } } }
			 */
			switch (type) {
			case "VB":
				return processVB(graph, root);
			case "VBP":
				return processVB(graph, root);
			case "JJR": // root is more, more up
				return processVB(graph, root);
			default:
				return processSingleWord(name,root);
			}

		}
		System.out.println("Empty sentence.");
		return Action.DO_NOTHING;
	}


	private Action processSingleWord(String name, IndexedWord root) {
		System.out.println("Processing Single Word");
		String todo = root.originalText().toLowerCase();
		
		if (todo.equalsIgnoreCase("again") || todo.equalsIgnoreCase("repeat")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println(this.prevAct);
			return this.prevAct;
		} else if (name.contains("clean")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("clean.");
			this.prevAct = Action.CLEAN;
			return Action.CLEAN;
		} else if (todo.equalsIgnoreCase("undo")) {
			System.out.println("Yes, undo.");
			return negateMoves();
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

		// System.out.println("Command: " + root.toString());
		String todo = root.originalText().toLowerCase();
		// filtering pick up
		if (todo.equalsIgnoreCase("pick")) {
			System.out.println("No, pick isn't valid.");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		// please clean
		else if (todo.equalsIgnoreCase("clean")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("clean.");
			this.prevAct = Action.CLEAN;
			return Action.CLEAN;
		}
		// undo
		else if (todo.equalsIgnoreCase("undo")) {
			System.out.println("Yes, undo.(VB)");
			return negateMoves();
		}

		List<Pair<GrammaticalRelation, IndexedWord>> pairs = graph.childPairs(root);
		System.out.println(pairs.toString());

		for (Pair<GrammaticalRelation, IndexedWord> pair : pairs) {

			String word = pair.second.originalText().toLowerCase();
			// List<Pair<GrammaticalRelation, IndexedWord>> pair = graph.childPairs(word);
			//System.out.println(pair.toString());

			if (word.equalsIgnoreCase("left")) {
				System.out.print(getRandom(this.actionPrepends));
				System.out.println("move left");
				this.prevAct = Action.MOVE_LEFT;
				return Action.MOVE_LEFT;
			} else if (word.equalsIgnoreCase("right")) {
				System.out.print(getRandom(this.actionPrepends));
				System.out.println("move right");
				this.prevAct = Action.MOVE_RIGHT;
				return Action.MOVE_RIGHT;
			} else if (word.equalsIgnoreCase("up")) {
				System.out.print(getRandom(this.actionPrepends));
				System.out.println("move up");
				this.prevAct = Action.MOVE_UP;
				return Action.MOVE_UP;
			} else if (word.equalsIgnoreCase("down")) {
				System.out.print(getRandom(this.actionPrepends));
				System.out.println("move down");
				this.prevAct = Action.MOVE_DOWN;
				return Action.MOVE_DOWN;
			} else if (todo.equalsIgnoreCase("clean")) {
				System.out.print(getRandom(this.actionPrepends));
				System.out.println("clean");
				this.prevAct = Action.CLEAN;
				return Action.CLEAN;
			} else if (word.equalsIgnoreCase("again")) {
				System.out.print(getRandom(this.actionPrepends));
				System.out.println(this.prevAct);
				return this.prevAct;
			} else if (word.equalsIgnoreCase("back")) {
				System.out.println("Yes, going back");
				return negateMoves();
			}

			List<Pair<GrammaticalRelation, IndexedWord>> innerpair = graph.childPairs(pair.second);
			//System.out.println("innerpair" + innerpair);
			if (innerpair.size() != 0) {
				for (Pair<GrammaticalRelation, IndexedWord> p : innerpair) {
					String inword = p.second.originalText().toLowerCase();
					if (inword.equalsIgnoreCase("left")) {
						System.out.print(getRandom(this.actionPrepends));
						System.out.println("go left");
						this.prevAct = Action.MOVE_LEFT;
						return Action.MOVE_LEFT;
					} else if (inword.equalsIgnoreCase("right")) {
						System.out.print(getRandom(this.actionPrepends));
						System.out.println("go right");
						this.prevAct = Action.MOVE_RIGHT;
						return Action.MOVE_RIGHT;
					} else if (inword.equalsIgnoreCase("up")) {
						System.out.print(getRandom(this.actionPrepends));
						System.out.println("go up");
						this.prevAct = Action.MOVE_UP;
						return Action.MOVE_UP;
					} else if (inword.equalsIgnoreCase("down")) {
						System.out.print(getRandom(this.actionPrepends));
						System.out.println("go down");
						this.prevAct = Action.MOVE_DOWN;
						return Action.MOVE_DOWN;
					} else if (todo.equalsIgnoreCase("clean")) {
						System.out.print(getRandom(this.actionPrepends));
						System.out.println("clean.");
						this.prevAct = Action.CLEAN;
						return Action.CLEAN;
					}
				}
			}

		}

		System.out.println("No, invalid VB, doing nothing");
		this.prevAct = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action basicMoves(String todo) {
		//System.out.println("basic moving" + todo);
		if (todo.equalsIgnoreCase("left")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move left");
			this.prevAct = Action.MOVE_LEFT;
			return Action.MOVE_LEFT;
		} else if (todo.equalsIgnoreCase("right")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move right");
			this.prevAct = Action.MOVE_RIGHT;
			return Action.MOVE_RIGHT;
		} else if (todo.equalsIgnoreCase("up")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move up");
			this.prevAct = Action.MOVE_UP;
			return Action.MOVE_UP;
		} else if (todo.equalsIgnoreCase("down")) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move down");
			this.prevAct = Action.MOVE_DOWN;
			return Action.MOVE_DOWN;
		} else {
			System.out.println("Type is invalid, doing nothing");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
	}

	public Action negateMoves() {
		//System.out.println("negating move");
		if (this.prevAct.equals(Action.MOVE_RIGHT)) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move left");
			this.prevAct = Action.MOVE_LEFT;
			return Action.MOVE_LEFT;
		} else if (this.prevAct.equals(Action.MOVE_LEFT)) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move right");
			this.prevAct = Action.MOVE_RIGHT;
			return Action.MOVE_RIGHT;
		} else if (this.prevAct.equals(Action.MOVE_DOWN)) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move up");
			this.prevAct = Action.MOVE_UP;
			return Action.MOVE_UP;
		} else if (this.prevAct.equals(Action.MOVE_UP)) {
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move down");
			this.prevAct = Action.MOVE_DOWN;
			return Action.MOVE_DOWN;
		} else {
			System.out.println("No action before, doing nothing");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
	}
	
	private static String getRandom(String[] array) {
		int randomarray = new Random().nextInt(array.length);
		return array[randomarray];
	}

}