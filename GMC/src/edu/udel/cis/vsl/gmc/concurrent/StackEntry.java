package edu.udel.cis.vsl.gmc.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

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
	private ConcurrentNode<STATE> node;

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
	private int tid;

	/**
	 * The current transition.
	 */
	private TRANSITION current;

	/**
	 * True iff the entry is an entry for ample set complement.
	 */
	private boolean full;

	/**
	 * If a successor is on stack, then it will have an index on the stack. This
	 * variable will store the minimum value among all the successors.
	 */
	private int minimumSuccessorStackIndex = Integer.MAX_VALUE;

	/**
	 * True iff the source state should be expanded.
	 */
	private boolean expand = true;

	private AtomicInteger childrenThreadsCounter = new AtomicInteger(0);

	/**
	 * @param node
	 *            The node that wraps the source state.
	 * @param transitions
	 *            The ample set or ample set complement of the source state.
	 * @param full
	 *            Whether the collection is ample set complement or not.
	 */
	public StackEntry(ConcurrentNode<STATE> node,
			Collection<TRANSITION> transitions, boolean full) {
		this.node = node;
		this.transitions = transitions;
		this.transitionIterator = transitions.iterator();
		this.full = full;
		this.current = transitionIterator.hasNext()
				? transitionIterator.next()
				: null;
		tid = 0;
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
			tid++;
			current = transitionIterator.hasNext()
					? transitionIterator.next()
					: null;
		} else
			throw new NoSuchElementException();
		return result;
	}

	public int getTid() {
		return tid;
	}

	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public ConcurrentNode<STATE> getNode() {
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

	public boolean getExpand() {
		return expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	public AtomicInteger getChildrenThreadsCounter() {
		return childrenThreadsCounter;
	}

	public void incrementCounter() {
		childrenThreadsCounter.incrementAndGet();
	}
}
