/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.Objects;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import seak.conmop.util.Bounds;

/**
 * A satellite variable that has a boolean parameter that defines whether this
 * satellite should be manifested in a constellation. Used for a fixed-length
 * chromosome representation for constellations
 *
 * @author SEAK1
 */
public class BooleanSatelliteVariable extends SatelliteVariable {

    /**
     * boolean parameter that defines whether this satellite should be
     * manifested in a constellation
     */
    private Boolean manifest;

    /**
     * {@inheritDoc}
     * @param smaBound
     * @param eccBound
     * @param incBound 
     */
    public BooleanSatelliteVariable(Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound) {
        super(smaBound, eccBound, incBound);
        manifest = null;
    }

    public BooleanSatelliteVariable(Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound, Bounds<Double> argPerBound, Bounds<Double> raanBound, Bounds<Double> anomBound) {
        super(smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
        manifest = null;
    }
    
    public BooleanSatelliteVariable(SatelliteVariable var) {
        super(var);
        manifest = null;
    }

    public BooleanSatelliteVariable(BooleanSatelliteVariable var) {
        super(var);
        manifest = var.getManifest();
    }

    public Boolean getManifest() {
        return manifest;
    }

    public void setManifest(Boolean manifest) {
        this.manifest = manifest;
    }
    
    @Override
    public void randomize() {
        super.randomize();
        this.manifest = PRNG.nextBoolean();
    }

    @Override
    public Variable copy() {
        return new BooleanSatelliteVariable(this);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.manifest);
        hash = 89 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BooleanSatelliteVariable other = (BooleanSatelliteVariable) obj;
        if (!Objects.equals(this.manifest, other.manifest)) {
            return false;
        }
        return super.equals(obj);
    }

    
}
