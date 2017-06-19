/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.List;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import seak.conmop.util.Bounds;
import seak.conmop.util.Factor;

/**
 * Variable for a Walker constellation
 *
 * @author nhitomi
 */
public class WalkerVariable implements Variable {

    /**
     * The bounds allowable on the total number of satellites
     */
    private final Bounds<Integer> tBound;

    /**
     * The bounds allowable on the number of planes
     */
    private final Bounds<Integer> pBound;

    /**
     * The bounds allowable on the phasing value
     */
    private final Bounds<Integer> fBound;

    /**
     * The number of satellites
     */
    private Integer t;

    /**
     * The number of planes
     */
    private Integer p;

    /**
     * The phasing value
     */
    private Integer f;

    public WalkerVariable(Bounds<Integer> tBound, Bounds<Integer> pBound, Bounds<Integer> fBound) {
        this.tBound = tBound;
        this.pBound = pBound;
        this.fBound = fBound;
        this.t = null;
        this.p = null;
        this.f = null;
    }

    public WalkerVariable(int t, int p, int f, Bounds<Integer> tBound, Bounds<Integer> pBound, Bounds<Integer> fBound) {
        this.tBound = tBound;
        this.pBound = pBound;
        this.fBound = fBound;
        this.t = t;
        this.p = p;
        this.f = f;

        checkFeasible(t, p, f);
    }

    /**
     * Checks that the t, p, f values are valid
     */
    private boolean checkFeasible(int t, int p, int f) {
        if (t < 0 || p < 0 || f < 0) {
            throw new IllegalArgumentException(
                    String.format("Expected t/p/f values to be non-negative. "
                            + "Found %d/%d/%d", t, p, f));
        } else if (p > t) {
            throw new IllegalArgumentException(
                    String.format("Expected t >= p."
                            + "Found %d/%d/%d", t, p, f));
        } else if (!Factor.divisors(t).contains(p)) {
            throw new IllegalArgumentException(
                    String.format("Expected t to be divisible by p."
                            + "Found %d/%d/%d", t, p, f));
        } else if (f >= p) {
            throw new IllegalArgumentException(
                    String.format("Expected f < p."
                            + "Found %d/%d/%d", t, p, f));
        } else {
            return true;
        }
    }

    public int getT() {
        return t;
    }

    public int getP() {
        return p;
    }

    public int getF() {
        return f;
    }

    @Override
    public Variable copy() {
        return new WalkerVariable(t, p, f, tBound, pBound, fBound);
    }

    @Override
    public void randomize() {
        this.t = PRNG.nextInt(tBound.getLowerBound(), tBound.getUpperBound());
        List<Integer> possibleP = new ArrayList<>();
        for (int i = 0; i < Factor.divisors(this.t).size(); i++) {
            if ((Factor.divisors(this.t).get(i) >= pBound.getLowerBound())
                    && (Factor.divisors(this.t).get(i) <= pBound.getUpperBound())) {
                possibleP.add(Factor.divisors(this.t).get(i));
            }
        }
        this.p = possibleP.get(PRNG.nextInt(possibleP.size()));
        this.f = PRNG.nextInt(p);
    }

}
