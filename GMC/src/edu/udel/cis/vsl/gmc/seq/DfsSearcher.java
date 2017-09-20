package edu.udel.cis.vsl.gmc.seq;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Stack;

import edu.udel.cis.vsl.gmc.GMCConfiguration;
import edu.udel.cis.vsl.gmc.StatePredicateIF;
import edu.udel.cis.vsl.gmc.TraceStepIF;
import edu.udel.cis.vsl.gmc.util.Utils;

/**
 * A DfsSearcher performs a depth-first search of the state space of a
 * transition system, stopping immediately if it finds a state satisfying the
 * given predicate. A DfsSearcher is instantiated with a given enabler (an
 * object which tells what transitions to explore from a given state), a state
 * manager, a predicate, and a state from which to start the search.
 * 
 * <p>
 * Note that the STATE that this searcher use MUST override
 * {@link Object#hashCode()} and {@link Object #equals(Object)} methods and
 * STATEs that are canonically the same should return have the same hashcode and
 * they should be equal. Also the transition that is used by this searcher also
 * needs to implement {@link Object#equals(Object)}.
 * 
 * 
 * @author Stephen F. Siegel, University of Delaware
 * @author Yihao Yan (yanyihao)
 */
public class DfsSearcher<STATE, TRANSITION> {

	/**
	 * The enabler, used to determine the set of enabled transitions at any
	 * state, among other things.
	 */
	private EnablerIF<STATE, TRANSITION> enabler;

	/**
	 * The state manager, used to determine the next state, given a state and
	 * transition. Also used for other state management issues.
	 */
	private StateManager<STATE, TRANSITION> manager;

	/**
	 * The predicate on states. This searching is searching for state that
	 * satisfies this predicate. Typically, this predicate describes something
	 * "bad", like deadlock.
	 */
	private StatePredicateIF<STATE> predicate;

	/**
	 * The depth-first search stack. An element in this stack in a transition
	 * sequence, which encapsulates a state together with the transitions
	 * enabled at that state which have not yet been completely explored.
	 */
	private Stack<StackEntry<STATE, TRANSITION>> stack;

	/**
	 * This factory is used to get or construct some objects used in the search.
	 * For example, it is used to get the associated {@link SequentialNode} of a
	 * {@code state} and construct new instances of {@link StackEntry}.
	 */
	private SequentialNodeFactory<STATE, TRANSITION> sequentialNodeFactory;

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
	private int numTransitions = 0;

	/**
	 * The number of states encountered which are recognized as having already
	 * been seen earlier in the search.
	 */
	private int numStatesMatched = 0;

	/**
	 * The number of states seen in this search.
	 */
	private int numStatesSeen = 1;

	/**
	 * Where to print debugging output, if debugging is turned on.
	 */
	private PrintStream debugOut;

	/**
	 * Should we print debugging output?
	 */
	private boolean debugging = false;

	/**
	 * A name to give this searcher, used only for printing out messages about
	 * the search, such as in debugging.
	 */
	private String name = null;

	/**
	 * When the stack is being summarized in debugging output, this is the upper
	 * bound on the number of stack entries (starting from the top and moving
	 * down) that will be printed.
	 */
	private int summaryCutOff = 5;

	/**
	 * Upper bound on stack depth.
	 */
	private int depthBound = Integer.MAX_VALUE;

	/**
	 * Place an upper bound on stack size (depth).
	 */
	private boolean stackIsBounded = false;

	/**
	 * Are we searching for a minimal counterexample?
	 */
	private boolean minimize = false;

	boolean printTransitions = false;

	/**
	 * Constructs a new depth first search searcher.
	 * 
	 * @param enabler
	 *            the enabler used to determine the set of enabled transitions
	 *            at each state in the course of this search
	 * @param manager
	 *            the object used to manage states, compute the next state from
	 *            a current state and transition, and so, during this search
	 * @param predicate
	 *            the state predicate -- this will be checked at each state
	 *            encountered in the search, and if it is found to hold, the
	 *            search method will return; hence it is usually a predicate
	 *            about something "bad" happening, like a deadlock
	 * @param debugOut
	 *            if null, deubgging output is not printed, otherwise debugging
	 *            output will be printing to this stream
	 */
	public DfsSearcher(EnablerIF<STATE, TRANSITION> enabler,
			StateManager<STATE, TRANSITION> manager,
			StatePredicateIF<STATE> predicate, GMCConfiguration gmcConfig,
			PrintStream debugOut) {

		if (enabler == null) {
			throw new NullPointerException("null enabler");
		}
		if (manager == null) {
			throw new NullPointerException("null manager");
		}
		this.enabler = enabler;
		this.manager = manager;
		this.predicate = predicate;
		this.debugOut = debugOut;
		this.sequentialNodeFactory = new SequentialNodeFactory<>(manager,
				gmcConfig.getSaveStates());
		this.manager.setSequentialNodeFactory(sequentialNodeFactory);
		if (debugOut != null) {
			this.debugging = true;
		}
		stack = new Stack<>();
		this.printTransitions = gmcConfig.printTransitions();
	}

	public StatePredicateIF<STATE> predicate() {
		return predicate;
	}

	public DfsSearcher(EnablerIF<STATE, TRANSITION> enabler,
			StateManager<STATE, TRANSITION> manager,
			StatePredicateIF<STATE> predicate, GMCConfiguration gmcConfig) {
		this(enabler, manager, predicate, gmcConfig, null);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public boolean isDepthBounded() {
		return stackIsBounded;
	}

	public void unboundDepth() {
		this.stackIsBounded = false;
		depthBound = Integer.MAX_VALUE;
	}

	public void boundDepth(int value) {
		depthBound = value;
		stackIsBounded = true;
	}

	/**
	 * Sets the depth bound to one less than the current stack size. Also sets
	 * the "stackIsBounded" bit to true.
	 */
	public void restrictDepth() {
		depthBound = stack.size() - 1;
		stackIsBounded = true;
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

	/**
	 * If reportCycleAsViolation is true, and the search terminates with a
	 * "true" value, then this method can be called to determine whether the
	 * predicate holds (indicating a standard property violation) or a cycle has
	 * been found.
	 */
	public boolean cycleFound() {
		return cycleFound;
	}

	/**
	 * Returns the state at the top of the stack, without modifying the stack.
	 */
	public STATE currentState() {
		if (stack.isEmpty()) {
			return null;
		} else {
			return stack.peek().getState();
		}
	}

	/** Returns the stack used to perform the depth first search */
	public Stack<StackEntry<STATE, TRANSITION>> stack() {
		return stack;
	}

	/**
	 * Performs a depth-first search starting from the given state. Essentially,
	 * this pushes the given state onto the stack, making it the current state,
	 * and then invokes search().
	 * 
	 * @return true if a state is found that satisfies the predicate, in which
	 *         case method {@link #currentState} can be used to get the state.
	 *         If false is returned, the search has completed without finding a
	 *         state satisfying the predicate.
	 */
	public boolean search(STATE initialState) {
		SequentialNode<STATE> initialNode = sequentialNodeFactory
				.getInitialNode(initialState);

		initialState = initialNode.getState();
		if (minimize)
			initialNode.setDepth(0);

		StackEntry<STATE, TRANSITION> stackEntry = sequentialNodeFactory
				.newStackEntry(initialNode, enabler.ampleSet(initialState), 0);

		stack.push(stackEntry);
		initialNode.setSeen(true);
		initialNode.setStackPosition(stack.size() - 1);
		if (debugging) {
			debugOut.println("Pushed initial state onto stack " + name + ":\n");
			manager.printStateLong(debugOut, initialState);
			debugOut.println();
			debugOut.flush();
		}
		return search();
	}

	/**
	 * Resumes a depth-first search of the state space starting from the current
	 * state. The set of seen states and the stack may be non-empty, as this
	 * method is typically used to resume a search that has been "paused". It
	 * can also be used to start a search, as long as the current state has been
	 * set.
	 * 
	 * Returns true iff predicate holds at some state reachable from the current
	 * state, including the current state. If this is the case, this will return
	 * true when the first state satisfying predicate is found in search. Once
	 * true is returned you may print the stack or look at the current state
	 * before resuming the search again. If false is returned, the search has
	 * completed without finding a state satisfying the predicate.
	 * 
	 * If reportCycleAsViolation is true, this will also terminate and return
	 * true if a cycle in the state space has been found. The final state in the
	 * trace will also be the one which occurs earlier in the trace, forming a
	 * cycle.
	 * 
	 * @return true if state is found which satisfies predicate. false if search
	 *         completes without finding such a state.
	 */
	public boolean search() {
		while (!predicate.holdsAt(currentState())) {
			debug("Predicate does not hold at current state of " + name + ".");
			if (!proceedToNewState()) {
				if (cycleFound) {
					debug("Cycle found in state space.");
					return true;
				}
				debug("Search complete: predicate " + predicate
						+ " does not hold at " + "any reachable state of "
						+ name + ".\n");
				return false;
			}
		}
		debug("Predicate " + predicate + " holds at current state of " + name
				+ ": terminating search.\n");
		return true;
	}

	/**
	 * <p>
	 * Proceeds with the search until we arrive at a state that has not been
	 * seen before (assuming there is one). In this case it marks the new state
	 * as seen, pushes it on the stack, and marks it as on the stack, and then
	 * returns true. If it finishes searching without finding a new state, it
	 * returns false.
	 * </p>
	 * <p>
	 * The search proceeds in the depth-first manner. The last transition
	 * sequence is obtained from the stack; these are the enabled transitions
	 * departing from the current state. The first transition in this sequence
	 * is applied to the current state. If the resulting state has not been seen
	 * before, we are done. Otherwise, the next transition is tried, and so on.
	 * If all these transitions are exhausted we proceed as follows: if the
	 * stack is empty, the search has completed and false is returned.
	 * Otherwise, the stack is popped, and the list of remaining transitions to
	 * be explored for the new current state is used, and we proceed as before.
	 * If this is again exhausted, we pop and repeat.
	 * </p>
	 * 
	 * @return true if there is a new state; false if the search is over so
	 *         there is no new state
	 */
	public boolean proceedToNewState() {
		while (!stack.isEmpty()) {
			StackEntry<STATE, TRANSITION> currentStackEntry = stack.peek();
			SequentialNode<STATE> currentSequentialNode = currentStackEntry
					.getNode();
			STATE currentState = currentStackEntry.getState();

			while (!stackOutOfBound() && currentStackEntry.hasNext()) {
				TRANSITION transition = currentStackEntry.peek();
				TraceStepIF<STATE> traceStep = manager.nextState(currentState,
						transition);

				// Let manager print the trace step (exclude final state):
				manager.printTraceStep(currentState, traceStep);

				STATE newState = traceStep.getFinalState();
				SequentialNode<STATE> newSequentialNode = sequentialNodeFactory
						.getNode(traceStep);

				newState = newSequentialNode.getState();
				// Let manager print the final state of the trace step:
				manager.printTraceStepFinalState(newState,
						newSequentialNode.getId());

				int newStateStackIndex = newSequentialNode.getStackPosition();
				if (currentSequentialNode.getFullyExpanded()
						|| (newStateStackIndex == -1)) {
					currentSequentialNode.setExpand(false);
				} else {
					updateMinimumStackIndex(currentStackEntry,
							newStateStackIndex);
				}
				numTransitions++;
				if (!newSequentialNode.getSeen() || (minimize
						&& stack.size() < newSequentialNode.getDepth())) {
					if (newSequentialNode.getSeen()) {
						resetState(newSequentialNode);
					}

					assert newSequentialNode.getStackPosition() == -1;
					if (debugging) {
						debugOut.println("New state of " + name + " is "
								+ newState + ":");
						debugOut.println();
						manager.printStateLong(debugOut, newState);
						debugOut.println();
						debugOut.flush();
					}
					if (minimize)
						newSequentialNode.setDepth(stack.size());

					Collection<TRANSITION> newSet = enabler.ampleSet(newState);
					// assume a state must be fully expanded until proven
					// otherwise.
					if (newSet.iterator().hasNext())
						newSequentialNode.setExpand(true);
					else
						newSequentialNode.setExpand(false);
					stack.push(sequentialNodeFactory
							.newStackEntry(newSequentialNode, newSet, 0));
					newSequentialNode.setSeen(true);
					newSequentialNode.setStackPosition(stack.size() - 1);
					numStatesSeen++;
					debugPrintStack("Pushed " + newState + " onto the stack "
							+ name + ". ", false);
					return true;
				}
				debug(newState + " seen before!  Moving to next transition.");
				if (reportCycleAsViolation
						&& newSequentialNode.getStackPosition() >= 0) {
					cycleFound = true;
					return false;
				}
				numStatesMatched++;
				currentStackEntry.next();
			}

			// if there is a bound of the state and the stack depth is out of
			// bound, don't try to expand the state.
			if (!stackOutOfBound() && !currentSequentialNode.getFullyExpanded()
					&& currentSequentialNode.getExpand()
					&& checkStackTrace(currentStackEntry)) {
				Collection<TRANSITION> ampleSet = currentStackEntry
						.getTransitions();
				Collection<TRANSITION> fullSet = enabler.fullSet(currentState);
				@SuppressWarnings("unchecked")
				Collection<TRANSITION> ampleSetComplement = (Collection<TRANSITION>) Utils
						.subtract(fullSet, ampleSet);

				stack.pop();
				stack.push(sequentialNodeFactory.newStackEntry(
						currentSequentialNode, ampleSetComplement,
						ampleSet.size()));
				currentSequentialNode.setFullyExpanded(true);
				continue;
			}
			stack.pop();
			currentSequentialNode.setStackPosition(-1);
			if (!stack.isEmpty())
				stack.peek().next();
			debugPrintStack("Popped stack.", false);
		}
		return false;
	}

	/**
	 * Reset the fullyExpanded field of a state to false, and the expand field
	 * to true.
	 * 
	 * @param state
	 *            The state that will be reset.
	 */
	private void resetState(SequentialNode<STATE> node) {
		node.setExpand(true);
		node.setFullyExpanded(false);
	}

	/**
	 * @return true iff the stack is out of bound.
	 */
	private boolean stackOutOfBound() {
		return stackIsBounded && stack.size() >= depthBound;
	}

	/**
	 * Update the minimum successor index of the iterator.
	 * 
	 * @param iterator
	 *            The iterator that will be updated.
	 * @param index
	 *            The possible smaller successor stack index.
	 */
	private void updateMinimumStackIndex(
			StackEntry<STATE, TRANSITION> stackEntry, int index) {
		stackEntry.setMinimumSuccessorStackIndex(
				Math.min(stackEntry.getMinimumSuccessorStackIndex(), index));
	}

	/**
	 * @return false iff there exist a state on the trace that has already been
	 *         fully expanded.
	 */
	public boolean checkStackTrace(StackEntry<STATE, TRANSITION> stackEntry) {
		int stackIndex = stackEntry.getMinimumSuccessorStackIndex();
		int size = stack.size();

		assert stackIndex != Integer.MAX_VALUE;

		for (int i = stackIndex; i < size - 1; i++) {
			if (stack.get(i).getNode().getFullyExpanded())
				return false;
		}
		return true;
	}

	/**
	 * Set the debugging flag to the given value. If true, debugging output will
	 * be printed to the debug stream. Otherwise debugging output will not be
	 * printed.
	 * 
	 * @param value
	 *            if true, start showing the debugging output, otherwise don't
	 *            show it
	 */
	public void setDebugging(boolean value) {
		debugging = value;
	}

	/**
	 * Returns the current value of the debugging flag. If true, debugging
	 * output will be printed to the debug stream. Otherwise debugging output
	 * will not be printed.
	 * 
	 * @return the current value of the debugging flag
	 */
	boolean debugging() {
		return debugging;
	}

	/**
	 * Sets the debugging output stream to the given stream. This is the stream
	 * used to print debugging information if the debugging flag is on.
	 * 
	 * @param out
	 *            the output stream to which debugging information should be
	 *            sent
	 */
	public void setDebugOut(PrintStream out) {
		if (out == null) {
			throw new NullPointerException("null out");
		}
		debugOut = out;
	}

	/**
	 * Returns the stream used to print debugging output when the debugging flag
	 * is on.
	 * 
	 * @return the debugging output stream
	 */
	public PrintStream getDebugOut() {
		return debugOut;
	}

	/**
	 * If the debugging flag is on, prints the message s to the debugging output
	 * stream, otherwise does nothing.
	 * 
	 * @param s
	 *            a debugging message
	 */
	protected void debug(String s) {
		if (debugging) {
			debugOut.println(s);
			debugOut.flush();
		}
	}

	/**
	 * Prints the current stack in a human-readable format.
	 * 
	 * @param out
	 *            the stream to which to print the stack
	 * @param longFormat
	 *            if true, provide detailed information about each state
	 * @param summarize
	 *            if true, don't print out more than some fixed bound number of
	 *            entries from the top of the stack; otherwise print the whole
	 *            stack
	 */
	public void printStack(PrintStream out, boolean longFormat,
			boolean summarize) {
		int size = stack.size();

		if (size == 0) {
			out.println("  <EMPTY>");
		}
		for (int i = 0; i < size; i++) {
			StackEntry<STATE, TRANSITION> stackEntry = stack.elementAt(i);
			STATE state = stackEntry.getState();

			if (!summarize || i <= 1 || size - i < summaryCutOff - 1) {
				if (i > 0) {
					out.print(" -> ");
					manager.printStateShort(out, state);
					out.println();
				}
				if (longFormat) {
					out.println();
					manager.printStateLong(out, state);
					out.println();
				}
			}
			if (summarize && size - i == summaryCutOff - 1) {
				for (int j = 0; j < 3; j++)
					out.println("     .");
			}
			if (!summarize || i <= 0 || size - i < summaryCutOff) {
				out.print("Step " + (i + 1) + ": ");
				manager.printStateShort(out, state);
				if (stackEntry.hasNext()) {
					out.print(" --");
					manager.printTransitionShort(out, stackEntry.peek());
				}
				out.flush();
			}
		}
		out.println();
		out.flush();
	}

	/**
	 * Prints the whole stack in a human readable form to the given stream.
	 * Prints first a summary, then the stack in full detail (with detailed
	 * state information).
	 * 
	 * @param out
	 *            output stream to which this information should be sent
	 */
	public void printStack(PrintStream out) {
		if (name != null)
			out.print(name + " ");
		out.println("Trace summary:\n");
		printStack(out, false, false);
		out.println();
		if (name != null)
			out.print(name + " ");
		out.println("Trace details:");
		printStack(out, true, false);
	}

	/**
	 * Prints the stack, summarizing, i.e., only printing out the first few
	 * entries from the top.
	 * 
	 * @param s
	 *            a message to print at the beginning
	 * @param longFormat
	 *            if true, print complete state information, otherwise use short
	 *            names for the states
	 */
	void debugPrintStack(String s, boolean longFormat) {
		if (debugging) {
			debugOut.println(s + "  New stack for " + name + ":\n");
			printStack(debugOut, longFormat, true);
			debugOut.println();
		}
	}

	/**
	 * If the debugging flag is on, prints out all the states held by the state
	 * manager in their full gory detail. Otherwise, a no-op.
	 * 
	 * @param s
	 *            a message to print first
	 */
	void debugStates(String s) {
		if (debugging) {
			debugOut.println(s + "All states for " + name + ":\n");
			manager.printAllStatesLong(debugOut);
			debugOut.println();
			printSummary(debugOut);
		} else {
		}
	}

	/**
	 * The number of states seen in this search.
	 * 
	 * @return the number of states seen so far
	 */
	public int numStatesSeen() {
		return numStatesSeen;
	}

	/**
	 * The number of transitions executed in the course of this search so far.
	 * 
	 * @return the number of transitions executed.
	 */
	public int numTransitions() {
		return numTransitions;
	}

	/**
	 * The number of states matched so far. A state is "matched" when the search
	 * determines the state has been seen before, earlier in the search. If the
	 * state has been seen before, it is not explored.
	 * 
	 * @return the number of states matched
	 */
	public int numStatesMatched() {
		return numStatesMatched;
	}

	/**
	 * @return the number of search nodes saved which is also the number of
	 *         non-equal states.
	 */
	public int numOfSearchNodeSaved() {
		return sequentialNodeFactory.numOfSearchNodeSaved();
	}

	/**
	 * Summarizes the current state of the search in a human-readable form
	 * printed to the given stream.
	 * 
	 * @param out
	 *            the stream to which to print the information
	 */
	public void printSummary(PrintStream out) {
		out.println("Number of states seen:    " + numStatesSeen);
		out.println("Number of transitions:   " + numTransitions);
		out.println("Number of states matched: " + numStatesMatched + "\n");
		out.flush();
	}

	/**
	 * Write the state of the current stack in a condensed form that can be used
	 * to replay the trace later.
	 * 
	 * @param stream
	 *            stream to which to write the current state of the DFS stack
	 */
	public void writeStack(PrintStream stream) {
		int size = stack.size();
		int prevTid = 0;
		int count = 0;

		stream.println("LENGTH = " + size);
		for (int i = 0; i < size; i++) {
			int curTid = stack.elementAt(i).getTid();

			if (count == 0) {
				count++;
				prevTid = curTid;
			} else {
				if (curTid == prevTid) {
					count++;
				} else {
					stream.println(count + ":" + prevTid);
					count = 1;
					prevTid = curTid;
				}
			}
			if (i == size - 1) {
				stream.println(count + ":" + prevTid);
			}
		}
		stream.flush();
	}
}
