/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp. All Rights Reserved.
 ******************************************************************************/
/* =============================================================================
 *
 *                  COPYRIGHT 2010 BBN Technologies Corp.
 *                  1300 North 17th Street, Suite 600
 *                       Arlington, VA  22209
 *                          (703) 284-1200
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * ==============================================================================
 */
package com.bbn.c2s2.pint.pf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.heuristics.EdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;

/**
 * Creates a graph of {@link Binding} objects where the edges between nodes are
 * based on the calculated {@link EdgeWeightHeuristic}. The resulting graph will
 * have strong edges between {@link Binding} objects that can agreeably
 * participate in the given {@link Process} together.
 * 
 * @author jsherman
 * 
 */
public class PairGraph {
	

	/**
	 * Square {@link Matrix} of {@link Binding} objects where non-zero values
	 * represent weighted edges between {@link Binding} objects.
	 */
	private Matrix edgeMatrix;

	/**
	 * List of {@link Binding} objects in the given {@link RnrmProcess}
	 */
	private List<Binding> bindings;

	/**
	 * Heuristic used to calculate the edge weight between two {@link Binding}
	 * objects
	 */
	private IEdgeWeightHeuristic heuristic;

	/**
	 * Index of non-zero edge weights in the {@link Matrix}. There is a row for
	 * each {@link Binding} in the process. The values in each row are column
	 * indexes for the full {@link Matrix}. The actual column indexes in
	 * mxCoordIndex can then be used to find the edge weight by going to the
	 * same coordinate in the edgeWeights matrix. Example: If mxCoordIndex[3][5]
	 * has the value 100, then that means that the value at edgeWeights[3][5] is
	 * the value you would find at [3][100] in the full {@link Binding}
	 * {@link Matrix}.
	 */
	private int[][] mxCoordIndex;

	/**
	 * Index of non-zero weights in the {@link Binding} {@link Matrix}. This 2d
	 * array mirrors mxCoordIndex.
	 */
	private double[][] edgeWeights;
	
	/**
	 * Map to facilitate lookup into the adjacency matrix for this PairGrpah.
	 */
	
	private Map<Binding, Integer> bindingToIndex = new HashMap<Binding, Integer>();
	

	/**
	 * Constructs a {@link PairGraph} object.
	 * 
	 * @param group
	 *            BindingGroup containing all of the potential {@link Binding}s.
	 * @param h
	 *            Heuristic to use for calculating the weight between 2
	 *            {@link Binding}s.
	 */
	public PairGraph(BindingGroup group,
			IEdgeWeightHeuristic h) {
		this.heuristic = h;
		this.bindings = group.getBindings();
		for (int i = 0; i < this.bindings.size(); i++) {
			this.bindingToIndex.put(this.bindings.get(i), i);
		}
		this.buildMatrix();
	}
	
	public PairGraph(List<Binding> group, IEdgeWeightHeuristic h) {
		this.heuristic = h;
		this.bindings = group;
		for (int i = 0; i < this.bindings.size(); i++) {
			this.bindingToIndex.put(this.bindings.get(i), i);
		}
		this.buildMatrix();
	}

	/**
	 * Finds the index of the given {@link Binding} in the {@link Matrix} that
	 * represents this graph. The index can be used to find both the row and the
	 * column that represent this {@link Binding}.
	 * 
	 * @param b
	 *            {@link Binding} to find
	 * @return Index of the {@link Binding} or -1 if it does not exist
	 */
	public int indexOf(IBinding b) {
		Integer i = bindingToIndex.get(b);
		return (null != i) ? i.intValue() : -1;
	}
	
	
	public int[][] getNonZeroCols() {
		return this.mxCoordIndex;
	}

	/**
	 * Returns the {@link Set} of {@link Binding} indexes where the edge weight
	 * between the {@link Binding}s at each index and the {@link Binding} at the
	 * given index is greater than zero.
	 * 
	 * @param bindingIndex
	 *            Index of the {@link Binding} to find connections from.
	 * @return {@link Set} of indexes to the connected {@link Binding}s.
	 */
	public Set<Integer> getConnectedNodes(IBinding binding) {
		Set<Integer> rv = new HashSet<Integer>();
		int bindingIndex = this.bindingToIndex.get(binding);
		for (int i = 0; i < this.mxCoordIndex[bindingIndex].length; i++) {
			if (edgeWeights[bindingIndex][i] > 0) {
				rv.add(this.mxCoordIndex[bindingIndex][i]);
			}
		}
		return rv;
	}
	
	public Set<Integer> getConnectedNodes(int bindingIndex) {
		Set<Integer> rv = new HashSet<Integer>();
		for (int i = 0; i < this.mxCoordIndex[bindingIndex].length; i++) {
			if (edgeWeights[bindingIndex][i] > 0) {
				rv.add(this.mxCoordIndex[bindingIndex][i]);
			}
		}
		return rv;
	}
	

	/**
	 * Get all of the {@link Binding}s in this graph.
	 * 
	 * @return {@link List} of {@link Binding} objects.
	 */
	public List<Binding> getBindings() {
		return this.bindings;
	}

	/**
	 * Removes all {@link Binding} objects that contain the {@link Observation}
	 * with the given ID.
	 * 
	 * @param obsId
	 *            ID of the {@link Observation} to remove.
	 */
	public void removeBindingsWithObservation(int obsId) {
		for (int i = 0; i < bindings.size(); i++) {
			if (bindings.get(i).getObservationID() == obsId)
				this.removeBinding(bindings.get(i));
		}
	}

	/**
	 * Removes the given {@link Binding} from the available list and zeroes out
	 * any edges connected to it.
	 * 
	 * @param b
	 *            {@link Binding} to remove.
	 */
	private void removeBinding(IBinding b) {
		this.removeWeights(b);
		for (int i = 0; i < edgeWeights.length; i++) {
			for (int j = 0; j < edgeWeights[i].length; j++) {
				int actualCol = this.mxCoordIndex[i][j];
				this.edgeMatrix.set(i, actualCol, edgeWeights[i][j]);
			}
		}
	}

	/**
	 * Zeros out the weights for the given {@link Binding}. In other words, this
	 * removes all edges connected to the given {@link Binding} in this graph.
	 * 
	 * @param b
	 *            {@link Binding} to zero out.
	 */
	private void removeWeights(IBinding b) {
		int index = this.indexOf(b);
		for (int r = 0; r < this.bindings.size(); r++) {
			int c = this.findColumnIndex(r, index);
			if (c != -1) {
				this.edgeWeights[r][c] = 0;
			}
		}
		for (int i = 0; i < this.edgeWeights[index].length; i++) {
			this.edgeWeights[index][i] = 0;
		}
	}

	/**
	 * Determines the column-index within the partial matrices, mxCoordIndex and
	 * edgeWeights, that represent the true bindingIndex in the full
	 * {@link Matrix}.
	 * 
	 * @param rowVec
	 *            Index of the row to search in
	 * @param bindingIndex
	 *            Index of the desired {@Binding} in the full
	 *            {@link Matrix}.
	 * @return Column-index, i, such that edgeWeights[rowVec][i] will provide
	 *         the value of edgeMatrix[rowVec][bindingIndex].
	 */
	private int findColumnIndex(int rowVec, int bindingIndex) {
		for (int i = 0; i < this.mxCoordIndex[rowVec].length; i++) {
			if (bindingIndex == this.mxCoordIndex[rowVec][i])
				return i;
		}
		return -1;
	}

	/**
	 * Builds a square matrix of all {@link Binding} pairs where the values in
	 * the matrix represent the edge-weights between the pair as computed by the
	 * EdgeWeightHeuristic.
	 * 
	 * @param process
	 *            RnrmProcess to use for calculating weights between
	 *            {@link Binding}s.
	 */
	private void buildMatrix() {
		this.mxCoordIndex = new int[this.bindings.size()][];
		this.edgeWeights = new double[this.bindings.size()][];
		for (int i = 0; i < this.bindings.size(); i++) {
			List<Integer> tmp = new ArrayList<Integer>();
			List<Double> w_tmp = new ArrayList<Double>();
			for (int j = 0; j < this.bindings.size(); j++) {
				if (i != j) {
					double w = this.heuristic.getLinkWeight(this.bindings.get(i), this.bindings.get(j));
					if (w > 0) {
						tmp.add(j);
						w_tmp.add(w);
					}
				}
			}
			int[] colIndices = new int[tmp.size()];
			double[] w_colIndices = new double[tmp.size()];
			for (int c = 0; c < colIndices.length; c++) {
				colIndices[c] = tmp.get(c);
				w_colIndices[c] = w_tmp.get(c);
			}
			mxCoordIndex[i] = colIndices;
			this.edgeWeights[i] = w_colIndices;
		}
		this.edgeMatrix = new CompRowMatrix(this.bindings.size(), this.bindings
				.size(), this.mxCoordIndex);
		this.edgeMatrix.zero();
		for (int i = 0; i < edgeWeights.length; i++) {
			for (int j = 0; j < edgeWeights[i].length; j++) {
				int actualCol = this.mxCoordIndex[i][j];
				this.edgeMatrix.set(i, actualCol, this.edgeWeights[i][j]);
			}
		}
	}

	/**
	 * Gets the {@link Binding} at the given index.
	 * 
	 * @param index
	 *            of the desired {@link Binding}
	 * @return {@link Binding} at the given index.
	 */
	public Binding getBinding(int index) {
		return this.bindings.get(index);
	}

	/**
	 * Gets the {@link Matrix} that represents the graph of {@link Binding}
	 * nodes.
	 * 
	 * @return Matrix representing this PairGraph.
	 */
	public Matrix getAdjacencyMatrix() {
		return this.edgeMatrix;
	}

	/**
	 * Calculates the percentage of the {@link Binding} adjacency matrix that
	 * has edge-weights of 0.
	 * 
	 * @return Percentage
	 */
	public double getPercentSparse() {
		double rv = 1;
		int edges = this.getNumberOfEdges();
		int nodes = this.getNumberOfNodes();
		// max edges = n(n-1)
		int maxPossibleEdges = (nodes * nodes) - nodes;
		if(maxPossibleEdges > 0) {
			rv = 1 - ((double)edges / (double)maxPossibleEdges);
		}
		return rv;
	}

	/**
	 * Gets the number of nodes in the PairGraph.
	 * 
	 * @return Number of graph nodes.
	 */
	public int getNumberOfNodes() {
		return this.bindings.size();
	}

	/**
	 * Gets the number of edges in this graph
	 * 
	 * @return number of edges
	 */
	public int getNumberOfEdges() {
		int edgeCount = 0;
		for (int i = 0; i < mxCoordIndex.length; i++) {
			// add an edge for each non-zero entry
			for(int j = 0; j < mxCoordIndex[i].length; j++) {
				if(edgeWeights[i][j] > 0) {
					edgeCount++;
				}
			}
		}
		return edgeCount;
	}

	/**
	 * Builds a string representation of the matrix coordinate index that can be
	 * used to lookup non-zero values in the sparse {@link Binding} adjacency
	 * matrix.
	 * 
	 * @return String representing the matrix coordinate index
	 */
	public String printRows() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mxCoordIndex.length; i++) {
			for (int j = 0; j < mxCoordIndex[i].length; j++) {
				sb.append(this.mxCoordIndex[i][j] + " ");
			}
			sb.append(String.format("%n"));
		}
		return sb.toString();
	}

	/**
	 * Builds a string representation of the partial edge-weight matrix that
	 * coincides with the partial index matrix
	 * 
	 * @return String representing the matrix coordinate index
	 */
	public String printWeights() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < edgeWeights.length; i++) {
			for (int j = 0; j < edgeWeights[i].length; j++) {
				sb.append(this.edgeWeights[i][j] + " ");
			}
			sb.append(String.format("%n"));
		}
		return sb.toString();
	}
}
