/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import org.moeaframework.core.Variable;
import seak.conmop.util.Bounds;

/**
 * Variable for the satellite
 * @author nhitomi
 */
public class SatelliteVariable implements Variable {
    
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
    
    private final Double sma;
    
    private final Double ecc;
    
    private final Double inc;
    
    private final Double argPer;
    
    private final Double raan;
    
    private final Double anom;

    /**
     * Creates 
     * @param smaBound
     * @param eccBound
     * @param incBound
     * @param argPerBound
     * @param raanBound
     * @param anomBound 
     */
    public SatelliteVariable(Bounds<Double> smaBound, Bounds<Double> eccBound, 
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
    
    
    
    

    @Override
    public Variable copy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void randomize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
