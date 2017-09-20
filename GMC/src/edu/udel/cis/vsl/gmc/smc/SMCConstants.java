package edu.udel.cis.vsl.gmc.smc;

import edu.udel.cis.vsl.gmc.Option;
import edu.udel.cis.vsl.gmc.Option.OptionType;

public class SMCConstants {
	/**
	 * The replayer will stop checking after detecting
	 * {@value #DEFAULT_ERROR_BOUND} error(s).
	 */
	static final int DEFAULT_ERROR_BOUND = 1;

	/**
	 * The maximal number of errors allowed before stopping checking. 1 by
	 * default.
	 */
	static final Option DEFAULT_ERROR_BOUND_OPTION = Option.newScalarOption(
			"DEFAULT_ERROR_BOUND", OptionType.INTEGER,
			"stop after finding this many errors", DEFAULT_ERROR_BOUND);

	/**
	 * The starting state value is {@value #DEFAULT_SOURCE_STATE} by
	 * default.
	 */
	static final int DEFAULT_SOURCE_STATE = 0;

	/**
	 * The directory name that the replay output of SMC will be written to.
	 */
	static final String DEFAULT_REPLAY_OUTPUT_DIR = "SMCREP";

	/**
	 * @return an array of {@link Option}s used for constructing
	 *         {@link GMCConfiguration}.
	 */
	public static Option[] getAllOptions() {
		return new Option[]{DEFAULT_ERROR_BOUND_OPTION};
	}

}
