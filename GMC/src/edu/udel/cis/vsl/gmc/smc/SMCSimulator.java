package edu.udel.cis.vsl.gmc.smc;

import static edu.udel.cis.vsl.gmc.smc.SMCConstants.DEFAULT_ERROR_BOUND_OPTION;
import static edu.udel.cis.vsl.gmc.smc.SMCConstants.DEFAULT_REPLAY_OUTPUT_DIR;
import static edu.udel.cis.vsl.gmc.smc.SMCConstants.DEFAULT_SOURCE_STATE;

import java.io.File;
import java.io.PrintStream;

import edu.udel.cis.vsl.gmc.ErrorLog;
import edu.udel.cis.vsl.gmc.GMCConfiguration;
import edu.udel.cis.vsl.gmc.MisguidedExecutionException;
import edu.udel.cis.vsl.gmc.RandomTransitionChooser;
import edu.udel.cis.vsl.gmc.Simulator;
import edu.udel.cis.vsl.gmc.StatePredicateIF;
import edu.udel.cis.vsl.gmc.Trace;
import edu.udel.cis.vsl.gmc.TransitionChooser;
import edu.udel.cis.vsl.gmc.seq.EnablerIF;

/**
 * 
 * 
 * @author Wenhao Wu (wuwenhao@udel.edu)
 */
public class SMCSimulator {

	/**
	 * The {@link GMCConfiguration} used for configuring <code>this</code> trace
	 * player.
	 */
	private GMCConfiguration config;

	/**
	 * The {@link PrintStream} used for printing normal info.
	 */
	private PrintStream out = null;

	/**
	 * The GMC {@link ErrorLog} used for printing error info.
	 */
	private ErrorLog log = null;

	/**
	 * The {@link SimpleStateManager} used for replaying the trace info.
	 */
	private SimpleStateManager stateManager = null;

	/**
	 * The implementation of {@link EnablerIF} used for constructing the
	 * Transition-state system.
	 */
	private EnablerIF<Integer, String> enabler = null;

	/**
	 * The {@link SMCTransitionChooser} used for constructing {@link #simulator}.
	 */
	private TransitionChooser<Integer, String> chooser = null;

	/**
	 * The sequential {@link Simulator} used for replaying the trace info.
	 */
	private Simulator<Integer, String> simulator = null;

	public SMCSimulator(GMCConfiguration config, PrintStream out) {
		this.config = config;
		this.out = out;
	}

	public Trace<String, Integer> run(MatrixDirectedGraph graph,
			StatePredicateIF<Integer> predicate)
			throws MisguidedExecutionException {
		return run(graph, predicate, DEFAULT_SOURCE_STATE);
	}

	public Trace<String, Integer> run(MatrixDirectedGraph graph,
			StatePredicateIF<Integer> predicate, Integer initialState)
			throws MisguidedExecutionException {
		this.enabler = new SMCEnabler(graph);
		this.stateManager = new SimpleStateManager(graph);
		this.log = new ErrorLog(new File(DEFAULT_REPLAY_OUTPUT_DIR),
				graph.toString(), out);
		this.log.setErrorBound((int) config.getAnonymousSection()
				.getValueOrDefault(DEFAULT_ERROR_BOUND_OPTION));
		this.simulator = new Simulator<Integer, String>(stateManager, out);
		this.simulator.setPrintAllStates(false);
		this.simulator.setQuiet(config.isQuiet());
		this.simulator.setPredicate(predicate);
		this.chooser = new RandomTransitionChooser<Integer, String>(enabler);

		Trace<String, Integer> trace = simulator.play(initialState, chooser,
				!config.isQuiet())[0];
		boolean violation = trace.violation();

		violation = violation || log.numErrors() > 0;
		if (violation && !simulator.isQuiet()) {
			out.println("Violation(s) found.");
			out.flush();
		}
		trace.setViolation(violation);
		return trace;
	}
}
