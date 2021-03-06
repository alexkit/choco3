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
package solver.search.strategy.decision;


import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/06/12
 */
public class RootDecision extends Decision {
    public static RootDecision ROOT = new RootDecision();

    // FOR SERIALIZATION
    private Object readResolve() {
        return ROOT;
    }

    @Override
    public Variable getDecisionVariable() {
        return null;
    }

    @Override
    public Object getDecisionValue() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void buildNext() {
    }

    @Override
    public void rewind() {
    }


    @Override
    public void apply() throws ContradictionException {
    }

    @Override
    public void setPrevious(Decision decision) {
    }

    @Override
    public Decision getPrevious() {
        return null;
    }


    @Override
    public void free() {
    }

    @Override
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
    }

    @Override
    public Deduction getNegativeDeduction() {
        return null;
    }

    @Override
    public Deduction getPositiveDeduction() {
        return null;
    }

    @Override
    public String toString() {
        return "ROOT";
    }
}
