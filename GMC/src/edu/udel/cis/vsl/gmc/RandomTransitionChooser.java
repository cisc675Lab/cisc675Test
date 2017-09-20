package edu.udel.cis.vsl.gmc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import edu.udel.cis.vsl.gmc.seq.EnablerIF;

/**
 * An implementation of {@link TransitionChooser} in which a transition is
 * chosen randomly from the full set of the enabled transitions enabled at a
 * state.
 * 
 * @author Stephen F. Siegel (siegel@udel.edu)
 *
 * @param <STATE>
 *            the type for the states of the transition system
 * @param <TRANSITION>
 *            the type for the transitions of the transition system
 */
public class RandomTransitionChooser<STATE, TRANSITION>
		implements
			TransitionChooser<STATE, TRANSITION> {

	/**
	 * The seed used to create the {@link Random} number generator.
	 */
	private long seed;

	private EnablerIF<STATE, TRANSITION> enabler;

	private Random generator;

	public RandomTransitionChooser(EnablerIF<STATE, TRANSITION> enabler,
			long seed) {
		this.enabler = enabler;
		this.seed = seed;
		this.generator = new Random(seed);
	}

	public RandomTransitionChooser(EnablerIF<STATE, TRANSITION> enabler) {
		this(enabler, System.currentTimeMillis());
	}

	@Override
	public TRANSITION chooseEnabledTransition(STATE state)
			throws MisguidedExecutionException {
		ArrayList<TRANSITION> transitions = new ArrayList<TRANSITION>();
		Collection<TRANSITION> enabledTransitions = enabler.fullSet(state);
		Iterator<TRANSITION> iterator = enabledTransitions.iterator();
		int n, i;

		while (iterator.hasNext())
			transitions.add(iterator.next());
		n = transitions.size();
		if (n == 0)
			return null;
		i = generator.nextInt(n);
		return transitions.get(i);
	}

	public long getSeed() {
		return seed;
	}
}
