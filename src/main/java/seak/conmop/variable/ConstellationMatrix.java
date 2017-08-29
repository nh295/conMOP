/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.Collection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import seak.conmop.operators.RepairNumberOfSatellites;
import seak.conmop.util.Bounds;

/**
 *
 * @author nhitomi
 */
public class ConstellationMatrix extends ConstellationVariable {

    private static final long serialVersionUID = -3666761376685654535L;

    public ConstellationMatrix(Bounds<Integer> satelliteBound, Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound) {
        super(satelliteBound, smaBound, eccBound, incBound);
    }

    public ConstellationMatrix(Bounds<Integer> satelliteBound, Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound, Bounds<Double> argPerBound, Bounds<Double> raanBound, Bounds<Double> anomBound) {
        super(satelliteBound, smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
    }

    public ConstellationMatrix(Bounds<Integer> satelliteBound, Collection<SatelliteVariable> satellites) {
        super(satelliteBound, satellites);
    }
    
    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected ConstellationMatrix(ConstellationMatrix var) {
        this(var.getSatelliteBound(),
                var.getSmaBound(), var.getEccBound(), var.getIncBound(),
                var.getArgPerBound(), var.getRaanBound(), var.getAnomBound());
        this.setSatelliteVariables(var.getSatelliteVariables());
    }

    @Override
    public SatelliteVariable createSatelliteVariable() {
        return new BooleanSatelliteVariable(super.createSatelliteVariable());
    }

    @Override
    public void randomize() {
        ArrayList<SatelliteVariable> sats = new ArrayList();
        for (int i = 0; i < super.getSatelliteBound().getUpperBound(); i++) {
            SatelliteVariable var = createSatelliteVariable();
            var.randomize();
            sats.add(var);
        }
        super.setSatelliteVariables(sats);
        
        //repair variable to fit the bounds ont he number of satellites
        RepairNumberOfSatellites repair = new RepairNumberOfSatellites();
        Solution tmp = new Solution(1, 0);
        tmp.setVariable(0, this);
        repair.evolve(new Solution[]{tmp});
    }

    /**
     * Gets the number of satellites that are actively on in the constellation
     * matrix
     *
     * @return
     */
    @Override
    public int getNumberOfSatellites() {
        int count = 0;
        for (SatelliteVariable sat : getSatelliteVariables()) {
            if (((BooleanSatelliteVariable) sat).getManifest()) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public Variable copy() {
        return new ConstellationMatrix(this);
    }

}
