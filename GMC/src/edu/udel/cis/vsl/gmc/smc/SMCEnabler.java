package edu.udel.cis.vsl.gmc.smc;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import edu.udel.cis.vsl.gmc.seq.EnablerIF;

/**
 * The implementation of {@link EnablerIF} used by SMC.
 * 
 * @author Ziqing Luo
 * @author Wenhao Wu (wuwenhao@udel.edu)
 */
public class SMCEnabler implements EnablerIF<Integer, String> {

	/**
	 * The directed graph of the state-transition model
	 */
	private MatrixDirectedGraph graph;

	/**
	 * The boolean indicating whether debug info will be printed
	 */
	private boolean debug = false;

	/**
	 * The {@link PrintStream} used for printing debug info.
	 */
	private PrintStream debugStream = System.out;

	public SMCEnabler(MatrixDirectedGraph graph) {
		this.graph = graph;
	}

	/**
	 * {@inheritDoc}<br>
	 * <p>
	 * Note that, all transitions in a same {@link Collection} should share a
	 * same source state. (Because the SMC uses the fly-weight)
	 * </p>
	 */
	@Override
	public Collection<String> ampleSet(Integer source) {
		LinkedList<String> existingTransitions = graph
				.existingTransitions(source);
		LinkedList<String> ampleSet = new LinkedList<>();

		for (String transition : existingTransitions)
			if (transition.startsWith("@"))
				ampleSet.add(transition);
		if (ampleSet.isEmpty())
			return existingTransitions;
		return ampleSet;
	}

	@Override
	public Collection<String> fullSet(Integer state) {
		return graph.existingTransitions(state);
	}

	@Override
	public void setDebugging(boolean value) {
		this.debug = value;
	}

	@Override
	public boolean debugging() {
		return debug;
	}

	@Override
	public void setDebugOut(PrintStream out) {
		this.debugStream = out;
	}

	@Override
	public PrintStream getDebugOut() {
		return debugStream;
	}
}
