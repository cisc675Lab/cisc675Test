package edu.udel.cis.vsl.gmc.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;

import edu.udel.cis.vsl.gmc.StatePredicateIF;
import edu.udel.cis.vsl.gmc.seq.EnablerIF;
import edu.udel.cis.vsl.gmc.util.Utils;

/**
 * <p>
 * This ConcurrentDfsSearcher is implemented based on Alfons Laarman's algorithm
 * in paper "Partial-Order Reduction for Multi-Core LTL Model Checking", but
 * there are improvements being made. Rather than a totally concurrent
 * depth-first search, it may be more appropriate to say that the algorithm
 * consists of multiple sequential depth-first search that are synchronized to
 * some extent.
 * </p>
 * <p>
 * The basic idea is using multiple threads to search through the state space,
 * each of them starts a depth-first search with its own stack. And try to make
 * them search different parts of the state space.
 * </p>
 * 
 * @author Yihao Yan (yihaoyan)
 *
 * @param <STATE>
 * @param <TRANSITION>
 */
public class ConcurrentDfsSearcher<STATE, TRANSITION> {

	static ConcurrentLinkedQueue<Integer> threadIds = new ConcurrentLinkedQueue<Integer>();

	static {
		int bound = 2 * Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < bound; i++) {
			threadIds.add(i);
		}
	}

	// private AtomicInteger counter = new AtomicInteger(0);

	/**
	 * The # of threads which can be used in the concurrent searcher.
	 */
	private int N;

	/**
	 * True iff a state in which the predicate holds is found.
	 */
	private boolean predicateHold;

	private boolean violationFound = false;

	private RuntimeException exception = null;

	/**
	 * Thread pool to manage the threads.
	 */
	private MyThreadPool pool;

	/**
	 * A ConcurrentEnablerIF used to compute ampleSet, ampleSetComplement and
	 * allEnabledTransitions of a STATE.
	 */
	private EnablerIF<STATE, TRANSITION> enabler;

	/**
	 * The state manager, used to determine the next state, given a state and
	 * transition. Also used for other state management issues.
	 */
	private ConcurrentStateManagerIF<STATE, TRANSITION> manager;

	/**
	 * The predicate on states. This searcher is searching for state that
	 * satisfies this predicate. Typically, this predicate describes something
	 * "bad", like deadlock.
	 */
	private StatePredicateIF<STATE> predicate;

	/**
	 * If true, a cycle in the state space is reported as a violation.
	 */
	private boolean reportCycleAsViolation = false;

	/**
	 * If this searcher stopped because a cycle was found, this flag will be set
	 * to true, else it is false.
	 */
	private boolean cycleFound = false;

	/**
	 * The number of transitions executed since the beginning of the search.
	 */
	private int totalNumTransitions = 0;

	/**
	 * The number of states encountered which are recognized as having already
	 * been seen earlier in the search.
	 */
	private int totalNumStatesMatched = 0;

	private Object totalNumStatesMatchedLock = new Object();

	private Object totalNumTransitionsLock = new Object();

	/**
	 * The number of states seen in this search.
	 */
	private int totalNumStatesSeen = 1;

	/**
	 * A name to give this searcher, used only for printing out messages about
	 * the search, such as in debugging.
	 */
	private String name = null;

	/**
	 * Are we searching for a minimal counterexample?
	 */
	private boolean minimize = false;

	private ConcurrentNodeFactory<STATE, TRANSITION> concurrentNodeFactory = new ConcurrentNodeFactory<>(
			manager);

	public ConcurrentDfsSearcher(EnablerIF<STATE, TRANSITION> enabler,
			ConcurrentStateManagerIF<STATE, TRANSITION> manager,
			StatePredicateIF<STATE> predicate, int N) {

		if (enabler == null) {
			throw new NullPointerException("null enabler");
		}
		if (manager == null) {
			throw new NullPointerException("null manager");
		}
		this.enabler = enabler;
		this.manager = manager;
		this.predicate = predicate;
		this.N = N;
		this.predicateHold = false;
		this.pool = new MyThreadPool(N);
	}

	public StatePredicateIF<STATE> predicate() {
		return predicate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public void setMinimize(boolean value) {
		this.minimize = value;
	}

	public boolean getMinimize() {
		return minimize;
	}

	public boolean reportCycleAsViolation() {
		return this.reportCycleAsViolation;
	}

	/**
	 * If you want to check for cycles in the state space, and report the
	 * existence of a cycle as a violation, this flag should be set to true.
	 * Else set it to false. By default, it is false.
	 */
	public void setReportCycleAsViolation(boolean value) {
		this.reportCycleAsViolation = value;
	}

	public boolean cycleFound() {
		return cycleFound;
	}

	/**
	 * The number of states seen in this search.
	 * 
	 * @return the number of states seen so far
	 */
	public int totalNumStatesSeen() {
		return totalNumStatesSeen;
	}

	/**
	 * The number of transitions executed in the course of this search so far.
	 * 
	 * @return the number of transitions executed.
	 */
	public int totalNumTransitions() {
		return totalNumTransitions;
	}

	/**
	 * The number of states matched so far. A state is "matched" when the search
	 * determines the state has been seen before, earlier in the search. If the
	 * state has been seen before, it is not explored.
	 * 
	 * @return the number of states matched
	 */
	public int totalNumStatesMatched() {
		return totalNumStatesMatched;
	}

	/**
	 * Start a concurrent dfs task from a given state
	 * 
	 * @param initialState
	 *            The state the search starts from.
	 * @throws Exception
	 */
	public boolean search(STATE initialState) {
		if (predicate.holdsAt(initialState)) {
			predicateHold = true;
			return true;
		}
		SequentialDfsSearchTask task = new SequentialDfsSearchTask(initialState,
				generateThreadId(), null, null);
		pool.submit(task);
		while (!pool.isQuiescent());
		pool.shutdown();

		if (exception != null)
			throw exception;
		return predicateHold;
	}

	private class SequentialDfsSearchTask extends ForkJoinTask<Integer> {

		private static final long serialVersionUID = -2011438813013648270L;

		private AtomicInteger parentCounter = null;

		private int id;

		/**
		 * Each thread will have its own stack and elements in the stack are
		 * {@link TransitionIterator}.
		 */
		private Stack<StackEntry<STATE, TRANSITION>> stack;

		/**
		 * The number of transitions executed since the beginning of the search.
		 */
		private int numTransitions = 0;

		/**
		 * The number of states encountered which are recognized as having
		 * already been seen earlier in the search.
		 */
		private int numStatesMatched = 0;

		/**
		 * The number of states seen in this search.
		 */
		// private int numStatesSeen = 1;

		/**
		 * Upper bound on stack depth.
		 */
		// private int depthBound = Integer.MAX_VALUE;

		/**
		 * Place an upper bound on stack size (depth).
		 */
		// private boolean stackIsBounded = false;

		/**
		 * Are we searching for a minimal counterexample?
		 */
		// private boolean minimize = false;

		private SequentialDfsSearchTask parent;

		private LinkedList<STATE> parentStack = new LinkedList<>();;

		public SequentialDfsSearchTask(STATE initState, int id,
				SequentialDfsSearchTask parent, AtomicInteger counter) {
			Collection<TRANSITION> ampleSet = enabler.ampleSet(initState);
			ConcurrentNode<STATE> initNode = concurrentNodeFactory
					.getNode(initState);
			StackEntry<STATE, TRANSITION> initEntry = concurrentNodeFactory
					.newStackEntry(initNode, ampleSet, false);

			initState = initNode.getState();
			this.id = id;
			this.parentCounter = counter;
			this.stack = new Stack<>();
			this.stack.push(initEntry);
			initNode.setOnStack(id, true);
			this.parent = parent;
			copyParentStack(parent);
		}

		private void copyParentStack(SequentialDfsSearchTask parent) {
			if (parent != null) {
				Stack<StackEntry<STATE, TRANSITION>> parentStack = parent
						.stack();
				Enumeration<StackEntry<STATE, TRANSITION>> elements = parentStack
						.elements();

				while (elements.hasMoreElements()) {
					ConcurrentNode<STATE> node = elements.nextElement()
							.getNode();

					this.parentStack.add(node.getState());
					node.setOnStack(id, true);
				}
			}
		}

		// TODO has problem, parent may have a different stack.
		private void removeParentStack(SequentialDfsSearchTask parent) {
			for (STATE s : parentStack) {
				concurrentNodeFactory.getNode(s).setOnStack(id, false);
			}
		}

		// public StatePredicateIF<STATE> predicate() {
		// return predicate;
		// }

		// public boolean isDepthBounded() {
		// return stackIsBounded;
		// }

		// public void unboundDepth() {
		// this.stackIsBounded = false;
		// depthBound = Integer.MAX_VALUE;
		// }
		//
		// public void boundDepth(int value) {
		// depthBound = value;
		// stackIsBounded = true;
		// }
		//
		// public void restrictDepth() {
		// depthBound = stack.size() - 1;
		// stackIsBounded = true;
		// }

		// public void setMinimize(boolean value) {
		// this.minimize = value;
		// }
		//
		// public boolean getMinimize() {
		// return minimize;
		// }

		public Stack<StackEntry<STATE, TRANSITION>> stack() {
			return stack;
		}

		@Override
		public Integer getRawResult() {
			return null;
		}

		@Override
		protected void setRawResult(Integer value) {
		}

		private void notifyParent() {
			System.out.println("try to notify parent");
			if (parentCounter != null) {
				synchronized (parentCounter) {
					parentCounter.decrementAndGet();
					parentCounter.notify();
					System.out.println(
							this.id + " notify its parent" + this.parent.id);
				}
			}
		}

		private boolean checkTerminationCondition() {
			if (cycleFound || predicateHold || violationFound
					|| exception != null) {
				notifyParent();
				this.stack.clear();
				threadIds.add(id);

				synchronized (totalNumStatesMatchedLock) {
					totalNumStatesMatched += numStatesMatched;
				}
				synchronized (totalNumTransitionsLock) {
					totalNumTransitions += numTransitions;
				}
				return true;
			} else {
				return false;
			}
		}

		private void gatherStatistics() {
			synchronized (totalNumStatesMatchedLock) {
				totalNumStatesMatched += numStatesMatched;
			}
			synchronized (totalNumTransitionsLock) {
				totalNumTransitions += numTransitions;
			}
		}

		@Override
		protected boolean exec() {
			try {
				while (!stack.empty()) {
					if (checkTerminationCondition())
						return true;

					StackEntry<STATE, TRANSITION> currentStackEntry = stack
							.peek();
					ConcurrentNode<STATE> currentNode = currentStackEntry
							.getNode();
					STATE currentState = currentNode.getState();
					TRANSITION transition = null;
					boolean continueDFS = false;

					while (currentStackEntry.hasNext()) {
						if (checkTerminationCondition())
							return true;
						transition = currentStackEntry.next();

						STATE newState = null;
						ConcurrentNode<STATE> newNode = concurrentNodeFactory
								.getNode(newState);

						newState = manager.nextState(currentState, transition)
								.getFinalState();
						numTransitions++;

						if (checkPredicate(newState))
							return true;

						if (!newNode.onStack(id)) {
							currentStackEntry.setExpand(false);
							if (!newNode.fullyExplored()) {
								// trying to spawn new thread
								if (currentStackEntry.hasNext()) {
									if (spawnChildrenThread(currentStackEntry,
											newState))
										continue;
								}
								Collection<TRANSITION> newTransitionSet = enabler
										.ampleSet(newState);
								StackEntry<STATE, TRANSITION> newStackEntry = concurrentNodeFactory
										.newStackEntry(newNode,
												randomizeCollection(
														newTransitionSet),
												false);

								this.stack.push(newStackEntry);
								newNode.setOnStack(id, true);
								continueDFS = true;
								break;
							} else
								numStatesMatched++;
						} else {
							numStatesMatched++;
							if (reportCycleAsViolation) {
								cycleFound = true;
								cleanBeforeTermination();
								return true;
							}
						}
					}
					if (continueDFS) {
						continue;
					}
					if (checkStackProviso(currentStackEntry, currentState,
							currentStackEntry.getExpand())) {
						continue;
					}
					waitforChildren(
							currentStackEntry.getChildrenThreadsCounter());
					currentNode.setFullyExplored(true);
					this.stack.pop();
					currentNode.setOnStack(id, false);
				}
				System.out.println(this.id + "ends");
				cleanBeforeTermination();
			} catch (RuntimeException e) {
				exception = e;
				e.printStackTrace();
				violationFound = true;
				cleanBeforeTermination();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}

		private boolean spawnChildrenThread(
				StackEntry<STATE, TRANSITION> stackEntry, STATE newState) {
			try {
				synchronized (pool) {
					if (pool.getActiveNum() < N && pool.getRunningNum() < pool
							.getMaxNumOfThread()) {
						System.out.println(
								"total num threads:" + pool.getRunningNum());
						int newId = generateThreadId();
						SequentialDfsSearchTask newTask = new SequentialDfsSearchTask(
								newState, newId, this,
								stackEntry.getChildrenThreadsCounter());

						System.out.println(this.id + " spawned " + newId);
						stackEntry.incrementCounter();
						pool.submit(newTask);
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * @param state
		 * @return true iff find a state that satisfies the predicate.
		 */
		private boolean checkPredicate(STATE state) {
			boolean result = predicate.holdsAt(state);

			if (result) {
				predicateHold = true;
				cleanBeforeTermination();
				System.out
						.println("found a state that satisifies the predicate");
			}
			return result;
		}

		/**
		 * before a thread terminate, notify its parent, put back the id, and
		 * gather statistics, clean those states that are on the stack of this
		 * thread because of its parent.
		 */
		private void cleanBeforeTermination() {
			System.out.println("clean");
			try {
				removeParentStack(parent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			notifyParent();
			threadIds.add(id);
			pool.decrementTotal();
			gatherStatistics();
		}

		private void waitforChildren(AtomicInteger counter) {
			if (counter != null) {
				synchronized (counter) {
					while (counter.intValue() > 0) {
						try {
							pool.incrementWaiting();
							System.out.println(this.id + " is waiting..."
									+ "counter: " + counter.intValue());
							counter.wait();
							System.out.println(this.id + " is done waiting...");
							pool.decrementWaiting();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		private boolean checkStackProviso(
				StackEntry<STATE, TRANSITION> stackEntry, STATE currentState,
				boolean allOnStack) {
			ConcurrentNode<STATE> node = stackEntry.getNode();
			STATE state = node.getState();

			if (node.setStackProvisoCAS(
					(allOnStack ? ProvisoValue.TRUE : ProvisoValue.FALSE))) {
				if (node.getProviso() == ProvisoValue.TRUE) {
					Collection<TRANSITION> fullSet = enabler.fullSet(state);
					@SuppressWarnings("unchecked")
					Collection<TRANSITION> ac = (Collection<TRANSITION>) Utils
							.subtract(fullSet, stackEntry.getTransitions());
					StackEntry<STATE, TRANSITION> newStackEntry = concurrentNodeFactory
							.newStackEntry(node, ac, true);

					stack.pop();
					stack.push(newStackEntry);
					return true;
				}
			}
			return false;
		}
	}

	private int generateThreadId() {
		return threadIds.poll();
	}

	private Collection<TRANSITION> randomizeCollection(
			Collection<TRANSITION> collection) {
		LinkedList<TRANSITION> transitions = new LinkedList<>();

		for (TRANSITION t : collection)
			transitions.add(t);
		Collections.shuffle(transitions);
		return transitions;
	}
}
