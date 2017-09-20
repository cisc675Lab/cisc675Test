package edu.udel.cis.vsl.gmc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import edu.udel.cis.vsl.gmc.seq.EnablerIF;
import edu.udel.cis.vsl.gmc.util.Pair;
import edu.udel.cis.vsl.gmc.util.Utils;

/**
 * Transition Chooser which makes its choice using an explicit "guide". The
 * guide is a sequence of integers whose length is the number of
 * nondeterministic states encountered along the trace. (A nondeterministic
 * state is one that has more than one enabled transition.)
 *
 * @param <STATE>
 * @param <TRANSITION>
 */
public class GuidedTransitionChooser<STATE, TRANSITION>
		implements
			TransitionChooser<STATE, TRANSITION> {

	static class Guide {
		Guide(int length, LinkedList<Pair<Integer, Integer>> choices) {
			this.length = length;
			this.choices = choices;
		}

		int length; /* the number of steps in the trace */

		// int[] choices; /* transitions to choose when more than 1 enabled */

		LinkedList<Pair<Integer, Integer>> choices;
	}

	private EnablerIF<STATE, TRANSITION> enabler;

	private Guide guide;

	public GuidedTransitionChooser(EnablerIF<STATE, TRANSITION> enabler,
			Guide guide) {
		this.enabler = enabler;
		this.guide = guide;
	}

	public GuidedTransitionChooser(EnablerIF<STATE, TRANSITION> enabler,
			File traceFile) throws IOException, MisguidedExecutionException {
		this.enabler = enabler;
		this.guide = makeGuide(traceFile);
	}

	/**
	 * Returns the length of this execution.
	 * 
	 * @return length of this execution
	 */
	public int getLength() {
		return guide.length;
	}

	/**
	 * Creates a guide by parsing from the given buffered reader. This interface
	 * is provided because the buffered reader may point to the middle of a
	 * file. This is provided because the first part of the file might contain
	 * some application-specific information (such as parameter values), and the
	 * part containing the sequence of integers may start in the middle. This
	 * will parse to the end of the file so expects to see a newline-separated
	 * sequence of integers from the given point on. Closes the reader at the
	 * end.
	 * 
	 * @param reader
	 *            a buffered reader containing a newline-separated sequence of
	 *            integers
	 * @return the sequence of integers
	 * @throws IOException
	 *             if an error occurs in reading from or closing the buffered
	 *             reader
	 * @throws MisguidedExecutionException
	 */
	public static Guide makeGuide(BufferedReader reader)
			throws IOException, MisguidedExecutionException {
		int length;
		LinkedList<Pair<Integer, Integer>> choices = new LinkedList<>();

		while (true) {
			String line = reader.readLine();

			if (line == null)
				throw new MisguidedExecutionException(
						"Trace begin line not found");
			line = line.trim();
			if ("== Begin Trace ==".equals(line))
				break;
		}
		{
			String line = reader.readLine();
			String words[];

			if (line == null)
				throw new MisguidedExecutionException(
						"Trace LENGTH line not found");
			line = line.trim();
			words = line.split(" ");
			if (words.length != 3 || !words[0].equals("LENGTH")
					|| !words[1].equals("="))
				throw new MisguidedExecutionException(
						"Expected \"LENGTH = length\" in trace file, saw "
								+ line);
			try {
				length = new Integer(words[2]);
			} catch (NumberFormatException e) {
				throw new MisguidedExecutionException(
						"Expected integer in trace file, saw " + words[2]);
			}
		}
		while (true) {
			String line = reader.readLine();

			if (line == null)
				break; // end has been reached
			line = line.trim(); // remove white space
			if ("== End Trace ==".equals(line))
				break;
			if (line.isEmpty())
				continue; // skip blank line
			try {
				String[] indexCountPair = line.split(":");

				if (indexCountPair.length != 2) {
					throw new MisguidedExecutionException(
							"Malformed trace file: transition index should be in the form of a:b"
									+ line);
				}

				int indexCount = Integer.parseInt(indexCountPair[0]);
				int index = Integer.parseInt(indexCountPair[1]);

				choices.add(new Pair<Integer, Integer>(index, indexCount));
			} catch (NumberFormatException e) {
				throw new MisguidedExecutionException(
						"Expected integer, saws " + line);
			}
		}
		reader.close();

		return new Guide(length, choices);
	}

	/**
	 * Creates a guide by parsing a file which is a newline-separated list of
	 * integers.
	 * 
	 * @param file
	 *            a newline-separated list of integers
	 * @return the integers, as an array
	 * @throws IOException
	 *             if a problem occurs in opening, reading from, or closing the
	 *             file
	 * @throws MisguidedExecutionException
	 */
	public static Guide makeGuide(File file)
			throws IOException, MisguidedExecutionException {
		return makeGuide(new BufferedReader(new FileReader(file)));
	}

	/**
	 * change the format of the file providing more information. 1 25:0 (25 0s
	 * in the file) ac 3 36:0 (36 0s in the file)
	 */
	@Override
	public TRANSITION chooseEnabledTransition(STATE state)
			throws MisguidedExecutionException {
		LinkedList<Pair<Integer, Integer>> choices = guide.choices;
		Pair<Integer, Integer> indexCountPair = choices.peek();

		if (indexCountPair == null)
			return null;

		Collection<TRANSITION> ampleset = enabler.ampleSet(state);
		int ampleSetSize = ampleset.size();
		int count = --(indexCountPair.right);
		int index = indexCountPair.left;
		TRANSITION result = null;

		if (index == -1)
			return result;
		if (count == 0)
			choices.pop();
		if (index >= ampleSetSize) {
			Collection<TRANSITION> fullSet = enabler.fullSet(state);
			@SuppressWarnings("unchecked")
			Collection<TRANSITION> ampleSetComplement = (Collection<TRANSITION>) Utils
					.subtract(fullSet, ampleset);

			index -= ampleSetSize;
			try {
				result = getTransition(ampleSetComplement, index);
			} catch (NoSuchElementException e) {
				throw new MisguidedExecutionException(
						"State has fewer enabled transitions than expected: "
								+ state);
			}
		} else {
			try {
				result = getTransition(ampleset, index);
			} catch (NoSuchElementException e) {
				throw new MisguidedExecutionException(
						"State has fewer enabled transitions than expected: "
								+ state);
			}
		}

		return result;
		// if (!iterator.hasNext())
		// return null;
		// if (!(ampleset.size() > 1))
		// return iterator.next();
		// else if (guideIndex < guide.length) {
		// int index = guide.choices[guideIndex];
		//
		// guideIndex++;
		// for (int i = 0; i < index; i++) {
		// if (iterator.hasNext())
		// iterator.next();
		// else
		// throw new MisguidedExecutionException(
		// "State has fewer enabled transitions than expected: "
		// + state);
		// }
		// if (!iterator.hasNext())
		// throw new MisguidedExecutionException(
		// "State has fewer enabled transitions than expected: "
		// + state);
		// return iterator.next();
		// } else {
		// throw new MisguidedExecutionException(
		// "Trace file ends before trail is complete.");
		// }
	}

	private TRANSITION getTransition(Collection<TRANSITION> transitions,
			int index) {
		Iterator<TRANSITION> transitionIterator = transitions.iterator();
		
		while (index > 0) {
			transitionIterator.next();
			index--;
		}

		return transitionIterator.next();
	}
}
