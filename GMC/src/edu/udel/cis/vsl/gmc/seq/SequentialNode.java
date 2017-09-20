package edu.udel.cis.vsl.gmc.seq;

/**
 * A Node in the state graph for the sequential depth first search.
 * 
 * @author Yihao Yan (yanyihao)
 *
 */
public class SequentialNode<STATE> {

	private int id;

	/**
	 * The state that is associated with this node.
	 */
	private STATE state;

	/**
	 * True iff the Node has been seen.
	 */
	private boolean seen = false;
	/**
	 * The position index of which this {@link SequentialNode} is on Stack.
	 * Position 0 will be at the bottom of the stack (initial state). -1 means
	 * not on the stack.
	 */
	private int stackPosition = -1;

	/**
	 * True iff the State associated with the {@link SequentialNode} has already
	 * been expanded.
	 */
	private boolean fullyExpanded = false;

	/**
	 * Whether the State associated with the {@link SequentialNode} should be
	 * expanded.
	 */
	private boolean expand = true;

	/**
	 * Minimum depth at which this state has been encountered in DFS; used for
	 * finding minimal counterexample.
	 */
	private int depth = -1;

	public SequentialNode(STATE state, int id) {
		this.state = state;
		this.id = id;
	}
	
	

	/**
	 * Sets the "seen flag" to a given value.
	 * <p>
	 * The seen flag is intended to be used by a depth-first search algorithm,
	 * to mark that a state has been encountered in the search.
	 * 
	 * @param value
	 *            the value you want to assign to the seen flag associated to
	 *            that state
	 */
	public void setSeen(boolean value) {
		seen = value;
	}

	/**
	 * Returns the value of the seen flag associated to the given state.
	 * 
	 * @param state
	 *            any state in the state transition system
	 * @return the value of that state's seen flag.
	 */
	public boolean getSeen() {
		return seen;
	}

	/**
	 * <p>
	 * Set the stack position field of the {@link SequentialNode}.
	 * </p>
	 * <p>
	 * The "stack position" field is intended to be used by a depth-first search
	 * algorithm, to mark that a state is currently on the depth-first search
	 * stack.
	 * </p>
	 * <p>
	 * The bottom element in the stack will have an index 0.
	 * </p>
	 * 
	 * @param value
	 *            The value assigned to the stack position filed.
	 */
	public void setStackPosition(int stackPosition) {
		this.stackPosition = stackPosition;
	}

	/**
	 * Get the position of this {@link SequentialNode} on dfs stack or -1 if it
	 * is not on stack.
	 * 
	 * @return position of this state on dfs stack or -1
	 */
	public int getStackPosition() {
		return stackPosition;
	}

	/**
	 * @return true iff the target {@link SequentialNode} has been expanded.
	 */
	public boolean getFullyExpanded() {
		return fullyExpanded;
	}

	/**
	 * Set {@link SequentialNode#fullyExpanded} to a certain value.
	 * 
	 * @param value
	 *            The value that is assigned to
	 *            {@link SequentialNode#fullyExpanded} field.
	 */
	public void setFullyExpanded(boolean value) {
		fullyExpanded = value;
	}

	/**
	 * Set {@link SequentialNode#expand} to a given value.
	 * 
	 * @param expand
	 *            The value that is assigned to {@link SequentialNode#expand}.
	 */
	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	/**
	 * @return the value of {@link SequentialNode#expand}.
	 */
	public boolean getExpand() {
		return expand;
	}

	/**
	 * @return the value of {@link SequentialNode#depth}.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Set {@link SequentialNode#depth} to a given value.
	 * 
	 * @param depth
	 *            The value that is assigned to {@link SequentialNode#depth}.
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return the STATE this {@link SequentialNode} wraps.
	 */
	public STATE getState() {
		return state;
	}

	public int getId() {
		return id;
	}
}
