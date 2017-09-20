package edu.udel.cis.vsl.gmc.smc;

import java.util.LinkedList;

/**
 * A simple directed graph implemented with a square matrix.
 * 
 * @author Ziqing Luo
 * @author Wenhao Wu
 *
 */
public class MatrixDirectedGraph {
	/**
	 * The square matrix which represents a directed state-transition graph
	 */
	private String[][] matrix;

	/**
	 * The length of a side of the square matrix
	 */
	private int numStates;

	/**
	 * Constructs a {@link MatrixDirectedGraph} with given
	 * <code>squareMatrix</code>. <br>
	 * The <code>squareMatrix</code> is a 2-dimensional String matrix.The row
	 * index is the source state, the column index is the the destination state,
	 * and the String element in the matrix is the corresponding transition.<br>
	 * E.g., <br>
	 * 
	 * <pre>
	 * {@code
	 * Matrix:     Graph:
	 * X 0 1 2 3      0	
	 * 0   a b    'a'/ \'b'	
	 * 1       c    1   2
	 * 2       d  'c'\ /'d'	
	 * 3     		3	
	 * }
	 * </pre>
	 * 
	 * <br>
	 * For each state <code>s</code>, if <code>s</code> has <strong>at least one
	 * outgoing transition</strong> starting with '@' then the ample set for
	 * <code>s</code> consists of all transitions departing from <code>s</code>
	 * whose names start with '@'. If <code>s</code> has no outgoing transition
	 * with name beginning with '@' then the ample set consists of all
	 * transitions departing from <code>s</code>.<br>
	 * E.g., <br>
	 * 
	 * <pre>
	 * {@code
	 * Matrix:      		Graph:
	 * X 0  1  2  3  4  5	      0	
	 * 0    @a @b c   	'@a'/ |'@b'\'c'	
	 * 1             d  e	   1  2     3
	 * 2            	       'd'/ \'e'
	 * 3            	  	 4   5
	 * 4			- State 0 has an ample set consisting of '@a' and '@b'
	 * 5			- State 1 has an ample set consisting of 'd' and 'e'
	 * }
	 * </pre>
	 * 
	 * <strong>Preconditions:</strong><br>
	 * 1. The given <code>squareMatrix</code> must have a same value for its row
	 * number and column number.<br>
	 * 
	 * @param squareMatrix
	 *            an input 2-dimensional String matrix used for generating the
	 *            directed state-transition graph.
	 * @throws Exception
	 */
	public MatrixDirectedGraph(String[][] squareMatrix) throws Exception {
		int rowLength = squareMatrix.length;

		for (int i = 0; i < rowLength; i++)
			if (squareMatrix[i].length != rowLength)
				throw new Exception("Given matrix must be a square matrix.");
		this.matrix = squareMatrix;
		this.numStates = rowLength;
	}

	/**
	 * Get all outgoing transitions from the given source state to all other
	 * states in <code>this</code> graph. If there is no transition between the
	 * <code>sourceState</code> and the specific destination state then the
	 * corresponding transition is <code>null</code>
	 * 
	 * @param sourceState
	 *            The source state.
	 * @return an array consisting of all transitions outgoing from the given
	 *         state
	 */
	public String[] allTransitions(Integer sourceState) {
		String[] transitions = new String[numStates];

		assert sourceState >= 0 && sourceState < numStates;
		for (int i = 0; i < numStates; i++) {
			transitions[i] = matrix[sourceState][i];
		}
		return transitions;
	}

	/**
	 * Find the destination state with the given <code>sourceState</code> and
	 * <code>transition</code>.
	 * 
	 * @param sourceState
	 *            the source state
	 * @param transition
	 *            the transition outgoing from the <code>sourceState</code>
	 * @return the destination state; if it is not found then
	 *         {@link Integer#MIN_VALUE} will be returned.
	 */
	public Integer getDestState(Integer sourceState, String transition) {
		assert sourceState >= 0 && sourceState < numStates;
		for (int i = 0; i < numStates; i++)
			if (matrix[sourceState][i] != null
					&& matrix[sourceState][i].equals(transition))
				return i;
		return Integer.MIN_VALUE;
	}

	/**
	 * Get all existing outgoing transitions from the given
	 * <code>sourceState</code> in <code>this</code> graph.
	 * 
	 * @param sourceState
	 *            The source state.
	 * @return a {@link LinkedList} of outgoing transitions
	 */
	public LinkedList<String> existingTransitions(Integer sourceState) {
		LinkedList<String> transitions = new LinkedList<String>();

		assert sourceState >= 0 && sourceState < numStates;
		for (String transition : matrix[sourceState])
			if (transition != null)
				transitions.add(transition);
		return transitions;
	}

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();

		sBuilder.append("The transition map is: \n\t");
		sBuilder.append("X\t");

		for (int i = 0; i < numStates; i++) {
			sBuilder.append(i);
			sBuilder.append("\t");
		}
		for (int i = 0; i < numStates; i++) {
			sBuilder.append("\n\t");
			sBuilder.append(i);
			sBuilder.append("\t");
			for (int j = 0; j < numStates; j++) {
				if (matrix[i][j] != null)
					sBuilder.append(matrix[i][j]);
				sBuilder.append("\t");
			}
		}
		sBuilder.append("\n");
		return sBuilder.toString();
	}
}
