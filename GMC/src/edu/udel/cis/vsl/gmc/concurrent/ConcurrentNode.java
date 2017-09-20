package edu.udel.cis.vsl.gmc.concurrent;

public class ConcurrentNode<STATE> {

	private STATE state;

	private int[] onStack = new int[2
			* Runtime.getRuntime().availableProcessors()];

	private boolean fullyExplored = false;

	private ProvisoValue proviso = ProvisoValue.UNKNOWN;

	public ConcurrentNode(STATE state) {
		this.state = state;
	}

	/**
	 * Indicate whether a STATE is on the stack of the thread with certain id.
	 * 
	 * @param state
	 *            The given state.
	 * @param tid
	 *            The unique identifier of a thread.
	 * 
	 * @return true if state is on the stack of the thread(id).
	 */
	public boolean onStack(int tid) {
		return onStack[tid] > 0;
	}

	/**
	 * Indicate whether a STATE is fully explored (all its descendants have been
	 * visited). This is the same with telling whether a state is colored 'blue'
	 * in algorithm2 in <a href=
	 * "http://link.springer.com/chapter/10.1007%2F978-3-319-13338-6_20">Larrman
	 * 's paper</a>.
	 * 
	 * @param state
	 *            The given state.
	 * 
	 * @return true if all state is fully explored.
	 */
	public boolean fullyExplored() {
		return fullyExplored;
	}

	/**
	 * Set "fullyExplored" field of a STATE to a certain boolean value.
	 * 
	 * @param state
	 *            The given state.
	 * @param The
	 *            Value that is set to the "fully explored" flag of a state.
	 */
	public void setFullyExplored(boolean value) {
		this.fullyExplored = value;
	}

	/**
	 * Set the onStack field of a state.
	 * 
	 * @param state
	 *            The given state.
	 * @param id
	 *            The unique identifier of the thread
	 * @param value
	 *            The value that is set to the "onStack" flag of the given
	 *            state.
	 * 
	 */
	void setOnStack(int tid, boolean value) {
		onStack[tid] = value ? 1 : 0;
	}

	/**
	 * Set the proviso field of a state using atomic CAS operation.
	 * 
	 * @param state
	 *            The given state.
	 * 
	 * @param value
	 *            The value that is set to the "stackProviso" flag of the given
	 *            state.
	 */
	boolean setStackProvisoCAS(ProvisoValue value) {
		synchronized (this) {
			if (value == ProvisoValue.UNKNOWN) {
				this.proviso = value;
				return true;
			}

			return false;
		}
	}

	public STATE getState() {
		return state;
	}

	public ProvisoValue getProviso() {
		return proviso;
	}
}
