package edu.udel.cis.vsl.gmc;

import edu.udel.cis.vsl.gmc.seq.StateManager;

/**
 * A TraceStepIF represents the execution result of nextState of StateManagerIF.
 * See {@link StateManager#nextState(STATE, TRANSITION)}.
 * 
 * @author Manchun Zheng
 * 
 * @param <STATE>
 *            the type used to represent states in the state-transition system
 *            being analyzed
 * 
 */
public interface TraceStepIF<STATE> {
	/**
	 * Returns the resulting state of the trace step.
	 * 
	 * @return the resulting state of the trace step.
	 */
	STATE getFinalState();

	@Override
	String toString();
}
