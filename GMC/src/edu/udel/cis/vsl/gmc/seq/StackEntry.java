package edu.udel.cis.vsl.gmc.seq;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The stack entry that is going to be pushed onto the stack during the search.
 * The stack entry also serves as the iterator of the ample set or ample set
 * complement of the source {@code state}.
 * 
 * @author Yihao Yan (yanyihao)
 *
 */
public class StackEntry<STATE, TRANSITION> implements Iterator<TRANSITION> {
	/**
	 * The search node that wraps the source state with its search information
	 * like stack position or fullyExpanded flag.
	 */
	private SequentialNode<STATE> node;

	/**
	 * This collection could be the ample set or ample set compliment of the
	 * source state.
	 */
	private Collection<TRANSITION> transitions;

	/**
	 * The iterator to iterate either the ample set or the ample set complement
	 * of the {@link #sourceState}. This iterator will iterate over all the
	 * transitions after {@link #current} transition.
	 */
	private Iterator<TRANSITION> transitionIterator;

	/**
	 * The index of the current transition. This is used to write the trace file
	 * which will be used later for replay.
	 */
	private int tid = -1;

	/**
	 * The current transition.
	 */
	private TRANSITION current = null;

	/**
	 * If a successor is on stack, then it will have an index on the stack. This
	 * variable will store the minimum value among all the successors.
	 */
	private int minimumSuccessorStackIndex = Integer.MAX_VALUE;

	/**
	 * @param node
	 *            The node that wraps the source state.
	 * @param transitions
	 *            The ample set or ample set complement of the source state.
	 * @param offset
	 *            Offset should be the start index of transitions in
	 *            {@link #transitions}. When you construct a stack entry for the
	 *            candidate ample set of a state, offset should be 0, while when
	 *            you construct a stack entry for the candidate ample set
	 *            complement of a state, offset should be the size of candidate
	 *            ample set of the same state.
	 */
	public StackEntry(SequentialNode<STATE> node,
			Collection<TRANSITION> transitions, int offset) {
		this.node = node;
		this.transitions = transitions;
		this.transitionIterator = transitions.iterator();
		if (transitionIterator.hasNext()) {
			this.current = transitionIterator.next();
			tid = offset;
		}
	}

	/**
	 * @return the current transition but not move the
	 *         {@link #transitionIterator}.
	 */
	public TRANSITION peek() {
		if (current == null)
			throw new NoSuchElementException();
		return current;
	}

	/**
	 * @return the current transition and also move the
	 *         {@link #transitionIterator}.
	 */
	@Override
	public TRANSITION next() {
		TRANSITION result = current;

		if (result != null) {
			if (transitionIterator.hasNext()) {
				current = transitionIterator.next();
				tid++;
			} else
				current = null;
		} else
			throw new NoSuchElementException();
		return result;
	}

	public int getTid() {
		return tid;
	}

	public SequentialNode<STATE> getNode() {
		return node;
	}

	public Iterator<TRANSITION> getTransitionIterator() {
		return transitionIterator;
	}

	public STATE source() {
		return node.getState();
	}

	@Override
	public boolean hasNext() {
		return current != null;
	}

	public STATE getState() {
		return node.getState();
	}

	public int getMinimumSuccessorStackIndex() {
		return minimumSuccessorStackIndex;
	}

	public void setMinimumSuccessorStackIndex(int minimumSuccessorStackIndex) {
		this.minimumSuccessorStackIndex = minimumSuccessorStackIndex;
	}

	public Collection<TRANSITION> getTransitions() {
		return transitions;
	}
}
