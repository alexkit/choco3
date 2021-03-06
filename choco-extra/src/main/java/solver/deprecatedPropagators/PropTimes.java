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

package solver.deprecatedPropagators;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.tools.MathUtils;

/**
 * V0 * V1 = V2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
@Deprecated // bug somewhere
public class PropTimes extends Propagator<IntVar> {

    protected static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;

    IntVar v0, v1, v2;

    public PropTimes(IntVar v1, IntVar v2, IntVar result) {
        super(new IntVar[]{v1, v2, result}, PropagatorPriority.TERNARY, true);
        this.v0 = vars[0];
        this.v1 = vars[1];
        this.v2 = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        filter(0, true, true);
        filter(1, true, true);
        filter(2, true, true);
    }

    @Override
    public final void propagate(int varIdx, int mask) throws ContradictionException {
        filter(varIdx, EventType.isInclow(mask), EventType.isDecupp(mask));//bug
    }

    @Override
    public final ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(v0.getValue() * v1.getValue() == v2.getValue());
        } else if (v2.isInstantiatedTo(0)) {
            if (v0.isInstantiatedTo(0) || v1.isInstantiatedTo(0)) {
                return ESat.TRUE;
            } else if (!(v0.contains(0)) && !(v1.contains(0))) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else if (!(v2.contains(0))) {
            if (v0.getUB() < getXminIfNonZero()) {
                return ESat.FALSE;
            } else if (v0.getLB() > getXmaxIfNonZero()) {
                return ESat.FALSE;
            } else if (v1.getUB() < getYminIfNonZero()) {
                return ESat.FALSE;
            } else if (v1.getLB() > getYmaxIfNonZero()) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.UNDEFINED;
        }
    }

    //****************************************************************************************************************//
    //****************************************************************************************************************//
    //****************************************************************************************************************//

    protected void filter(int idx, boolean lb, boolean ub) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.contains(0))) {
                lb = false;
                ub = false;
                if (lb) {
                    int r = Math.min(getZmax(), MAX);
                    lb = v2.updateUpperBound(r, aCause);
                }
                if (ub) {
                    int r = Math.max(getZmin(), MIN);
                    ub = v2.updateLowerBound(r, aCause);
                }
                if (lb || ub) {
                    filter(2, lb, ub);
                }
            }
        }
    }

    /**
     * reaction when X (v0) is updated
     *
     * @throws ContradictionException
     */
    protected void awakeOnX() throws ContradictionException {
        if (v0.isInstantiatedTo(0)) {
            v2.instantiateTo(0, aCause);
        }
        if ((v2.isInstantiatedTo(0)) && (!v0.contains(0))) {
            v1.instantiateTo(0, aCause);
        } else if (!v2.contains(0)) {
            updateYandX();
        } else if (!(v2.isInstantiatedTo(0))) {
            shaveOnYandX();
        }
        if (!(v2.isInstantiatedTo(0))) {
            int r = Math.max(getZmin(), MIN);
            v2.updateLowerBound(r, aCause);
            r = Math.min(getZmax(), MAX);
            v2.updateUpperBound(r, aCause);
        }
    }

    protected void awakeOnY() throws ContradictionException {
        if (v1.isInstantiatedTo(0)) {
            v2.instantiateTo(0, aCause);
        }
        if ((v2.isInstantiatedTo(0)) && (!v1.contains(0))) {
            v0.instantiateTo(0, aCause);
        } else if (!v2.contains(0)) {
            updateXandY();
        } else if (!(v2.isInstantiatedTo(0))) {
            shaveOnXandY();
        }
        if (!(v2.isInstantiatedTo(0))) {
            int r = Math.max(getZmin(), MIN);
            v2.updateLowerBound(r, aCause);
            r = Math.min(getZmax(), MAX);
            v2.updateUpperBound(r, aCause);
        }
    }

    protected void awakeOnZ() throws ContradictionException {
        if (!(v2.contains(0))) {
            updateX();
            if (updateY()) {
                updateXandY();
            }
        } else if (!(v2.isInstantiatedTo(0))) {
            shaveOnX();
            if (shaveOnY()) {
                shaveOnXandY();
            }
        }
        if (v2.isInstantiatedTo(0)) {
            propagateZero();
        }
    }


    private int getXminIfNonZero() {
        if ((v2.getLB() >= 0) && (v1.getLB() >= 0)) {
            return infCeilmM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getUB() <= 0)) {
            return infCeilMm(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getUB() <= 0)) {
            return infCeilMM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() >= 0)) {
            return infCeilmm(v2, v1);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getUB() <= 0)) {
            return infCeilMM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infCeilmP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() >= 0)) {
            return infCeilmm(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infCeilMN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infCeilxx(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int getXmaxIfNonZero() {
        if ((v2.getLB() >= 0) && (v1.getLB() >= 0)) {
            return supCeilMm(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getUB() <= 0)) {
            return supCeilmM(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getUB() <= 0)) {
            return supCeilmm(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() >= 0)) {
            return supCeilMM(v2, v1);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getUB() <= 0)) {
            return supCeilmM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supCeilmN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() >= 0)) {
            return supCeilMm(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supCeilMP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supCeilEq(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int getYminIfNonZero() {
        if ((v2.getLB() >= 0) && (v0.getLB() >= 0)) {
            return infCeilmM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getUB() <= 0)) {
            return infCeilMm(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getUB() <= 0)) {
            return infCeilMM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() >= 0)) {
            return infCeilmm(v2, v0);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getUB() <= 0)) {
            return infCeilMM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return infCeilmP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() >= 0)) {
            return infCeilmm(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return infCeilMN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return infCeilxx(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int getYmaxIfNonZero() {
        if ((v2.getLB() >= 0) && (v0.getLB() >= 0)) {
            return supCeilMm(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getUB() <= 0)) {
            return supCeilmM(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getUB() <= 0)) {
            return supCeilmm(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() >= 0)) {
            return supCeilMM(v2, v0);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getUB() <= 0)) {
            return supCeilmM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return supCeilmN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() >= 0)) {
            return supCeilMm(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return supCeilMP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return supCeilEq(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int getZmin() {
        if ((v0.getLB() >= 0) && (v1.getLB() >= 0)) {
            return infFloormm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getUB() <= 0)) {
            return infFloorMM(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getUB() <= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getUB() <= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infFloorxx(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int getZmax() {
        if ((v0.getLB() >= 0) && (v1.getLB() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getUB() <= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getUB() <= 0)) {
            return supFloormM(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() >= 0)) {
            return supFloorMm(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getUB() <= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supFloorEq(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int infFloormm(IntVar b, IntVar c) {
        return b.getLB() * c.getLB();
    }

    private int infFloormM(IntVar b, IntVar c) {
        return b.getLB() * c.getUB();
    }

    private int infFloorMm(IntVar b, IntVar c) {
        return b.getUB() * c.getLB();
    }

    private int infFloorMM(IntVar b, IntVar c) {
        return b.getUB() * c.getUB();
    }

    private int supFloormm(IntVar b, IntVar c) {
        return b.getLB() * c.getLB();
    }

    private int supFloormM(IntVar b, IntVar c) {
        return b.getLB() * c.getUB();
    }

    private int supFloorMm(IntVar b, IntVar c) {
        return b.getUB() * c.getLB();
    }

    private int supFloorMM(IntVar b, IntVar c) {
        return b.getUB() * c.getUB();
    }

    private int getNonZeroSup(IntVar v) {
        return Math.min(v.getUB(), -1);
    }

    private int getNonZeroInf(IntVar v) {
        return Math.max(v.getLB(), 1);
    }

    private int infCeilmm(IntVar b, IntVar c) {
        return MathUtils.divCeil(b.getLB(), getNonZeroInf(c));
    }

    private int infCeilmM(IntVar b, IntVar c) {
        return MathUtils.divCeil(getNonZeroInf(b), c.getUB());
    }

    private int infCeilMm(IntVar b, IntVar c) {
        return MathUtils.divCeil(getNonZeroSup(b), c.getLB());
    }

    private int infCeilMM(IntVar b, IntVar c) {
        return MathUtils.divCeil(b.getUB(), getNonZeroSup(c));
    }

    private int infCeilmP(IntVar b) {
        return MathUtils.divCeil(b.getLB(), 1);
    }

    private int infCeilMN(IntVar b) {
        return MathUtils.divCeil(b.getUB(), -1);
    }

    private int supCeilmm(IntVar b, IntVar c) {
        return MathUtils.divFloor(getNonZeroInf(b), c.getLB());
    }

    private int supCeilmM(IntVar b, IntVar c) {
        return MathUtils.divFloor(b.getLB(), getNonZeroSup(c));
    }

    private int supCeilMm(IntVar b, IntVar c) {
        return MathUtils.divFloor(b.getUB(), getNonZeroInf(c));
    }

    private int supCeilMM(IntVar b, IntVar c) {
        return MathUtils.divFloor(getNonZeroSup(b), c.getUB());
    }

    private int supCeilmN(IntVar b) {
        return MathUtils.divFloor(b.getLB(), -1);
    }

    private int supCeilMP(IntVar b) {
        return MathUtils.divFloor(b.getUB(), 1);
    }

    private int infFloorxx(IntVar b, IntVar c) {
        int s1 = b.getLB() * c.getUB();
        int s2 = b.getUB() * c.getLB();
        if (s1 < s2) {
            return s1;
        } else {
            return s2;
        }
    }

    private int supFloorEq(IntVar b, IntVar c) {
        int l1 = b.getLB() * c.getLB();
        int l2 = b.getUB() * c.getUB();
        if (l1 > l2) {
            return l1;
        } else {
            return l2;
        }
    }

    private int infCeilxx(IntVar b) {
        return Math.min(MathUtils.divCeil(b.getLB(), 1), MathUtils.divCeil(b.getUB(), -1));
    }  //v0.18

    private int supCeilEq(IntVar b) {
        return Math.max(MathUtils.divFloor(b.getLB(), -1), MathUtils.divFloor(b.getUB(), 1));
    }   //v0.18


    /**
     * propagate the fact that v2 (Z) is instantiateTod to 0
     *
     * @throws ContradictionException
     */
    protected final void propagateZero() throws ContradictionException {
        if (!(v1.contains(0))) {
            v0.instantiateTo(0, aCause);
        }
        if (!(v0.contains(0))) {
            v1.instantiateTo(0, aCause);
        }
    }

    /**
     * Updating X and Y when Z cannot be 0
     */
    protected boolean updateX() throws ContradictionException {
        int r = Math.max(getXminIfNonZero(), MIN);
        boolean infChange = v0.updateLowerBound(r, aCause);
        r = Math.min(getXmaxIfNonZero(), MAX);
        boolean supChange = v0.updateUpperBound(r, aCause);
        return (infChange || supChange);
    }

    protected boolean updateY() throws ContradictionException {
        int r = Math.max(getYminIfNonZero(), MIN);
        boolean infChange = v1.updateLowerBound(r, aCause);
        r = Math.min(getYmaxIfNonZero(), MAX);
        boolean supChange = v1.updateUpperBound(r, aCause);
        return (infChange || supChange);
    }

    /**
     * loop until a fix point is reach (see testProd14)
     */
    protected final void updateXandY() throws ContradictionException {
        while (updateX() && updateY()) ;
    }

    protected final void updateYandX() throws ContradictionException {
        while (updateY() && updateX()) ;
    }

    /**
     * Updating X and Y when Z can  be 0
     */

    protected boolean shaveOnX() throws ContradictionException {
        int xmin = Math.max(getXminIfNonZero(), MIN);
        int xmax = Math.min(getXmaxIfNonZero(), MAX);
        if ((xmin > v0.getUB()) || (xmax < v0.getLB())) {
            v2.instantiateTo(0, aCause);
            propagateZero();    // make one of X,Y be 0 if the other cannot be
            return false;       //no more shaving need to be performed
        } else {
            boolean infChange = (!(v1.contains(0)) && v0.updateLowerBound(Math.min(0, xmin), aCause));
            boolean supChange = (!(v1.contains(0)) && v0.updateUpperBound(Math.max(0, xmax), aCause));
            return (infChange || supChange);
        }
    }

    protected boolean shaveOnY() throws ContradictionException {
        int ymin = Math.max(getYminIfNonZero(), MIN);
        int ymax = Math.min(getYmaxIfNonZero(), MAX);
        if ((ymin > v1.getUB()) || (ymax < v1.getLB())) {
            v2.instantiateTo(0, aCause);
            propagateZero();    // make one of X,Y be 0 if the other cannot be
            return false;       //no more shaving need to be performed
        } else {
            boolean infChange = (!(v0.contains(0)) && v1.updateLowerBound(Math.min(0, ymin), aCause));
            boolean supChange = (!(v0.contains(0)) && v1.updateUpperBound(Math.max(0, ymax), aCause));
            return (infChange || supChange);
        }
    }

    protected final void shaveOnXandY() throws ContradictionException {
        while (shaveOnX() && shaveOnY()) {
        }
    }

    protected final void shaveOnYandX() throws ContradictionException {
        while (shaveOnY() && shaveOnX()) {
        }
    }

}
