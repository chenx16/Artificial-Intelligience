import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

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
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private int rowD[] = { -1, 0, 1, 0 };
	private int colD[] = { 0, 1, 0, -1 };

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
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

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action getAction() {
		if (path.isEmpty()) {
			return Action.DO_NOTHING;
		} else {
			return path.removeFirst();
		}
	}

	/**
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void bfs() {
		// System.out.print("bbbbbbbbbbbbbbbbbbb\n");
		Queue<State> open = new LinkedList<>();
		LinkedList<State> closed = new LinkedList<>();
		open.add(new State(posRow, posCol, new LinkedList<Action>()));
		this.openCount++;
		while (!open.isEmpty()) {
			State cell = open.poll();
			closed.add(cell);
			// System.out.print(cell.col + "," + cell.row + "; " + " ");
			// TileStatus status = this.env.getTileStatus(cell.row, cell.col);
			if (cell.row == env.getTargetRow() && cell.col == env.getTargetCol()) {
				// System.out.println(cell.row+" "+ cell.col+"");
				this.pathFound = true;
				this.path = cell.getActions();
				this.pathLength = this.path.size();
				// System.out.print(pathLength);
				/*
				 * for (int i = 0; i < pathLength; i++) { System.out.println(path.get(i)); }
				 */
				return;

			}
			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];
				// for Map3
				// only checks open
				// if (env.validPos(row, col) && !containsState((LinkedList<State>) open, row,
				// col)) {

				// only checks closed
				// if (env.validPos(row, col) && !containsState(closed, row, col)) {

				// check both
				if (env.validPos(row, col) && !containsState((LinkedList<State>) open, row, col)
						&& !containsState(closed, row, col)) {

					LinkedList<Action> nxt = (LinkedList<Action>) cell.getActions().clone();
					if (i == 0)
						nxt.add(Action.MOVE_UP);
					else if (i == 1)
						nxt.add(Action.MOVE_RIGHT);
					else if (i == 2)
						nxt.add(Action.MOVE_DOWN);
					else if (i == 3)
						nxt.add(Action.MOVE_LEFT);
					open.add(new State(row, col, nxt));
					this.openCount++;
				}

			}

			if (open.isEmpty())
				return;

		}

	}

	/**
	 * This method implements breadth-first search for maps with multiple targets.
	 * It populates the path LinkedList and sets pathFound to true, if a path has
	 * been found. IMPORTANT: This method increases the openCount field every time
	 * your code adds a node to the open data structure, i.e. the queue or
	 * priorityQueue
	 * 
	 */
	public void bfsM() {
		// TODO: Implement this method
		LinkedList<Position> targets = env.getTargets();
		Queue<State> open = new LinkedList<>();
		LinkedList<State> closed = new LinkedList<>();
		open.add(new State(posRow, posCol, new LinkedList<Action>()));
		this.openCount++;
		while (!open.isEmpty()) {
			State cell = open.poll();
			// System.out.print(cell.col + "," + cell.row + "; " + " ");
			// TileStatus status = this.env.getTileStatus(cell.row, cell.col);
			closed.add(cell);
			if (containsPosition(targets, cell.row, cell.col)) {
				removesPosition(targets, cell.row, cell.col);
				if (targets.size() == 0) {
					this.pathFound = true;
					this.path = cell.getActions();
					this.pathLength = this.path.size();
					break;
				}

				else {
					// still other targets not find
					open.clear();
					closed.clear();

					// LinkedList<Action> next = (LinkedList<Action>) cell.getActions().clone();
					// cell = new State(cell.row, cell.col, next);

					closed.add(cell);

				}
			}

			// check its neighbors and decide next step
			// int ifnext = 0;
			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];

				if (env.validPos(row, col) && !containsState((LinkedList<State>) open, row, col)
						&& !containsState(closed, row, col)) {
					// ifnext ++;
					LinkedList<Action> nxt = (LinkedList<Action>) cell.getActions().clone();
					if (i == 0)
						nxt.add(Action.MOVE_UP);
					else if (i == 1)
						nxt.add(Action.MOVE_RIGHT);
					else if (i == 2)
						nxt.add(Action.MOVE_DOWN);
					else if (i == 3)
						nxt.add(Action.MOVE_LEFT);
					open.add(new State(row, col, nxt));
					this.openCount++;
				}

			}

			if (open.isEmpty())
				return;

		}
	}

	/**
	 * This method implements A* search. It populates the path LinkedList and sets
	 * pathFound to true, if a path has been found. IMPORTANT: This method increases
	 * the openCount field every time your code adds a node to the open data
	 * structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astar() {
		PriorityQueue<AState> open = new PriorityQueue<>();
		LinkedList<AState> closed = new LinkedList<>();
		open.add(new AState(posRow, posCol, 0, 0, new LinkedList<Action>()));
		this.openCount++;

		while (!open.isEmpty()) {
			AState cell = open.poll();
			closed.add(cell);
			if (cell.row == env.getTargetRow() && cell.col == env.getTargetCol()) {
				this.pathFound = true;
				this.path = cell.actions;
				this.pathLength = cell.actions.size();
				return;
			}
			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];
				if (env.validPos(row, col)) {
					LinkedList<Action> nxt = (LinkedList<Action>) cell.actions.clone();
					if (i == 0)
						nxt.add(Action.MOVE_UP);
					else if (i == 1)
						nxt.add(Action.MOVE_RIGHT);
					else if (i == 2)
						nxt.add(Action.MOVE_DOWN);
					else if (i == 3)
						nxt.add(Action.MOVE_LEFT);
					AState nextS = new AState(row, col, 0, cell.currentF + 1, nxt);
					// PriorityQueue<AState> openCopy = new PriorityQueue<AState>(open);
					if (!containsAStateQ(open, row, col) && !containsAState(closed, row, col)) {
						// ifnext ++;
						nextS.finalF = nextS.currentF + calculateH(nextS, env.getTargets().getFirst());
						open.add(nextS);
						this.openCount++;
					}

				}
			}

		}

	}

	/**
	 * This method implements A* search for maps with multiple targets. It populates
	 * the path LinkedList and sets pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the openCount field every time your code
	 * adds a node to the open data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astarM() {
		PriorityQueue<AState> open = new PriorityQueue<>();
		LinkedList<AState> closed = new LinkedList<>();
		LinkedList<Position> targets = env.getTargets();
		LinkedList<Position> ClosestTarget = new LinkedList<Position>();
		int targetsFound = 0;
		ClosestTarget.add(getClosestTarget(targets, posRow, posCol));
		open.add(new AState(posRow, posCol, 0, 0, new LinkedList<Action>(), ClosestTarget));
		this.openCount++;

		while (!open.isEmpty()) {
			AState cell = open.poll();
			closed.add(cell);
			if (cell.targets.isEmpty()) {
				if (targetsFound == env.getTargets().size() - 1) {
					this.pathFound = true;
					this.path = cell.actions;
					this.pathLength = cell.actions.size();
					return;
				} else {
					removesPosition(targets, cell.row, cell.col);
					ClosestTarget.clear();
					targetsFound++;
					ClosestTarget.add(getClosestTarget(targets, cell.row, cell.col));
					open.clear();
					closed.clear();
					open.add(new AState(cell.row, cell.col, cell.finalF, cell.currentF + 1,
							(LinkedList<Action>) cell.actions.clone(), (LinkedList<Position>) ClosestTarget.clone()));
					this.openCount++;
					continue;

				}
			}
			// recalculate the nearest target
			ClosestTarget.clear();
			ClosestTarget.add(getClosestTarget(targets, cell.row, cell.col));
			cell.targets = ClosestTarget;

			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];
				if (env.validPos(row, col)) {
					LinkedList<Action> nxt = (LinkedList<Action>) cell.actions.clone();
					if (i == 0)
						nxt.add(Action.MOVE_UP);
					else if (i == 1)
						nxt.add(Action.MOVE_RIGHT);
					else if (i == 2)
						nxt.add(Action.MOVE_DOWN);
					else if (i == 3)
						nxt.add(Action.MOVE_LEFT);
					LinkedList<Position> newTargetList = (LinkedList<Position>) cell.targets.clone();
					removesPosition(newTargetList, row, col);
					AState nextS = new AState(row, col, 0, cell.currentF + 1, nxt, newTargetList);
					if (!containsAStateQ(open, row, col) && !containsAState(closed, row, col)) {
						nextS.finalF = cell.currentF + calculateH(nextS, null);
						open.add(nextS);
						this.openCount++;
					}

				}
			}
		}

	}

	public class State {
		private int row;
		private int col;
		private LinkedList<Action> actions;

		public State(int row, int col, LinkedList<Action> actions) {
			this.row = row;
			this.col = col;
			this.actions = actions;
		}

		public LinkedList<Action> getActions() {
			return actions;
		}

		public void setActions(LinkedList<Action> actions) {
			this.actions = actions;
		}
	}

	public boolean containsPosition(LinkedList<Position> positions, int row, int col) {
		for (Position p : positions) {
			if (p.getRow() == row && p.getCol() == col) {
				return true;
			}
		}
		return false;
	}

	public boolean containsState(LinkedList<State> states, int row, int col) {
		for (State s : states) {
			if (s.row == row && s.col == col) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAState(LinkedList<AState> states, int row, int col) {
		for (AState s : states) {
			if (s.row == row && s.col == col) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAStateQ(PriorityQueue<AState> states, int row, int col) {
		for (AState s : states) {
			if (s.row == row && s.col == col) {
				return true;
			}
		}
		return false;
	}

	public void removesPosition(LinkedList<Position> positions, int row, int col) {
		for (int i = 0; i < positions.size(); i++) {
			Position p = positions.get(i);
			if (p.getRow() == row && p.getCol() == col) {
				positions.remove(i);
				break;
			}
		}
	}

	public class AState implements Comparable<AState> {
		int row;
		int col;
		int finalF = 0;
		int currentF;
		private LinkedList<Action> actions;
		LinkedList<Position> targets;

		// private LinkedList<Position> targets;
		public AState(int row, int col, int finalCost, int cost, LinkedList<Action> actions) {
			this.row = row;
			this.col = col;
			this.finalF = finalCost;
			this.currentF = cost;
			this.actions = actions;
			// this.targets =targets;
		}

		public AState(int row, int col, int finalCost, int cost, LinkedList<Action> actions,
				LinkedList<Position> targets) {
			this.row = row;
			this.col = col;
			this.finalF = finalCost;
			this.currentF = cost;
			this.actions = actions;
			this.targets = targets;
		}

		public int compareTo(AState s) {
			if (this.finalF < s.finalF)
				return -1;
			else if (this.finalF > s.finalF)
				return 1;
			else
				return 0;
		}
	}

	/*
	 * // The first function is g(n), which calculates the path cost between the
	 * start // node and the current node. public int calculateG(AState s) { return
	 * Math.abs(s.row - posRow) + Math.abs(s.col - posCol); }
	 */

	// The second function is h(n), which is a heuristic to calculate the estimated
	// path cost from the current node to the goal node.
	public int calculateH(AState s, Position t) {
		if (t == null) {
			LinkedList<Position> targets = (LinkedList<Position>) s.targets.clone();
			Position p = targets.poll();
			if (p == null)
				return 0;
			return manhattanDist(s.row, s.col, p);
		} else
			return manhattanDist(s.row, s.col, t);
	}

	public int manhattanDist(int row, int col, Position t) {
		return Math.abs(row - t.getRow()) + Math.abs(col - t.getCol());
	}

	/*
	 * // F(n) = g(n) + h(n). public int calculateF(AState s, Position target) {
	 * return calculateG(s) + calculateH(s, target); }
	 */
	public Position getClosestTarget(LinkedList<Position> targets, int row, int col) {
		Position toreturn = new Position(0, 0);
		int minD = 100;
		for (Position p : targets) {
			int dist = manhattanDist(row, col, p);
			if (dist < minD) {
				toreturn.setCol(p.getCol());
				toreturn.setRow(p.getRow());
				minD = dist;
			}
		}
		return toreturn;
	}

}