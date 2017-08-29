/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 * This crossover is intended for a static length chromosome in which the number
 * of satellites in the constellation are allowed to vary but the length of the
 * chromosome representation remains constant. The crosspoint will be in the
 * identical location of both parents and the children will also have the same
 * length chromosome
 *
 * @author nhitomi
 */
public class StaticLengthOnePointCrossover implements Variation {

    /**
     * The probability of applying this operator to solutions.
     */
    private final double probability;

    /**
     * Constructs a static chromosome length one-point crossover operator with
     * the specified probability of applying this operator to solutions.
     *
     * @param probability the probability of applying this operator to solutions
     */
    public StaticLengthOnePointCrossover(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the probability of applying this operator to solutions.
     *
     * @return the probability of applying this operator to solutions
     */
    public double getProbability() {
        return probability;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        if (PRNG.nextDouble() <= probability) {
            for (int i = 0; i < result1.getNumberOfVariables(); i++) {
                if (result1.getVariable(i) instanceof ConstellationVariable
                        && result2.getVariable(i) instanceof ConstellationVariable) {

                    ConstellationVariable constel1 = (ConstellationVariable) result1.getVariable(i);
                    ConstellationVariable constel2 = (ConstellationVariable) result2.getVariable(i);

                    ConstellationVariable[] newVars = evolve(constel1, constel2);

                    result1.setVariable(i, newVars[0]);
                    result2.setVariable(i, newVars[1]);
                }
            }
        }

        return new Solution[]{result1, result2};
    }

    private ConstellationVariable[] evolve(ConstellationVariable constel1, ConstellationVariable constel2) {
        //check to see if both constellations have the same number of satellites
        if (constel1.getSatelliteVariables().size()!= constel2.getSatelliteVariables().size()) {
            throw new IllegalArgumentException(
                    "Expected both constellations to have the same number of satellites.");
        }

        if (constel1.getSatelliteVariables().size() > 1) {
            //select crossover point
            int crossoverPoint1 = PRNG.nextInt(
                    constel1.getSatelliteVariables().size() - 1);

            ArrayList<SatelliteVariable> satList1 = new ArrayList();
            ArrayList<SatelliteVariable> satList2 = new ArrayList();
            Iterator<SatelliteVariable> iter1 = constel1.getSatelliteVariables().iterator();
            Iterator<SatelliteVariable> iter2 = constel2.getSatelliteVariables().iterator();

            //exchange the first few satellites until the crossover point
            for (int j = 0; j <= crossoverPoint1; j++) {
                satList2.add(iter1.next());
                satList1.add(iter2.next());
            }

            //place the rest of constel1 satellites in list1
            while (iter1.hasNext()) {
                satList1.add(iter1.next());
            }
            //place the rest of constel2 satellites in list2
            while (iter2.hasNext()) {
                satList2.add(iter2.next());
            }
            
            if (new HashSet<>(satList1).size() != new HashSet<>(satList2).size()) {
                System.out.println("");
            }

            constel1.setSatelliteVariables(satList1);
            constel2.setSatelliteVariables(satList2);
        }
        return new ConstellationVariable[]{constel1, constel2};
    }

    @Override
    public int getArity() {
        return 2;
    }

}
