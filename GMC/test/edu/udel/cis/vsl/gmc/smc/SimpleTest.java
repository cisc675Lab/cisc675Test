/**
 * 
 */
package edu.udel.cis.vsl.gmc.smc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.udel.cis.vsl.gmc.GMCConfiguration;

/**
 * The basic test set for SMC
 * 
 * @author Wenhao Wu (wuwenhao@udel.edu)
 */
public class SimpleTest {
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
	 * @TODO: Check the random simulator has no enabled transitions from the
	 *        final state.
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

	@Test
	public void noViolation_noAmple() throws Exception {
		numStates = 2;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void violationOnFinalState_noAmple() throws Exception {
		numStates = 2;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(1);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	@Test
	public void noViolation_withAmple() throws Exception {
		numStates = 3;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.printMat(DEBUG);
		checkAssertions(true);
	}

	@Test
	public void violationOnFinalState_withAmple() throws Exception {
		numStates = 3;
		setUpEnv(numStates);
		helper.addTrans("@t0", 0, 1);
		helper.addTrans("t1", 1, 2);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(1);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}

	/**
	 * State 3 is an error state <code>
	 *   0
	 *  / \
	 * 1   2
	 *  \ /
	 *   3
	 * </code>
	 * 
	 * @throws Exception
	 */
	@Test
	public void basic_smc_test_violationOnState3() throws Exception {
		numStates = 4;
		setUpEnv(numStates);
		helper.addTrans("t0", 0, 1);
		helper.addTrans("t1", 1, 3);
		helper.addTrans("t2", 0, 2);
		helper.addTrans("t3", 2, 3);
		helper.printMat(DEBUG);
		helper.generateViolationPredicate(3);
		helper.printViolationStatePredicate(DEBUG);
		checkAssertions(false);
	}
}
