/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.Objects;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.time.AbsoluteDate;
import seak.conmop.util.Bounds;
import seak.orekit.object.Satellite;

/**
 * Variable for the satellite
 *
 * @author nhitomi
 */
public class SatelliteVariable implements Variable {

    private static final long serialVersionUID = -7659983869462960260L;

    /**
     * The bounds on the semi major axis
     */
    private final Bounds<Double> smaBound;

    /**
     * The bounds on the eccentricity
     */
    private final Bounds<Double> eccBound;

    /**
     * The bounds on the inclination
     */
    private final Bounds<Double> incBound;

    /**
     * The bounds on the argument of perigee
     */
    private final Bounds<Double> argPerBound;

    /**
     * The bounds on the right ascension of the ascending node
     */
    private final Bounds<Double> raanBound;

    /**
     * The bounds on the true anomaly
     */
    private final Bounds<Double> anomBound;

    private Double sma;

    private Double ecc;

    private Double inc;

    private Double argPer;

    private Double raan;

    private Double anom;

    /**
     * Creates a new variable for a satellite. Assumes all valid values are
     * allowed for argument of perigee, right ascension of the ascending node,
     * and true anomaly.
     *
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     */
    public SatelliteVariable(Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound) {
        this(smaBound, eccBound, incBound,
                new Bounds<>(0.0, 2. * Math.PI),
                new Bounds<>(0.0, 2. * Math.PI),
                new Bounds<>(0.0, 2. * Math.PI));
    }

    /**
     * Creates a new variable for a satellite.
     *
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     * @param argPerBound The bounds on the argument of perigee [rad]
     * @param raanBound The bounds on the right ascension of the ascending node
     * [rad]
     * @param anomBound The bounds on the true anomaly [rad]
     */
    public SatelliteVariable(
            Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound, Bounds<Double> argPerBound,
            Bounds<Double> raanBound, Bounds<Double> anomBound) {
        this.smaBound = smaBound;
        this.eccBound = eccBound;
        this.incBound = incBound;
        this.argPerBound = argPerBound;
        this.raanBound = raanBound;
        this.anomBound = anomBound;
        this.sma = Double.NaN;
        this.ecc = Double.NaN;
        this.inc = Double.NaN;
        this.argPer = Double.NaN;
        this.raan = Double.NaN;
        this.anom = Double.NaN;
    }

    /**
     * Copies the fields of the given satellite variable and creates a new
     * instance of a satellite.
     *
     * @param var the satellite variable to copy
     */
    protected SatelliteVariable(SatelliteVariable var) {
        this.smaBound = var.smaBound;
        this.eccBound = var.eccBound;
        this.incBound = var.incBound;
        this.argPerBound = var.argPerBound;
        this.raanBound = var.raanBound;
        this.anomBound = var.anomBound;
        this.sma = var.getSma();
        this.ecc = var.getEcc();
        this.inc = var.getInc();
        this.argPer = var.getArgPer();
        this.raan = var.getRaan();
        this.anom = var.getTrueAnomaly();
    }

    /**
     * Gets the semimajor axis [m]
     *
     * @return the semimajor axis [m]
     */
    public Double getSma() {
        return sma;
    }

    /**
     * Sets the semimajor axis [m]
     *
     * @param sma the semimajor axis [m]
     */
    public void setSma(Double sma) {
        if (smaBound.inBounds(sma)) {
            this.sma = sma;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given semi-major axis (%f)m is out of bounds ([%f,%f])m",
                    sma, smaBound.getLowerBound(), smaBound.getUpperBound()));
        }
    }

    /**
     * Sets the eccentricity
     *
     * @return the eccentricity
     */
    public Double getEcc() {
        return ecc;
    }

    /**
     * Gets the eccentricity
     *
     * @param ecc the eccentricity
     */
    public void setEcc(Double ecc) {
        if (eccBound.inBounds(ecc)) {
            this.ecc = ecc;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given eccentricity (%f) is out of bounds ([%f,%f])",
                    ecc, eccBound.getLowerBound(), eccBound.getUpperBound()));
        }
    }

    /**
     * Gets the inclination [rad]
     *
     * @return the inclination [rad]
     */
    public Double getInc() {
        return inc;
    }

    /**
     * Sets the inclination [rad]
     *
     * @param inc the inclination [rad]
     */
    public void setInc(Double inc) {
        if (incBound.inBounds(inc)) {
            this.inc = inc;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given inclination (%f)rad is out of bounds ([%f,%f])rad",
                    inc, incBound.getLowerBound(), incBound.getUpperBound()));
        }
    }

    /**
     * Gets the argument of perigee [rad]
     *
     * @return the argument of perigee [rad]
     */
    public Double getArgPer() {
        return argPer;
    }

    /**
     * Sets the argument of perigee [rad]
     *
     * @param argPer the argument of perigee [rad]
     */
    public void setArgPer(Double argPer) {
        if (argPerBound.inBounds(argPer)) {
            this.argPer = argPer;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given argument of perigee (%f)rad is out of bounds ([%f,%f])rad",
                    argPer, argPerBound.getLowerBound(), argPerBound.getUpperBound()));
        }
    }

    /**
     * Gets the right ascension of the ascending node [rad]
     *
     * @return the right ascension of the ascending node [rad]
     */
    public Double getRaan() {
        return raan;
    }

    /**
     * Sets the right ascension of the ascending node [rad]
     *
     * @param raan the right ascension of the ascending node [rad]
     */
    public void setRaan(Double raan) {
        if (raanBound.inBounds(raan)) {
            this.raan = raan;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given right ascension of the ascending node (%f)rad is out of bounds ([%f,%f])rad",
                    raan, raanBound.getLowerBound(), raanBound.getUpperBound()));
        }
    }

    /**
     * Gets the true anomaly [rad]
     *
     * @return the true anomaly [rad]
     */
    public Double getTrueAnomaly() {
        return anom;
    }

    /**
     * Sets the true anomaly [rad]
     *
     * @param anom the true anomaly [rad]
     */
    public void setTrueAnomaly(Double anom) {
        if (anomBound.inBounds(anom)) {
            this.anom = anom;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Given true anomaly (%f)rad is out of bounds ([%f,%f])rad",
                    anom, anomBound.getLowerBound(), anomBound.getUpperBound()));
        }
    }

    @Override
    public Variable copy() {
        return new SatelliteVariable(this);
    }

    @Override
    public void randomize() {
        this.setTrueAnomaly(PRNG.nextDouble(anomBound.getLowerBound(), anomBound.getUpperBound()));
        this.setArgPer(PRNG.nextDouble(argPerBound.getLowerBound(), argPerBound.getUpperBound()));
        this.setEcc(PRNG.nextDouble(eccBound.getLowerBound(), eccBound.getUpperBound()));
        this.setInc(PRNG.nextDouble(incBound.getLowerBound(), incBound.getUpperBound()));
        this.setRaan(PRNG.nextDouble(raanBound.getLowerBound(), raanBound.getUpperBound()));
        this.setSma(PRNG.nextDouble(smaBound.getLowerBound(), smaBound.getUpperBound()));
    }

    /**
     * Gets the bounds for an allowable semi-major axis [m]
     *
     * @return the bounds for an allowable semi-major axis [m]
     */
    public Bounds<Double> getSmaBound() {
        return smaBound;
    }

    /**
     * Gets the bounds for an allowable eccentricity
     *
     * @return the bounds for an allowable eccentricity
     */
    public Bounds<Double> getEccBound() {
        return eccBound;
    }

    /**
     * Gets the bounds for an allowable inclination [rad]
     *
     * @return the bounds for an allowable inclination [rad]
     */
    public Bounds<Double> getIncBound() {
        return incBound;
    }

    /**
     * Gets the bounds for an allowable argument of perigee [rad]
     *
     * @return the bounds for an allowable argument of perigee [rad]
     */
    public Bounds<Double> getArgPerBound() {
        return argPerBound;
    }

    /**
     * Gets the bounds for an allowable right ascension of the ascending node
     * [rad]
     *
     * @return the bounds for an allowable right ascension of the ascending node
     * [rad]
     */
    public Bounds<Double> getRaanBound() {
        return raanBound;
    }

    /**
     * Gets the bounds for an allowable true anomaly [rad]
     *
     * @return the bounds for an allowable true anomaly [rad]
     */
    public Bounds<Double> getAnomBound() {
        return anomBound;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.sma);
        hash = 89 * hash + Objects.hashCode(this.ecc);
        hash = 89 * hash + Objects.hashCode(this.inc);
        hash = 89 * hash + Objects.hashCode(this.argPer);
        hash = 89 * hash + Objects.hashCode(this.raan);
        hash = 89 * hash + Objects.hashCode(this.anom);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SatelliteVariable other = (SatelliteVariable) obj;
        if (!Objects.equals(this.sma, other.sma)) {
            return false;
        }
        if (!Objects.equals(this.ecc, other.ecc)) {
            return false;
        }
        if (!Objects.equals(this.inc, other.inc)) {
            return false;
        }
        if (!Objects.equals(this.argPer, other.argPer)) {
            return false;
        }
        if (!Objects.equals(this.raan, other.raan)) {
            return false;
        }
        if (!Objects.equals(this.anom, other.anom)) {
            return false;
        }
        return true;
    }

}
