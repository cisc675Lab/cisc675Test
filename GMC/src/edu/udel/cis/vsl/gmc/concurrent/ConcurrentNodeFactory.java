package edu.udel.cis.vsl.gmc.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import edu.udel.cis.vsl.gmc.seq.StateManager;

public class ConcurrentNodeFactory<STATE, TRANSITION> {
	/**
	 * Maps each STATE to a unique {@link SequentialNode}.
	 */
	private Map<STATE, ConcurrentNode<STATE>> nodeMap = new HashMap<>();

	/**
	 * A {@link StateManager} can be used to compute the next state and
	 * normalize a state.
	 */
	private ConcurrentStateManagerIF<STATE, TRANSITION> stateManager;

	public ConcurrentNodeFactory(
			ConcurrentStateManagerIF<STATE, TRANSITION> stateManager) {
		this.stateManager = stateManager;
	}

	/**
	 * Implements the fly-weight pattern.
	 * 
	 * @param state
	 * @return the existing {@link SequentialNode} mapped to the state, or a new
	 *         {@link SequentialNode} mapped to the state if there was no entry
	 *         in the map for the key state. Note that the
	 *         {@link SequentialNode} will always store the normalized or
	 *         simplified version of {@code state}.
	 */
	public ConcurrentNode<STATE> getNode(STATE state) {
		ConcurrentNode<STATE> result = nodeMap.get(state);

		if (result == null) {
			STATE normalizedState = stateManager.normalize(state);

			if (normalizedState != state) {
				result = nodeMap.get(normalizedState);
				if (result == null) {
					result = new ConcurrentNode<STATE>(normalizedState);
					nodeMap.put(normalizedState, result);
				}
			} else {
				result = new ConcurrentNode<STATE>(state);
			}
			nodeMap.put(state, result);
		}
		return result;
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
			ConcurrentNode<STATE> node, Collection<TRANSITION> transitions,
			boolean full) {
		return new StackEntry<>(node, transitions, full);
	}

	/**
	 * @return the number of search nodes saved.
	 */
	public int numOfSearchNodeSaved() {
		return nodeMap.size();
	}
}
