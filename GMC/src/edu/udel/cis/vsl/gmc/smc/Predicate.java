package edu.udel.cis.vsl.gmc.smc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import edu.udel.cis.vsl.gmc.StatePredicateIF;

/**
 * The predicate used for detecting violation state defined in the given list
 * <code>states</code>. <br>
 * 
 * @author Wenhao Wu (wuwenhao@udel.edu)
 *
 */
public class Predicate implements StatePredicateIF<Integer> {
	/**
	 * The violation state collection.<br>
	 * If any state is in this collection, that state is considered as a
	 * violation state.
	 */
	private Collection<Integer> violationStates = new LinkedList<>();

	/**
	 * Temporarily store the latest detected violation state for
	 * {@link #explanation()}.
	 */
	private Integer detectedViolationState;

	public Predicate(Integer... states) {
		Collections.addAll(violationStates, states);
	}

	/**
	 * {@inheritDoc}<br>
	 * <p>
	 * For the Violation State Predicate, if the given <code>state<state> is in
	 * the field <code>violationStates</code>, this function will return
	 * <code>true</code> (which will make the SMC return <code>false</code> for
	 * this violation), else <code>false<code>.
	 * </p>
	 */
	@Override
	public boolean holdsAt(Integer state) {
		boolean result = violationStates.contains(state);

		if (result)
			this.detectedViolationState = state;
		return result;
	}

	@Override
	public String explanation() {
		StringBuilder sBuilder = new StringBuilder();

		sBuilder.append("Violation type: ");
		sBuilder.append(Predicate.class.getName());
		sBuilder.append("\nState<");
		sBuilder.append(detectedViolationState);
		sBuilder.append("> is in the violation state collection: \n\t{");
		for (Integer state : violationStates) {
			sBuilder.append("<");
			sBuilder.append(state);
			sBuilder.append(">,");
		}
		sBuilder.append("}");
		return sBuilder.toString();
	}

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();

		sBuilder.append("'");
		sBuilder.append(Predicate.class.getName());
		sBuilder.append("'");
		return sBuilder.toString();
	}
}
