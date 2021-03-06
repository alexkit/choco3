/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.integer;

import samples.AbstractProblem;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.ICF;
import solver.search.loop.monitors.IMonitorInitPropagation;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.solution.Solution;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VF;

/**
 * simple CP model to solve a toy SMPTSP instance
 * (see Fages and Lapègue, CP'13 or Artificial Intelligence journal)
 * Enumeration of all optimal solutions
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class SMPTSP extends AbstractProblem {

	// ***********************************************************************************
	// VARIABLES
	// ***********************************************************************************

	//input
	private int nbTasks;
	private int nbAvailableShifts;
	private int bestObj;

	// model
	private IntVar nbValues;
	private IntVar[] assignment;

	// ***********************************************************************************
	// METHODS
	// ***********************************************************************************

	@Override
	public void createSolver() {
		solver = new Solver("Shift Minimization Personnel Task Scheduling Problem");
	}

	@Override
	public void buildModel() {
		// Input
		nbTasks = 5;
		nbAvailableShifts = 5;
		int[][] skilledShifts = new int[][]{{2,3,4}, {1,2,3}, {1,3}, {3,4,5}, {1,2,5}};
		final boolean[][] taskOverlaps = new boolean[][]{
				{true, true, true, true, false},
				{true, true, true, false, false},
				{true, true, true, true, false},
				{true, false, true, true, true},
				{false, false, false, true, true},
		};

		// Variables
		nbValues = VF.bounded("nb shifts", 0, nbAvailableShifts, solver);
		assignment = new IntVar[nbTasks];
		for(int i=0;i<nbTasks;i++){
			assignment[i] = VF.enumerated("t" + (i+1), skilledShifts[i], solver);
		}

		// Constraints
		for (int t1 = 0; t1 < nbTasks; t1++) {
			for (int t2 = t1+1; t2 < nbTasks; t2++) {
				if(taskOverlaps[t1][t2]){
					solver.post(ICF.arithm(assignment[t1],"!=",assignment[t2]));
				}
			}
		}
		solver.post(ICF.nvalues(assignment,nbValues,"AMNV<Gci|MDRk|R13>"));
	}

	@Override
	public void configureSearch() {
		// bottom-up optimisation, then classical branching
		solver.set(ISF.lexico_LB(nbValues), ISF.minDom_LB(assignment));
		// displays the root lower bound
		solver.plugMonitor(new IMonitorInitPropagation() {
			public void beforeInitialPropagation() {
			}

			public void afterInitialPropagation() {
				System.out.println("bound after initial propagation : " + nbValues);
			}
		});
		solver.plugMonitor(new IMonitorSolution() {
			@Override
			public void onSolution() {
				bestObj = nbValues.getValue();
				System.out.println("Solution found! Objective = "+bestObj);
			}
		});
	}

	@Override
	public void solve() {
		solver.findAllOptimalSolutions(ResolutionPolicy.MINIMIZE, nbValues, false);
	}

	@Override
	public void prettyOut() {
		int nb = 1;
		for(Solution s:solver.getSolutionRecorder().getSolutions()){
			System.out.println("Optimal solution : "+nb);
			for(int i=0;i<5;i++){
				System.out.println(assignment[i].getName()+" = "+s.getIntVal(assignment[i]));
			}nb++;
		}
	}

	public static void main(String[] args){
	    new SMPTSP().execute(args);
	}
}
