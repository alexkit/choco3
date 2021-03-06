/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.binary;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * A specific <code>Propagator</code> extension defining filtering algorithm for:
 * <br/>
 * <b>X =/= Y</b>
 * <br>where <i>X</i> and <i>Y</i> are <code>Variable</code> objects.
 * <br>
 * This <code>Propagator</code> defines the <code>propagate</code> and <code>awakeOnInst</code> methods. The other ones
 * throw <code>UnsupportedOperationException</code>.
 * <br/>
 * <br/>
 * <i>Based on Choco-2.1.1</i>
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @author Arnaud Malapert
 * @version 0.01, june 2010
 * @since 0.01
 */
public class PropNotEqualX_Y extends Propagator<IntVar> {

    IntVar x;
    IntVar y;

    @SuppressWarnings({"unchecked"})
    public PropNotEqualX_Y(IntVar x, IntVar y) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        //Principle : if v0 is instantiated and v1 is enumerated, then awakeOnInst(0) performs all needed pruning
        //Otherwise, we must check if we can remove the value from v1 when the bounds has changed.
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.INSTANTIATE.mask;
        }
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (x.isInstantiated()) {
            removeValV1();
        } else if (y.isInstantiated()) {
            removeValV0();
        } else if (x.getUB() < (y.getLB()) || (y.getUB()) < x.getLB()) {
            setPassive();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        // typical case: A=[1,4], B=[1,4] (bounded domains)
        // A instantiated to 3 => nothing can be done on B
        // then B dec supp to 3 => 3 can also be removed du to A = 3.
        propagate(0);
    }

    private void removeValV0() throws ContradictionException {
        if (x.removeValue(y.getValue(), aCause)
                || !x.contains(y.getValue())) {
            this.setPassive();
        }
    }

    private void removeValV1() throws ContradictionException {
        if (y.removeValue(x.getValue(), aCause)
                || !y.contains(x.getValue())) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() < y.getLB()) ||
                (y.getUB() < x.getLB()))
            return ESat.TRUE;
        else if (x.isInstantiated()
                && y.isInstantiated()
                && x.getValue() == y.getValue())
            return ESat.FALSE;
        else
            return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        bf.append("prop(").append(vars[0].getName()).append(".NEQ.").append(vars[1].getName()).append(")");
        return bf.toString();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        e.add(solver.getExplainer().getPropagatorActivation(this));
        Variable var = d.getVar();

        if (var.equals(x)) {
            // a deduction has been made on x ; this is related to y only
            y.explain(VariableState.DOM, e);
        } else if (var != null) {
            x.explain(VariableState.DOM, e);
        }
        // and the application of the current propagator
        e.add(aCause);
    }
}
