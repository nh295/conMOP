/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.List;
import org.moeaframework.core.Variable;
import org.orekit.utils.Constants;
import seak.conmop.util.Bounds;
import seak.orekit.object.Constellation;
import seak.orekit.object.Satellite;

/**
 *
 * @author nhitomi
 */
public class ConstellationVariable implements Variable {

    private Integer numberOfSatellites;

    /**
     * The bounds on the number of satellites allowed in this constellation
     */
    private final Bounds<Integer> satelliteBound;

    /**
     * The bounds on the semi major axis [m]
     */
    private final Bounds<Double> smaBound;

    /**
     * The bounds on the eccentricity in the range [0,1]
     */
    private final Bounds<Double> eccBound;

    /**
     * The bounds on the inclination [rad]
     */
    private final Bounds<Double> incBound;

    /**
     * The bounds on the argument of perigee [rad]
     */
    private final Bounds<Double> argPerBound;

    /**
     * The bounds on the right ascension of the ascending node [rad]
     */
    private final Bounds<Double> raanBound;

    /**
     * The bounds on the true anomaly [rad]
     */
    private final Bounds<Double> anomBound;

    /**
     * Satellite variables
     */
    private final List<SatelliteVariable> satelliteVars;

    /**
     * Constructs a new constellation variable. The number of satellites is not
     * set by default. Satellites within the constellations are given null
     * values for their orbital parameters
     *
     * @param satelliteBound
     * @param ecc
     * @param inc
     * @param sma
     * @param argPer
     * @param raan
     * @param anom
     */
    public ConstellationVariable(Bounds<Integer> satelliteBound,
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> argPer, Bounds<Double> raan, Bounds<Double> anom) {
        this(null, satelliteBound,
                sma, ecc, inc, argPer, raan, anom);
    }

    /**
     *
     * @param constellation
     * @param satelliteBound
     * @param sma
     * @param ecc
     * @param inc
     * @param argPer
     * @param raan
     * @param anom
     */
    public ConstellationVariable(Constellation constellation, Bounds<Integer> satelliteBound,
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> argPer, Bounds<Double> raan, Bounds<Double> anom) {
        this.numberOfSatellites = constellation.getSatellites().size();
        this.satelliteBound = satelliteBound;
        this.smaBound = sma;
        this.eccBound = ecc;
        this.incBound = inc;
        this.argPerBound = argPer;
        this.raanBound = raan;
        this.anomBound = anom;
        this.satelliteVars = new ArrayList<>();
        for (Satellite sat : constellation.getSatellites()) {
            
        }
    }

    public ConstellationVariable(int numberOfSatellites, Bounds<Integer> satelliteBound,
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> argPer, Bounds<Double> raan, Bounds<Double> anom) {
        this.numberOfSatellites = numberOfSatellites;
        this.satelliteBound = satelliteBound;
        this.smaBound = sma;
        this.eccBound = ecc;
        this.incBound = inc;
        this.argPerBound = argPer;
        this.raanBound = raan;
        this.anomBound = anom;
        this.satelliteVars = new ArrayList<>();
    }
    
    private void checkBounds(Bounds<Integer> satelliteBound,
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> argPer, Bounds<Double> raan, Bounds<Double> anom){
        if(satelliteBound.getLowerBound() < 0){
            throw new IllegalArgumentException("Expected lower bound on the number of total satellites to be non-negative");
        }
        if(sma.getLowerBound() < Constants.WGS84_EARTH_EQUATORIAL_RADIUS){
            throw new IllegalArgumentException("Expected semi-major axis to be larger than earth radius (6378137.0 m)");
        }
        if(ecc.getLowerBound() < 0 || ecc.getUpperBound()> 1){
            throw new IllegalArgumentException(
                    String.format("Expected eccentriciy bounds to be in range [0,1]."
                            + " Found [%f,%f]",ecc.getLowerBound(), ecc.getUpperBound()));
        }
        if(inc.getLowerBound() < 0 || inc.getUpperBound()> 1){
            throw new IllegalArgumentException(
                    String.format("Expected inclination bounds to be in range [0,2pi]."
                            + " Found [%f,%f]",inc.getLowerBound(), inc.getUpperBound()));
        }
        if(argPer.getLowerBound() < 0 || argPer.getUpperBound()> 1){
            throw new IllegalArgumentException(
                    String.format("Expected argument of perigee bounds to be in range [0,2pi]."
                            + " Found [%f,%f]",argPer.getLowerBound(), argPer.getUpperBound()));
        }
        if(raan.getLowerBound() < 0 || raan.getUpperBound()> 1){
            throw new IllegalArgumentException(
                    String.format("Expected right ascension of the ascending node bounds to be in range [0,2pi]."
                            + " Found [%f,%f]",raan.getLowerBound(), raan.getUpperBound()));
        }
        if(anom.getLowerBound() < 0 || anom.getUpperBound()> 1){
            throw new IllegalArgumentException(
                    String.format("Expected true anomaly bounds to be in range [0,2pi]."
                            + " Found [%f,%f]",raan.getLowerBound(), raan.getUpperBound()));
        }
    }

    public final void setNumberOfSatellites(int n) {
        if ((n < satelliteBound.getLowerBound()) || (n > satelliteBound.getUpperBound())) {
            throw new IllegalArgumentException(
                    String.format("Given value is out of bounds "
                            + "(value: %d, min: %d, max: d)",
                            n, satelliteBound.getLowerBound(), satelliteBound.getUpperBound()));
        }
        this.numberOfSatellites = n;
    }

    public Constellation toConstellation() {
        for (SatelliteVariable sat : satelliteVars) {

        }
        return null;
    }

    @Override
    public Variable copy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void randomize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
