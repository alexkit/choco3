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

package solver.constraints.ternary;

import solver.constraints.Constraint;
import solver.exception.SolverException;
import solver.variables.IntVar;

/**
 * X*Y = Z
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class Times extends Constraint {

    private static boolean inIntBounds(IntVar x, IntVar y) {
        boolean l1 = inIntBounds((long) x.getLB() * (long) y.getLB());
        boolean l2 = inIntBounds((long) x.getUB() * (long) y.getLB());
        boolean l3 = inIntBounds((long) x.getLB() * (long) y.getUB());
        boolean l4 = inIntBounds((long) x.getUB() * (long) y.getUB());
        return l1 && l2 && l3 && l4;
    }

    private static boolean inIntBounds(long l1) {
        return l1 > Integer.MIN_VALUE && l1 < Integer.MAX_VALUE;
    }

    public Times(IntVar v1, IntVar v2, IntVar result) {
        super("Times",new PropTimesNaive(v1,v2,result));
        if (!inIntBounds(v1, v2)) {
            throw new SolverException("Integer overflow.\nConsider reducing the variable domains.");
        }
    }
}
