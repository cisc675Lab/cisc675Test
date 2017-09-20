package edu.udel.cis.vsl.gmc.concurrent;

import edu.udel.cis.vsl.gmc.seq.StateManager;

/**
 * A ConcurrentStateManager is used by a {@link ConcurrentDfsSearcher} which
 * encapsulates all methods that are needed by {@link ConcurrentDfsSearcher} on
 * STATE.
 *
 * @param <STATE>
 *            the type used to represent states in the state-transition system
 *            being analyzed
 * @param <TRANSITION>
 *            the type used to represent transitions in the state-transition
 *            system being analyzed
 * 
 * @author Yihao Yan (yihaoyan)
 */
public abstract class ConcurrentStateManagerIF<STATE, TRANSITION>
		extends
			StateManager<STATE, TRANSITION> {
	public abstract STATE normalize(STATE traceStep);
}