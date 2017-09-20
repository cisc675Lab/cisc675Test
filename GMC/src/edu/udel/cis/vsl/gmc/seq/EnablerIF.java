package edu.udel.cis.vsl.gmc.seq;

import java.io.PrintStream;
import java.util.Collection;

/**
 * <p>
 * An EnablerIF tells you which transitions should be explored from a given
 * state. It might need to know things about the state of the search (such as
 * what states are currently on the DFS stack). Such information can be provided
 * at creation.
 * </p>
 * 
 * <p>
 * A transition is an edge in the state space which means that two transitions
 * emanating from different states are different even if they originated from
 * the same statement.
 * </p>
 * 
 * @param <STATE>
 *            the type used to represent states in the state-transition system
 *            being analyzed
 * @param <TRANSITION>
 *            the type used to represent transitions in the state-transition
 *            system being analyzed
 * 
 * @author Stephen F. Siegel, University of Delaware
 * @author Yihao Yan (yanyihao)
 */
public interface EnablerIF<STATE, TRANSITION> {

	/**
	 * <p>
	 * Return the candidate ampleSet of transitions departing from a given state
	 * by satisfying the following three conditions:
	 * </p>
	 * <ul>
	 * <li>C0: ampleSet is not empty if the full enabled set is not empty.</li>
	 * <li>C1: Along every path in the full state graph that starts at source
	 * state, the following condition holds: a transition that is dependent on a
	 * transition in ample(s) can not be executed without a transition in
	 * ample(s) occurring first</li>
	 * <li>C2: If source state is not fully expanded, then every transition in
	 * ample(s) is invisible.</li>
	 * </ul>
	 * <p>
	 * For detail information about how to compute the ample set of a state,
	 * please refer to the book "Model Checking" by Edmund M. Clarke Jr.
	 * </p>
	 * 
	 * @param source
	 *            The target state.
	 * @return The candidate ampleSet of transitions of the target state.
	 */
	Collection<TRANSITION> ampleSet(STATE source);

	/**
	 * Computes the set of transitions which are enabled at {@code state}.
	 * 
	 * @param state
	 *            The source state.
	 * @return the collection of transitions that are enabled at {@code state}.
	 */
	Collection<TRANSITION> fullSet(STATE state);

	/**
	 * Set the debugging flag to the given value. When true, debugging output
	 * will be printed to the debugging output stream.
	 * 
	 * @param value
	 *            true if you want to print debugging info, false otherwise
	 */
	void setDebugging(boolean value);

	/**
	 * Returns the current value of the debugging flag. When true, debugging
	 * output will be be printed to the debugging output stream.
	 * 
	 * @return current value of debugging flag.
	 */
	boolean debugging();

	/**
	 * Set the debugging output stream to the given stream.
	 * 
	 * @param out
	 *            the stream to which you want the debugging output to be sent
	 */
	void setDebugOut(PrintStream out);

	/**
	 * Returns the debugging output stream. This is the stream to which debuging
	 * output will be printed when the debugging flag is on.
	 * 
	 * @return the debugging output stream
	 */
	PrintStream getDebugOut();

}
