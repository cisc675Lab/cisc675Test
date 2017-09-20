package edu.udel.cis.vsl.gmc.smc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.udel.cis.vsl.gmc.GMCConfiguration;

public class CoverageTest {
	/**
	 * The boolean indicating whether debug info will be printed.
	 */
	private final boolean DEBUG = false;

	/**
	 * The instance of simple model checker.
	 */
	private final SMC smc = new SMC();

	/**
	 * A {@link TestHelper} assist the testing.
	 */
	private TestHelper helper;

	/**
	 * An integer representing the number of states.
	 */
	private int numStates;

	/**
	 * The configuration for SMC
	 */
	private GMCConfiguration config;

	/**
	 * Construct instances for {@link TestHelper} and {@link GMCConfiguration}
	 * 
	 * @param numStates
	 * @throws Exception
	 */
	private void setUpEnv(int numStates) throws Exception {
		helper = new TestHelper(numStates);
		config = helper.generateGMCConfig();
		config.setQuiet(!DEBUG);
	}

	/**
	 * Check assertions
	 * 
	 * @throws Exception
	 */
	private void checkAssertions(boolean hasViolation) throws Exception {
		Integer lastState = (new SMCSimulator(config, System.out))
				.run(helper.getTransitionGraph(), helper.getPredicate())
				.lastState();

		assertTrue(helper.getTransitionGraph().existingTransitions(lastState)
				.size() == 0);
		if (hasViolation) {
			assertTrue(smc.run(helper.getTransitionGraph(),
					helper.getPredicate(), config));
		} else {
			assertFalse(smc.run(helper.getTransitionGraph(),
					helper.getPredicate(), config));
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		helper = null;
		smc.setDebug(DEBUG);
	}

	@After
	public void tearDown() throws Exception {
	}

	// Simple

	@Test
	public void basic_test() throws Exception {
		numStates = 1;
		smc.setDebug(false);
		setUpEnv(numStates);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void single_noAmple_noViolation() throws Exception {
		numStates = 1;
		setUpEnv(numStates);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void simple_noAmple_noViolation() throws Exception {
		numStates = 2;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void simple_hasAmple_noViolation() throws Exception {
		numStates = 2;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void simple_noAmple_hasViolation() throws Exception {
		numStates = 2;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(1);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void simple_hasAmple_hasViolation() throws Exception {
		numStates = 2;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(1);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	// Branch

	@Test
	public void branch_noAmple_noViolation() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void branch_hasAmple_noViolation() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("@t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void branch_noAmple_hasViolationOnRoot() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(0);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void branch_noAmple_hasViolationOnTrunk() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(1);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void branch_noAmple_hasViolationOnBranch() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(2, 3);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void branch_hasAmple_hasViolationOnRoot() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.addTrans("@t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(0);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void branch_hasAmple_hasViolationOnTrunk() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.addTrans("@t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(1);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void branch_hasAmple_hasViolationOnBranch() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.addTrans("@t1", 1, 2);
		helper.addTrans("t2", 1, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(2, 3);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	// Loop

	@Test
	public void loop_noAmple_noViolation() throws Exception {
		numStates = 5;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 4);
		helper.addTrans("t3", 2, 3);
		helper.addTrans("t4", 3, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void loop_hasAmpleOnRoot_noViolation() throws Exception {
		numStates = 5;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 4);
		helper.addTrans("t3", 2, 3);
		helper.addTrans("t4", 3, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void loop_hasAmpleOnIntersectionState_noViolation()
			throws Exception {
		numStates = 5;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("@t1", 1, 2);
		helper.addTrans("@t2", 1, 4);
		helper.addTrans("t3", 2, 3);
		helper.addTrans("t4", 3, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void loop_hasAmpleOnLoop_noViolation() throws Exception {
		numStates = 5;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.addTrans("t2", 1, 4);
		helper.addTrans("t3", 2, 3);
		helper.addTrans("@t4", 3, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

}
