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

package solver.constraints.reification;

import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;
import util.ESat;

/**
 * Constraint representing the negation of a given constraint
 * does not filter but fails if the given constraint is satisfied
 * Can be used within any constraint
 *
 * Should not be called by the user
 *
 * @author Jean-Guillaume Fages
 * @since 15/05/2013
 */
public class PropOpposite extends Propagator {

	// constraint to negate
	Constraint original;

	public PropOpposite(Constraint original, Variable[] vars) {
		super(vars, PropagatorPriority.LINEAR, false);
		this.original = original;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		ESat op = original.isSatisfied();
		if(op == ESat.TRUE){
			contradiction(vars[0],"");
		}
		if(op == ESat.FALSE){
			setPassive();
		}
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	@Override
	public ESat isEntailed() {
		ESat op = original.isSatisfied();
		if(op == ESat.TRUE){
			return ESat.FALSE;
		}
		if(op == ESat.FALSE){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}