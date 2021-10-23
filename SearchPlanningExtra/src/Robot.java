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
	private LinkedList<AState> p;
	private LinkedList<AState> P;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private int colD[] = { 0, 1, 0, -1 };
	private int rowD[] = { -1, 0, 1, 0 };

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
		Queue<State> open = new LinkedList<>();
		LinkedList<State> closed = new LinkedList<>();
		open.add(new State(posRow, posCol, new LinkedList<Action>()));
		this.openCount++;
		while (!open.isEmpty()) {
			State cell = open.poll();
			closed.add(cell);
			if (cell.row == env.getTargetRow() && cell.col == env.getTargetCol()) {
				
				this.pathFound = true;
				this.path = cell.getActions();
				this.pathLength = this.path.size();
				
				return;

			}
			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];
				
				if (env.validPos(row, col) && !containsState((LinkedList<State>) open, row, col)
						&& !containsState(closed, row, col)) {
					LinkedList<Action> nxt = (LinkedList<Action>) cell.getActions().clone();
					if (i == 0) {
						nxt.add(Action.MOVE_UP);
						
					} else if (i == 1) {
						
						nxt.add(Action.MOVE_RIGHT);
					} else if (i == 2) {
						
						nxt.add(Action.MOVE_DOWN);
					} else if (i == 3) {
						
						nxt.add(Action.MOVE_LEFT);
					}

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
		// LinkedList<Action> actions = new LinkedList<>();
		// contains, if it contains, remove
		LinkedList<Position> targets = env.getTargets();

		Queue<State> open = new LinkedList<>();
		LinkedList<State> closed = new LinkedList<>();
		open.add(new State(posRow, posCol, new LinkedList<Action>(), targets));
		this.openCount++;

		while (!open.isEmpty()) {
			State cell = open.poll();
			// System.out.println(cell.row + "," + cell.col + "; " + " ");
			closed.add(cell);

			if (containsPosition(cell.targets, cell.row, cell.col)) {
				LinkedList<Position> newtargets = removesPosition(cell.targets, cell.row, cell.col);
				cell.setTargets(newtargets);
			}

			if (cell.targets.size() == 0) {
				// targets found
				this.pathFound = true;
				this.path = cell.getActions();
				this.pathLength = this.path.size();
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
					newtargets = removesPosition(newtargets, row, col);
					nxt.setTargets(newtargets);
					// this.addAction(nxt.actions, i);
				}

				if (nxt.targets.size() == 0) {
					this.pathFound = true;
					this.path = nxt.getActions();
					this.pathLength = this.path.size();
					return;
				}

				if (!containsState(closed, nxt) && !containsState(open, nxt)) {
					// this.addAction(nxt.actions, i);
					open.add(nxt);
					this.openCount++;
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
		// int targetsFound = 0;
		open.add(new AState(posRow, posCol, 0, 0, new LinkedList<Action>(), targets));
		this.openCount++;

		while (!open.isEmpty()) {
			AState cell = open.poll();
			closed.add(cell);

			if (containsPosition(cell.targets, cell.row, cell.col)) {
				LinkedList<Position> newtargets = removesPosition(cell.targets, cell.row, cell.col);
				cell.setTargets(newtargets);
				// targetsFound++;
			}

			if (cell.targets.size() == 0) {
				// targets found
				this.pathFound = true;
				this.path = cell.getActions();
				this.pathLength = this.path.size();
				return;
			}

			for (int i = 0; i < 4; i++) {
				int row = cell.row + this.rowD[i];
				int col = cell.col + this.colD[i];
				if (!env.validPos(row, col)) {
					continue;
				}

				LinkedList<Action> as = (LinkedList<Action>) cell.actions.clone();

				this.addAction(as, i);

				LinkedList<Position> newTargetList = (LinkedList<Position>) cell.targets.clone();

				AState nextS = new AState(row, col, 0, 0, as, newTargetList);

				nextS.finalF = as.size() + calculateM12Astar(nextS, nextS.targets);
//				nextS.finalF = as.size() + calculateHAstar(nextS, nextS.targets);
//				nextS.finalF = as.size() + calculateM4Astar(nextS, nextS.targets);
//				nextS.finalF = as.size() + calculateM3567Astar(nextS, nextS.targets);

				if (containsAState(closed, nextS)) {
					continue;
				}

				boolean b = false;
				for (AState opens : open) {
					if (opens.row == nextS.row && opens.col == nextS.col
							&& this.sameTargets(opens.targets, nextS.targets)) {
						if (opens.finalF > nextS.finalF) {
							opens.setActions(nextS.actions);
							opens.finalF = nextS.finalF;

						}
						b = true;
						// continue;
					}
				}
				if (b)
					continue;
				open.add(nextS);
				this.openCount++;
			}
		}
		if (open.isEmpty())
			return;

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
				if (p1.getRow() == p2.getRow() && p1.getCol() == p2.getCol()) {
					k++;
					break;
				}
			}
		}
		if (k == l)
			return true;
		return false;
	}


	public boolean containsState(Queue<State> states, int row, int col) {
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

	public boolean containsAState(LinkedList<AState> states, AState check) {
		for (AState s : states) {
			if (s.row == check.row && s.col == check.col && this.sameTargets(s.targets, check.targets)) {
				return true;
			}
		}
		return false;
	}

	public AState containsAStateS(LinkedList<AState> states, AState check) {
		for (AState s : states) {
			if (s.row == check.row && s.col == check.col && this.sameTargets(s.targets, check.targets)) {
				return s;
			}
		}
		return null;
	}

	public boolean containsAStateQ(PriorityQueue<AState> states, AState check) {
		for (AState s : states) {
			if (s.row == check.row && s.col == check.col && this.sameTargets(s.targets, check.targets)) {
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

	public void removesState(Queue<State> positions, int row, int col) {
		for (State p : positions) {
			if (p.row == row && p.col == col) {
				positions.remove(p);
				break;
			}
		}
	}

	public LinkedList<Position> removesPosition(LinkedList<Position> positions, int row, int col) {
		for (int i = 0; i < positions.size(); i++) {
			Position p = positions.get(i);
			if (p.getRow() == row && p.getCol() == col) {
				positions.remove(i);
				return positions;
			}
		}
		return positions;

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

		public LinkedList<Action> getActions() {
			// TODO Auto-generated method stub
			return this.actions;
		}

		public void setTargets(LinkedList<Position> newtargets) {
			// TODO Auto-generated method stub
			this.targets = newtargets;
		}

		public void setActions(LinkedList<Action> actions) {
			// TODO Auto-generated method stub
			this.actions = actions;
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

	public int calculateHAstar(AState s, LinkedList<Position> targets) {
		int total = 0;
		for (Position t : targets) {
			total += Math.sqrt(Math.pow(s.row - t.getRow(), 2) + Math.pow(s.col - t.getCol(), 2));
		}
		return total / targets.size();
	}

	public int calculateM12Astar(AState s, LinkedList<Position> targets) {
		int total = 0;
		for (Position t : targets) {
			total += manhattanDist(s.row, s.col, t);
		}
		return total / targets.size();
	}

	public int calculateM3567Astar(AState s, LinkedList<Position> targets) {
		int total = 0;
		for (Position t : targets) {
			total += manhattanDist(s.row, s.col, t);
		}
		return total;
	}

	public int calculateM4Astar(AState s, LinkedList<Position> targets) {
		return targets.size();
	}

	public int manhattanDist(int row, int col, Position t) {
		return Math.abs(row - t.getRow()) + Math.abs(col - t.getCol());
	}



}