/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import java.util.ArrayList;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import seak.conmop.variable.BooleanSatelliteVariable;
import seak.conmop.variable.ConstellationMatrix;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 * This repair operator modifies a constellation such that the number of
 * satellites in the constellation remain in the properly specified bounds
 *
 * @author nozomihitomi
 */
public class RepairNumberOfSatellites implements Variation {

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution child = parents[0].copy();
        for (int i = 0; i < child.getNumberOfVariables(); i++) {
            Variable var = child.getVariable(i);
            if (var instanceof ConstellationMatrix) {
                ConstellationMatrix constelVar = (ConstellationMatrix)var;
                //check if the number of satellites is within the allowable bounds
                while (constelVar.getNumberOfSatellites() < constelVar.getSatelliteBound().getLowerBound()) {
                    //turn on a random satellite that is off
                    ArrayList<BooleanSatelliteVariable> offSats = new ArrayList<>();
                    for (SatelliteVariable sat : constelVar.getSatelliteVariables()) {
                        if (!((BooleanSatelliteVariable) sat).getManifest()) {
                            offSats.add((BooleanSatelliteVariable) sat);
                        }
                    }
                    int select = PRNG.nextInt(offSats.size());
                    offSats.get(select).setManifest(true);
                }
                while (constelVar.getNumberOfSatellites() > constelVar.getSatelliteBound().getUpperBound()) {
                    //turn off a random satellite that is on
                    ArrayList<BooleanSatelliteVariable> onSats = new ArrayList<>();
                    for (SatelliteVariable sat : constelVar.getSatelliteVariables()) {
                        if (((BooleanSatelliteVariable) sat).getManifest()) {
                            onSats.add((BooleanSatelliteVariable) sat);
                        }
                    }
                    int select = PRNG.nextInt(onSats.size());
                    onSats.get(select).setManifest(false);
                }
            } else if(var instanceof ConstellationVariable){
                ConstellationVariable constelVar = (ConstellationVariable)var;
                //check if the number of satellites is within the allowable bounds
                if (constelVar.getNumberOfSatellites() < constelVar.getSatelliteBound().getLowerBound()) {

                } 
                while(constelVar.getNumberOfSatellites() > constelVar.getSatelliteBound().getUpperBound()) {
                    //remove a random sallite from the chromosome
                    ArrayList<SatelliteVariable> sats = new ArrayList(constelVar.getSatelliteVariables());
                    sats.remove(PRNG.nextInt(sats.size()));
                    constelVar.setSatelliteVariables(sats);
                }
            }
        }
        return new Solution[]{child};
    }

}
