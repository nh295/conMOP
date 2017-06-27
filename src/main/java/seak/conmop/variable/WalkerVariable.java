/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.List;
import org.hipparchus.util.FastMath;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import seak.conmop.util.Bounds;
import seak.conmop.util.Factor;

/**
 * Variable for a Walker constellation
 *
 * @author nhitomi
 */
public class WalkerVariable extends ConstellationVariable {

    private static final long serialVersionUID = 4317728591290279354L;

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

    /**
     * The semi-major axis of the Walker constellation [m]
     */
    private Double sma;

    /**
     * The inclination of the Walker constellation [rad]
     */
    private Double inc;

    /**
     * Initializes an empty Walker constellation (t=0, p=0, f=0) and declares
     * the allowable bounds on t, p, and f
     *
     * @param smaBound the bounds on the semi-major axis [m]
     * @param incBound the bounds on the inclination [rad]
     * @param tBound the bounds on the number of satellites allowed in the
     * constellation
     * @param pBound the bounds on the number of planes allowed in the
     * constellation
     * @param fBound the bounds on the phasing number allowed for the
     * constellation
     */
    public WalkerVariable(Bounds<Double> smaBound, Bounds<Double> incBound,
            Bounds<Integer> tBound, Bounds<Integer> pBound, Bounds<Integer> fBound) {
        super(tBound, smaBound, new Bounds(0.0, 0.0), incBound);
        this.tBound = tBound;
        this.pBound = pBound;
        this.fBound = fBound;
        this.sma = null;
        this.inc = null;
    }

    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected WalkerVariable(WalkerVariable var) {
        super(var);
        this.tBound = var.tBound;
        this.pBound = var.pBound;
        this.fBound = var.fBound;
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

    /**
     * Sets the t, p, and f values that define the Walker constellation. The
     * position of all satellites are based on the anchor satellite. This method
     * assumes that the right ascension of the ascending node and the true
     * anomaly of the anchor satellite is 0.0 radians.
     *
     * @param sma the semi-major axis of the Walker constellation [m[
     * @param inc the inclination of the Walker constellation [rad]
     * @param t the total number of satellites in the constellation
     * @param p the number of planes in the constellation. p must be a divisor
     * of t
     * @param f the phasing value. f must be in the range [0,p-1].
     */
    public final void setWalker(double sma, double inc, int t, int p, int f) {
        setWalker(sma, inc, t, p, f, 0.0, 0.0);
    }

    /**
     * Sets the t, p, and f values that define the Walker constellation. The
     * position of all satellites are based on the anchor satellite.
     *
     * @param sma the semi-major axis of the Walker constellation [m[
     * @param inc the inclination of the Walker constellation [rad]
     * @param t the total number of satellites in the constellation
     * @param p the number of planes in the constellation. p must be a divisor
     * of t
     * @param f the phasing value. f must be in the range [0,p-1].
     * @param refRaan the right ascension of the ascending node [rad] of the
     * anchor satellite
     * @param refAnom the true anomaly [rad] of the anchor satellite
     */
    public final void setWalker(double sma, double inc, int t, int p, int f, double refRaan, double refAnom) {
        checkFeasible(t, p, f);

        //Uses Walker delta pattern
        final int s = t / p; //number of satellites per plane
        final double pu = 2 * FastMath.PI / t; //pattern unit
        final double delAnom = pu * p; //in plane spacing between satellites
        final double delRaan = pu * s; //node spacing
        final double phasing = pu * f;

        final ArrayList<SatelliteVariable> satelliteVariables = new ArrayList(t);
        for (int planeNum = 0; planeNum < p; planeNum++) {
            for (int satNum = 0; satNum < s; satNum++) {
                //since eccentricity = 0, doesn't matter if using true or mean anomaly
                SatelliteVariable var = createSatelliteVariable();
                var.setSma(sma);
                var.setEcc(0.0);
                var.setInc(inc);
                var.setArgPer(0.0);
                var.setRaan(refRaan + planeNum * delRaan);
                var.setTrueAnomaly(refAnom + satNum * delAnom + phasing * planeNum);
                satelliteVariables.add(var);
            }
        }
        setSatelliteVariables(satelliteVariables);
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
        return new WalkerVariable(this);
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
