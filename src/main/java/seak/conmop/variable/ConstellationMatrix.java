/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.Collection;
import seak.conmop.util.Bounds;

/**
 *
 * @author nhitomi
 */
public class ConstellationMatrix extends ConstellationVariable {

    public ConstellationMatrix(Bounds<Integer> satelliteBound, Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound) {
        super(satelliteBound, smaBound, eccBound, incBound);
    }

    public ConstellationMatrix(Bounds<Integer> satelliteBound, Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound, Bounds<Double> argPerBound, Bounds<Double> raanBound, Bounds<Double> anomBound) {
        super(satelliteBound, smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
    }

    public ConstellationMatrix(Bounds<Integer> satelliteBound, Collection<SatelliteVariable> satellites) {
        super(satelliteBound, satellites);
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
    }
}
