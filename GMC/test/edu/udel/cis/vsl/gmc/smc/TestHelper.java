package edu.udel.cis.vsl.gmc.smc;

import java.io.PrintStream;
import java.util.Arrays;

import edu.udel.cis.vsl.gmc.GMCConfiguration;

public class TestHelper {
	private String[][] transMap;
	private Predicate predicate;
	private PrintStream out;

	public TestHelper(int numState) throws Exception {
		this.transMap = new String[numState][numState];
		this.predicate = new Predicate();
		out = System.out;
	}

	public MatrixDirectedGraph getTransitionGraph() throws Exception {
		return new MatrixDirectedGraph(transMap);
	}

	public Predicate getPredicate() {
		return this.predicate;
	}

	public void addTrans(String transitionLabel, int srcStateId,
			int destStateId) {
		transMap[srcStateId][destStateId] = transitionLabel;
	}

	public void generateViolationPredicate(Integer... stateIds) {
		predicate = new Predicate(stateIds);
	}

	public GMCConfiguration generateGMCConfig() {
		return new GMCConfiguration(
				Arrays.asList(SMCConstants.getAllOptions()));
	}

	public void printMat(boolean isDebug) throws Exception {
		if (isDebug) {
			out.println(new MatrixDirectedGraph(transMap));
		}
	}

	public void printViolationStatePredicate(boolean isDebug) {
		if (isDebug) {
			out.println(predicate);
		}
	}
}
