/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import java.util.ArrayList;
import java.util.HashMap;
import org.hipparchus.util.FastMath;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.RealVariable;
import seak.conmop.variable.BooleanSatelliteVariable;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 *
 * @author nhitomi
 */
public class StaticOrbitElementOperator implements Variation {

    private final Variation operator;

    public StaticOrbitElementOperator(Variation operator) {
        this.operator = operator;
    }

    @Override
    public int getArity() {
        return operator.getArity();
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        //copy the solutions
        Solution[] children = new Solution[parents.length];
        for (int i = 0; i < parents.length; i++) {
            children[i] = parents[i].copy();
        }

        for (int i = 0; i < children[0].getNumberOfVariables(); i++) {
            //if the solution is composed of constellation variables
            boolean constelVariables = true;
            for (int j = 0; j < parents.length; j++) {
                if (!(children[j].getVariable(i) instanceof ConstellationVariable)) {
                    constelVariables = false;
                    break;
                }
            }
            boolean satVariables = true;
            for (int j = 0; j < parents.length; j++) {
                if (!(children[j].getVariable(i) instanceof SatelliteVariable)) {
                    satVariables = false;
                    break;
                }
            }
            if (constelVariables) {
                ConstellationVariable[] input = new ConstellationVariable[children.length];
                for (int j = 0; j < children.length; j++) {
                    input[j] = (ConstellationVariable) children[j].getVariable(i);
                }
                ConstellationVariable[] output = evolve(input);
                for (int j = 0; j < children.length; j++) {
                    children[j].setVariable(i, output[j]);
                }
            } else if (satVariables) {
                //if the solution is composed of satellite variables
                SatelliteVariable[] input = new SatelliteVariable[children.length];
                for (int j = 0; j < children.length; j++) {
                    input[j] = (SatelliteVariable) children[j].getVariable(i);
                }
                SatelliteVariable[] output = evolve(input);
                for (int j = 0; j < children.length; j++) {
                    children[j].setVariable(i, output[j]);
                }
            }
        }
        return children;
    }

    /**
     * Constellations can have the same number of satellites. If they do not,
     * then a number of satellites equal to the number of satellites in the
     * smallest constellations will be grouped randomly with satellites from the
     * other constellations and crossed using the given operation
     *
     * @param constellations constellations to recombine
     * @return recombined constellation variables
     */
    private ConstellationVariable[] evolve(ConstellationVariable[] constellations) {
        //find the minimum number of satellites contained in any of the constellations
        int minNSats = Integer.MAX_VALUE;
        for (int i = 0; i < constellations.length; i++) {
            minNSats = FastMath.min(minNSats, constellations[i].getSatelliteVariables().size());
        }

        //find which varibles should be included in search.
        //variables with lower bound == upperbound are not included
        HashMap<String, Integer> variableLocus = new HashMap();
        //assume that each satellite variable has the same upper and lower bounds
        SatelliteVariable repSat = constellations[0].createSatelliteVariable();
        int locusIndex = 0;
        if (!repSat.getSmaBound().getLowerBound().equals(repSat.getSmaBound().getUpperBound())) {
            variableLocus.put("sma", locusIndex);
            locusIndex++;
        }
        if (!repSat.getEccBound().getLowerBound().equals(repSat.getEccBound().getUpperBound())) {
            variableLocus.put("ecc", locusIndex);
            locusIndex++;
        }
        if (!repSat.getIncBound().getLowerBound().equals(repSat.getIncBound().getUpperBound())) {
            variableLocus.put("inc", locusIndex);
            locusIndex++;
        }
        if (!repSat.getArgPerBound().getLowerBound().equals(repSat.getArgPerBound().getUpperBound())) {
            variableLocus.put("ap", locusIndex);
            locusIndex++;
        }
        if (!repSat.getRaanBound().getLowerBound().equals(repSat.getRaanBound().getUpperBound())) {
            variableLocus.put("raan", locusIndex);
            locusIndex++;
        }
        if (!repSat.getAnomBound().getLowerBound().equals(repSat.getAnomBound().getUpperBound())) {
            variableLocus.put("ta", locusIndex);
        }

        //create the vector representation of the constellation
        Solution[] parents = new Solution[constellations.length];
        for (int i = 0; i < constellations.length; i++) {
            Solution parent = new Solution((variableLocus.size() + 1) * constellations[i].getSatelliteVariables().size(), 0);
            int satCount = 0;
            for (SatelliteVariable sat : constellations[i].getSatelliteVariables()) {
                if (variableLocus.containsKey("sma")) {
                    parent.setVariable(satCount + variableLocus.get("sma"),
                            new RealVariable(sat.getSma(), sat.getSmaBound().getLowerBound(), sat.getSmaBound().getUpperBound()));
                }
                if (variableLocus.containsKey("ecc")) {
                    parent.setVariable(satCount + variableLocus.get("ecc"),
                            new RealVariable(sat.getEcc(), sat.getEccBound().getLowerBound(), sat.getEccBound().getUpperBound()));
                }
                if (variableLocus.containsKey("inc")) {
                    parent.setVariable(satCount + variableLocus.get("inc"),
                            new RealVariable(sat.getInc(), sat.getIncBound().getLowerBound(), sat.getIncBound().getUpperBound()));
                }
                if (variableLocus.containsKey("ap")) {
                    parent.setVariable(satCount + variableLocus.get("ap"),
                            new RealVariable(sat.getArgPer(), sat.getArgPerBound().getLowerBound(), sat.getArgPerBound().getUpperBound()));
                }
                if (variableLocus.containsKey("raan")) {
                    parent.setVariable(satCount + variableLocus.get("raan"),
                            new RealVariable(sat.getRaan(), sat.getRaanBound().getLowerBound(), sat.getRaanBound().getUpperBound()));
                }
                if (variableLocus.containsKey("ta")) {
                    parent.setVariable(satCount + variableLocus.get("ta"),
                            new RealVariable(sat.getTrueAnomaly(), sat.getAnomBound().getLowerBound(), sat.getAnomBound().getUpperBound()));
                }
                BinaryVariable manifest = new BinaryVariable(1);
                manifest.set(0, ((BooleanSatelliteVariable) sat).getManifest());
                parent.setVariable(satCount + variableLocus.size(), manifest);
                satCount += variableLocus.size() + 1;
            }
            parents[i] = parent;
        }

        Solution[] children = operator.evolve(parents);

        ConstellationVariable[] out = constellations;
        for (int i = 0; i < children.length; i++) {
            ArrayList<SatelliteVariable> satList = new ArrayList<>();
            int satCount = 0;
            Solution child = children[i];
            for (SatelliteVariable sat : constellations[i].getSatelliteVariables()) {
                BooleanSatelliteVariable satVar = (BooleanSatelliteVariable) sat;
                if (variableLocus.containsKey("sma")) {
                    satVar.setSma(((RealVariable) child.getVariable(satCount + variableLocus.get("sma"))).getValue());
                }
                if (variableLocus.containsKey("ecc")) {
                    satVar.setEcc(((RealVariable) child.getVariable(satCount + variableLocus.get("ecc"))).getValue());
                }
                if (variableLocus.containsKey("inc")) {
                    satVar.setInc(((RealVariable) child.getVariable(satCount + variableLocus.get("inc"))).getValue());
                }
                if (variableLocus.containsKey("ap")) {
                    satVar.setArgPer(((RealVariable) child.getVariable(satCount + variableLocus.get("ap"))).getValue());
                }
                if (variableLocus.containsKey("raan")) {
                    satVar.setRaan(((RealVariable) child.getVariable(satCount + variableLocus.get("raan"))).getValue());
                }
                if (variableLocus.containsKey("ta")) {
                    satVar.setTrueAnomaly(((RealVariable) child.getVariable(satCount + variableLocus.get("ta"))).getValue());
                }
                satVar.setManifest(((BinaryVariable) child.getVariable(satCount + variableLocus.size())).get(0));
                satCount += variableLocus.size() + 1;
                satList.add(satVar);
            }
            out[i].setSatelliteVariables(satList);
        }
        return out;
    }

    /**
     * Operates on the real-valued orbital elements with the given operator
     *
     * @param satellites The satellite variables to operate on
     * @return the modified satellite variables. They are new instances
     */
    private SatelliteVariable[] evolve(SatelliteVariable[] satellites) {
        Solution[] parents = new Solution[satellites.length];

        //find which varibles should be included in search.
        //variables with lower bound == upperbound are not included
        HashMap<String, Integer> variableLocus = new HashMap();
        //assume that each satellite variable has the same upper and lower bounds
        SatelliteVariable repSat = satellites[0];
        int locusIndex = 0;
        if (!repSat.getSmaBound().getLowerBound().equals(repSat.getSmaBound().getUpperBound())) {
            variableLocus.put("sma", locusIndex);
            locusIndex++;
        }
        if (!repSat.getEccBound().getLowerBound().equals(repSat.getEccBound().getUpperBound())) {
            variableLocus.put("ecc", locusIndex);
            locusIndex++;
        }
        if (!repSat.getIncBound().getLowerBound().equals(repSat.getIncBound().getUpperBound())) {
            variableLocus.put("inc", locusIndex);
            locusIndex++;
        }
        if (!repSat.getArgPerBound().getLowerBound().equals(repSat.getArgPerBound().getUpperBound())) {
            variableLocus.put("ap", locusIndex);
            locusIndex++;
        }
        if (!repSat.getRaanBound().getLowerBound().equals(repSat.getRaanBound().getUpperBound())) {
            variableLocus.put("raan", locusIndex);
            locusIndex++;
        }
        if (!repSat.getAnomBound().getLowerBound().equals(repSat.getAnomBound().getUpperBound())) {
            variableLocus.put("ta", locusIndex);
        }

        for (int i = 0; i < satellites.length; i++) {
            Solution parent = new Solution(7, 0);
            SatelliteVariable sat = satellites[i];

            if (variableLocus.containsKey("sma")) {
                parent.setVariable(variableLocus.get("sma"),
                        new RealVariable(sat.getSma(), sat.getSmaBound().getLowerBound(), sat.getSmaBound().getUpperBound()));
            }
            if (variableLocus.containsKey("ecc")) {
                parent.setVariable(variableLocus.get("ecc"),
                        new RealVariable(sat.getEcc(), sat.getEccBound().getLowerBound(), sat.getEccBound().getUpperBound()));
            }
            if (variableLocus.containsKey("inc")) {
                parent.setVariable(variableLocus.get("inc"),
                        new RealVariable(sat.getInc(), sat.getIncBound().getLowerBound(), sat.getIncBound().getUpperBound()));
            }
            if (variableLocus.containsKey("ap")) {
                parent.setVariable(variableLocus.get("ap"),
                        new RealVariable(sat.getArgPer(), sat.getArgPerBound().getLowerBound(), sat.getArgPerBound().getUpperBound()));
            }
            if (variableLocus.containsKey("raan")) {
                parent.setVariable(variableLocus.get("raan"),
                        new RealVariable(sat.getRaan(), sat.getRaanBound().getLowerBound(), sat.getRaanBound().getUpperBound()));
            }
            if (variableLocus.containsKey("ta")) {
                parent.setVariable(variableLocus.get("ta"),
                        new RealVariable(sat.getTrueAnomaly(), sat.getAnomBound().getLowerBound(), sat.getAnomBound().getUpperBound()));
            }
            BinaryVariable manifest = new BinaryVariable(1);
            manifest.set(0, ((BooleanSatelliteVariable) sat).getManifest());
            parent.setVariable(variableLocus.size(), manifest);
            parents[i] = parent;
        }

        Solution[] offspring = operator.evolve(parents);

        SatelliteVariable[] out = new SatelliteVariable[satellites.length];
        for (int i = 0; i < satellites.length; i++) {
            Solution child = offspring[i];
            BooleanSatelliteVariable satVar = (BooleanSatelliteVariable) satellites[i];

            if (variableLocus.containsKey("sma")) {
                satVar.setSma(((RealVariable) child.getVariable(variableLocus.get("sma"))).getValue());
            }
            if (variableLocus.containsKey("ecc")) {
                satVar.setEcc(((RealVariable) child.getVariable(variableLocus.get("ecc"))).getValue());
            }
            if (variableLocus.containsKey("inc")) {
                satVar.setInc(((RealVariable) child.getVariable(variableLocus.get("inc"))).getValue());
            }
            if (variableLocus.containsKey("ap")) {
                satVar.setArgPer(((RealVariable) child.getVariable(variableLocus.get("ap"))).getValue());
            }
            if (variableLocus.containsKey("raan")) {
                satVar.setRaan(((RealVariable) child.getVariable(variableLocus.get("raan"))).getValue());
            }
            if (variableLocus.containsKey("ta")) {
                satVar.setTrueAnomaly(((RealVariable) child.getVariable(variableLocus.get("ta"))).getValue());
            }
            satVar.setManifest(((BinaryVariable) child.getVariable(variableLocus.size())).get(0));
            out[i] = satVar;
        }
        return out;
    }

}
