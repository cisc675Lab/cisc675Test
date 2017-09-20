package edu.udel.cis.vsl.gmc.smc;

import edu.udel.cis.vsl.gmc.TraceStepIF;

/**
 * The implementation of the interface {@link TraceStepIF} used by SMC.
 * 
 * @author Wenhao Wu (wuwenhao@udel.edu)
 */
public class TraceStep implements TraceStepIF<Integer> {
	/**
	 * The transition related with <code>this</code> trace step.
	 */
	private String transition;

	/**
	 * The final state of <code>this</code> trace-step
	 */
	private Integer finalState;

	/**
	 * Construct an instance of {@link TraceStep} with given
	 * <code>transition</code> and <code>finalState</code>
	 * 
	 * @param transition
	 *            the transition related with <code>this</code> trace step.
	 * @param finalState
	 *            the final state of <code>this</code> trace step.
	 */
	public TraceStep(String transition, Integer finalState) {
		this.transition = transition;
		this.finalState = finalState;
	}

	@Override
	public Integer getFinalState() {
		return this.finalState;
	}

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();

		sBuilder.append("['");
		sBuilder.append(transition);
		sBuilder.append("'=>State<");
		sBuilder.append(finalState);
		sBuilder.append(">]");
		return sBuilder.toString();
	}
}
