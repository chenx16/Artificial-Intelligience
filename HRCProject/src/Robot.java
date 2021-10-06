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
	private String myname;
	private String[] actPrepends;
	private String[] clarifications;
	private String[] praise = { "No problem!", "Thank you! I've tried my best!", "Glad to privide service for you!",
			"Happy to work with you!", "Nice to meet you,too!" };
	private String[] reponses = { "Got it.", "Roger that.", "10-4.", "Ja ja.", "OK!" };
	private boolean ifNaming = false;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.myname = null;
		this.actPrepends = new String[5];
		this.clarifications = new String[10];
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
			System.out.println(getRandom(this.reponses));
			System.out.println("Sure, do nothing.");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		if (this.myname == null && !this.ifNaming) {
			System.out.println("Hello! I am your private cleaner, would you please give me a name?");
			this.ifNaming = true;
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		if (this.ifNaming) {
			this.myname = name;
			this.ifNaming = false;
			System.out.println(getRandom(this.reponses));
			System.out.println("Got the name!");
			updateactPrepend();
			updateactClarification();
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			// graph.prettyPrint();
			IndexedWord root = graph.getFirstRoot();
			List<Pair<GrammaticalRelation, IndexedWord>> pairs = graph.childPairs(root);
			String type = root.tag();
			String rootString = root.toString();
			//System.out.println("Root: " + rootString);
			// System.out.println("Pairs: " + pairs.toString());
			switch (type) {
			case "VB":
				return processVB(graph, root);
			case "VBP":
				return processVB(graph, root);
			case "JJR": // root is more, more up
				return processVB(graph, root);
			default:
				return processSingleWord(name, root, graph);
			}

		}
		System.out.println("Empty sentence.");
		System.out.println(getRandom(this.clarifications));
		return Action.DO_NOTHING;
	}

	private Action processSingleWord(String name, IndexedWord root, SemanticGraph graph) {
		//System.out.println("Processing Single Word");
		String todo = root.originalText().toLowerCase();

		if (todo.equalsIgnoreCase("again") || todo.equalsIgnoreCase("repeat")) {
			System.out.println(getRandom(this.reponses));
			System.out.print(getRandom(this.actPrepends));
			System.out.println(this.prevAct);
			return this.prevAct;
		} else if (name.contains("clean")) {
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

		// System.out.println("Command: " + root.toString());
		String todo = root.originalText().toLowerCase();
		// filtering pick up
		if (todo.equalsIgnoreCase("pick")) {
			System.out.println("No, pick isn't valid.");
			System.out.println(getRandom(this.clarifications));
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
		System.out.println(pairs.toString());

		for (Pair<GrammaticalRelation, IndexedWord> pair : pairs) {

			String word = pair.second.originalText().toLowerCase();
			// List<Pair<GrammaticalRelation, IndexedWord>> pair = graph.childPairs(word);
			// System.out.println(pair.toString());

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
				System.out.println(this.prevAct);
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
		System.out.println(getRandom(this.clarifications));
		// System.out.println("No, invalid VB, doing nothing");
		this.prevAct = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action basicMoves(String todo) {
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
			System.out.println(getRandom(this.clarifications));
			// System.out.println("Type is invalid, doing nothing");
			this.prevAct = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
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
					if (this.myname == null) {
						System.out.println("Hello! I don't have a name, please give me one!");
						this.ifNaming = true;
					} else {
						System.out.println("Hello! My name is " + this.myname + ", and I am a intelligent cleaner!!");
					}
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

	private static String getRandom(String[] array) {
		int randomarray = new Random().nextInt(array.length);
		return array[randomarray];
	}

}