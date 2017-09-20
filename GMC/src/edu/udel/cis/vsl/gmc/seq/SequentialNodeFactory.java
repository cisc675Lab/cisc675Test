package edu.udel.cis.vsl.gmc.seq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.gmc.TraceStepIF;

/**
 * The factory to get a GMC search {@link SequentialNode}, if the
 * {@link SequentialNode} has been seen before, the seen {@link SequentialNode}
 * will be returned, otherwise, a new {@link SequentialNode} will be created and
 * returned.
 * 
 * @author Yihao Yan (yanyihao)
 */
public class SequentialNodeFactory<STATE, TRANSITION> {
	/**
	 * Maps each STATE to a unique {@link SequentialNode}.
	 */
	private Map<STATE, SequentialNode<STATE>> nodeMap = new HashMap<>();

	/**
	 * The counter used to count the # of {@link SequentialNode} cached in
	 * {@link #nodeMap}.
	 */
	private int nodeCounter = 0;

	/**
	 * A {@link StateManager} can be used to compute the next state and
	 * normalize a state.
	 */
	private StateManager<STATE, TRANSITION> stateManager;

	private boolean saveStates = true;

	private static int NOT_SAVED = -1;

	public SequentialNodeFactory(StateManager<STATE, TRANSITION> stateManager,
			boolean saveStates) {
		this.stateManager = stateManager;
		this.saveStates = saveStates;
	}

	/**
	 * <p>
	 * Implements the fly-weight pattern and normalize a state. Note that only
	 * normalized and canonical state will have id.
	 * </p>
	 * 
	 * @param state
	 * @return the existing {@link SequentialNode} mapped to the state, or a new
	 *         {@link SequentialNode} mapped to the state if there was no entry
	 *         in the map for the key state. Note that the
	 *         {@link SequentialNode} will always store the normalized or
	 *         simplified version of {@code state}.
	 */
	public SequentialNode<STATE> getNode(TraceStepIF<STATE> traceStep) {
		STATE state = traceStep.getFinalState();

		if (saveStates) {
			SequentialNode<STATE> result = nodeMap.get(state);

			if (result == null) {
				stateManager.normalize(traceStep);

				STATE normalizedState = traceStep.getFinalState();

				if (normalizedState != state) {
					result = nodeMap.get(normalizedState);
					if (result == null) {
						result = new SequentialNode<STATE>(normalizedState,
								nodeCounter++);
						nodeMap.put(normalizedState, result);
					}
				} else {
					result = new SequentialNode<STATE>(state, nodeCounter++);
				}
				nodeMap.put(state, result);
			}
			return result;
		} else
			return new SequentialNode<STATE>(state, NOT_SAVED);
	}

	/**
	 * Get the node associated to the given state, null there is no such a node.
	 * 
	 * @param state
	 *            The state whose associated node will be returned.
	 * @return the node associated to the given state, null there is no such a
	 *         node.
	 */
	public SequentialNode<STATE> getNode(STATE state) {
		return nodeMap.get(state);
	}

	/**
	 * Construct a new stack entry which will be pushed onto the stack.
	 * 
	 * @param node
	 *            The {@link SequentialNode} that wraps the source state.
	 * @param transitions
	 *            This could be the ample set or ample set complement of the
	 *            source state.
	 * @param full
	 *            Whether {@code transitions} is ample set complement or not.
	 * @return The newly constructed {@link StackEntry}.
	 */
	public StackEntry<STATE, TRANSITION> newStackEntry(
			SequentialNode<STATE> node, Collection<TRANSITION> transitions,
			int offset) {
		return new StackEntry<>(node, transitions, offset);
	}

	/**
	 * @return the number of search nodes saved.
	 */
	public int numOfSearchNodeSaved() {
		return nodeMap.size();
	}

	/**
	 * Get the {@link SequentialNode} associated with the initial state.
	 * 
	 * @param initState
	 * @return
	 */
	public SequentialNode<STATE> getInitialNode(STATE initState) {
		SequentialNode<STATE> initNode;

		if (saveStates) {
			initNode = new SequentialNode<STATE>(initState, nodeCounter++);
		} else
			initNode = new SequentialNode<STATE>(initState, NOT_SAVED);

		nodeMap.put(initState, initNode);
		return initNode;
	}

	/**
	 * Look up if there exists a {@link SequentialNode} associated with a
	 * non-initial state, if there is, return the {@link SequentialNode},
	 * otherwise return null.
	 * 
	 * @param state
	 * @return
	 */
	SequentialNode<STATE> lookup(STATE state) {
		return nodeMap.get(state);
	}
}
