import java.util.List;
import java.util.Properties;
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

public class yue {
	private Environment env;
	private int posRow;
	private int posCol;

	private Properties props;
	private StanfordCoreNLP pipeline;
	private Action prevAct;
	private Scanner sc;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public yue(Environment env, int posRow, int posCol) {
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
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			// graph.prettyPrint();
			IndexedWord root = graph.getFirstRoot();
			String type = root.tag();
			switch (type) {
			case "VB":
				return processVB(graph, root);
			case "JJ":
				return processJJ(root);
			default:
				return processOthers(root);
			}
			/*
			 * List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|UH|JJ|VB");
			 * for (IndexedWord w : li) {
			 * 
			 * if (w.tag().equals("RB")) { return processRB(w.word()); } if
			 * (w.tag().equals("UH")) { return processUH(w.word()); } if
			 * (w.tag().equals("JJ")) { return processJJ(w.word()); } if
			 * (w.tag().equals("VB")) { return processVB(w.word()); } }
			 */

			// System.out.println("Cannot identify sentence structure.");
			// return Action.DO_NOTHING;
		}
		System.out.println("Empty sentence.");
		return Action.DO_NOTHING;
	}

	public static Action processJJ(IndexedWord root) {
		if (root.originalText().toLowerCase().equalsIgnoreCase("clean")) {
			System.out.println("Yes, clean.(JJ)");
			return Action.CLEAN;
		}

		return Action.DO_NOTHING;
	}

	public static Action processOthers(IndexedWord root) {
		System.out.println("Command: " + root.toString());

		if (root.originalText().toLowerCase().equalsIgnoreCase("clean")) {
			System.out.println("Yes, clean.");
			return Action.CLEAN;
		} else if (root.originalText().toLowerCase().equalsIgnoreCase("left")) {
			System.out.println("Yes, left");
			return Action.MOVE_LEFT;
		} else if (root.originalText().toLowerCase().equalsIgnoreCase("right")) {
			System.out.println("Yes, right");
			return Action.MOVE_RIGHT;
		} else if (root.originalText().toLowerCase().equalsIgnoreCase("up")) {
			System.out.println("Yes, up");
			return Action.MOVE_UP;
		} else if (root.originalText().toLowerCase().equalsIgnoreCase("down")) {
			System.out.println("Yes, down");
			return Action.MOVE_DOWN;
		}

		System.out.println("Type is invalid: " + root.tag().toString() + ", doing nothing");
		return Action.DO_NOTHING;
	}
	

	public static Action processVB(SemanticGraph graph, IndexedWord root) {
		// if verb close to right, then go right
		// if verb close to left, then go left
		// if verb close to up, then go up
		// if verb close to down, then go down
		// if verb close to clean, then go clean (In "please clean" command, clean is a verb)

		System.out.println("Command: " + root.toString());

		// filtering pick up
		if (root.originalText().toLowerCase().equalsIgnoreCase("pick")) {
			System.out.println("No, pick isn't valid.");
			return Action.DO_NOTHING;
		}
		// please clean
		if (root.originalText().toLowerCase().equalsIgnoreCase("clean")) {
			System.out.println("Yes, clean.(VB)");
			return Action.CLEAN;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> pairs = graph.childPairs(root);
		System.out.println(pairs.toString());

		for (Pair<GrammaticalRelation, IndexedWord> pair : pairs) {
			IndexedWord word = pair.second;

			if (word.originalText().toLowerCase().equalsIgnoreCase("left")) {
				System.out.println("Yes, left");
				return Action.MOVE_LEFT;
			} else if (word.originalText().toLowerCase().equalsIgnoreCase("right")) {
				System.out.println("Yes, right");
				return Action.MOVE_RIGHT;
			} else if (word.originalText().toLowerCase().equalsIgnoreCase("up")) {
				System.out.println("Yes, up");
				return Action.MOVE_UP;
			} else if (word.originalText().toLowerCase().equalsIgnoreCase("down")) {
				System.out.println("Yes, down");
				return Action.MOVE_DOWN;
			}

		}

		System.out.println("No, invalid VB, doing nothing");
		return Action.DO_NOTHING;
	}

}