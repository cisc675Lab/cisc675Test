package edu.udel.cis.vsl.gmc.seq;

import java.io.PrintStream;

import edu.udel.cis.vsl.gmc.TraceStepIF;

/**
 * A StateManagerIF provides part of a generic interface to a state-transition
 * system. The primary method is {@link #nextState}, which, given a state and a
 * transition, returns the "next state", i.e., the state which results from
 * executing the transition from the given state. Other methods are provided
 * that are needed specifically for depth-first search, including methods to
 * mark a state as "seen before", and to make a state as
 * "currently on (or off) the stack". Still other methods are provided for
 * printing information abou states.
 * 
 * @author Stephen F. Siegel
 * @author Yihao Yan (yanyihao)
 * 
 * @param <STATE>
 *            the type used to represent states in the state-transition system
 *            being analyzed
 * @param <TRANSITION>
 *            the type used to represent transitions in the state-transition
 *            system being analyzed
 */
public abstract class StateManager<STATE, TRANSITION> {
	private SequentialNodeFactory<STATE, TRANSITION> nodeFactory;

	/**
	 * Given a state and a transition, returns the trace step after executing
	 * the transition at the given state. See {@link TraceStepIF}.
	 * 
	 * @param state
	 *            a state in the state transition system
	 * @param transition
	 *            an execution which is enabled at the given state
	 * @return the trace step after executing the transition at the given state.
	 */
	public abstract TraceStepIF<STATE> nextState(STATE state,
			TRANSITION transition);

	/**
	 * <p>
	 * Normalize/simplify a state. It takes a {@link TraceStepIF} as input since
	 * normalize always happens after {@link #nextState(Object, Object)} which
	 * will return a {@link TraceStepIF} and normalize may need information from
	 * {@link TraceStepIF}.
	 * </p>
	 * 
	 * <p>
	 * Note that each distinct state (not equal with each other) will only
	 * normalized once. Since when a unnormalized state is seen again, you can
	 * get its normalized state through its associated {@link SequentialNode}.
	 * </p>
	 * 
	 * @param state
	 *            The state that is being normalized/simplified.
	 * @return the normalized or simplified states.
	 */
	public abstract void normalize(TraceStepIF<STATE> traceStep);

	/**
	 * This method should print the source state id and transitions within this
	 * traceStep. Note that this method should not print the final state in the
	 * traceStep.
	 * 
	 * @param traceStep
	 *            The traceStep you want to print
	 */
	public abstract void printTraceStep(STATE sourceState,
			TraceStepIF<STATE> traceStep);

	/**
	 * <p>
	 * This method should print the final state of a trace step. So a complete
	 * print of a trace step consists of a call to
	 * {@link #printTraceStep(Object, TraceStepIF)} and a call to this method.
	 * </P>
	 * <p>
	 * When this method is called, the final state of the traceStep is already
	 * normalized if (-saveStates option is enabled).
	 * </p>
	 * 
	 * @param finalState
	 *            The final state of a trace step
	 * @param normalizedID
	 *            The ID of a normalized state (-1, if -saveStates option is
	 *            diabled). see also {@link #getId(Object)}
	 */
	public abstract void printTraceStepFinalState(STATE finalState,
			int normalizedID);

	/**
	 * Prints out a short human-readable representation of the state. This is
	 * intended to be something like "State 13", or something similar.
	 * 
	 * @param out
	 *            the stream to which to send the output
	 * @param state
	 *            any state in the state transition system
	 */
	public abstract void printStateShort(PrintStream out, STATE state);

	/**
	 * Prints out a long human-readable representation of the state. This is
	 * intended to show all the details of the state, e.g., the values of all
	 * variables, etc.
	 * 
	 * @param out
	 *            the stream to which to send the output
	 * @param state
	 *            any state in the state transition system
	 */
	public abstract void printStateLong(PrintStream out, STATE state);

	/**
	 * Prints out a short human-readable representation of the transition.
	 * 
	 * @param out
	 *            the stream to which to send the output
	 * @param transition
	 *            any transition in the state transition system
	 */
	public abstract void printTransitionShort(PrintStream out,
			TRANSITION transition);

	/**
	 * Prints out a long human-readable representation of the transition. This
	 * is intended to show all details of the transition.
	 * 
	 * @param out
	 *            the stream to which to send the output
	 * @param transition
	 *            any transition in the state transition system
	 */
	public abstract void printTransitionLong(PrintStream out,
			TRANSITION transition);

	/**
	 * Prints out all the states, in short form, currently "held" by this
	 * manager. It is up to each implementation to decide what states are
	 * "held".
	 * 
	 * @param out
	 *            the stream to which to send the output
	 */
	public abstract void printAllStatesShort(PrintStream out);

	/**
	 * Prints out all the states, in long form, currently "held" by this
	 * manager. It is up to each implementation to decide what states are
	 * "held".
	 * 
	 * @param out
	 *            the stream to which to send the output
	 */
	public abstract void printAllStatesLong(PrintStream out);

	/**
	 * Get the id of a normalizedState.
	 * 
	 * @param normalizedState
	 *            The State of which you want the id.
	 * @return the id of the normalizedState.
	 */
	public int getId(STATE normalizedState) {
		if (nodeFactory != null) {
			SequentialNode<STATE> node = nodeFactory.getNode(normalizedState);

			if (node != null)
				return node.getId();
			else
				return -1;
		}
		return -1;
	}

	public void setSequentialNodeFactory(
			SequentialNodeFactory<STATE, TRANSITION> nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

}