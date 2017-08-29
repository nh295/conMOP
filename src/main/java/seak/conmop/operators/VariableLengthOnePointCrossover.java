/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import java.util.ArrayList;
import java.util.Iterator;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 * This crossover is intended for a variable length chromosome in which the
 * number of satellites in the constellation are allowed to vary and a gene is
 * defined as a satellite and its associated parameters (i.e. orbital
 * parameters, instruments etc). A crosspoint is identified from each parent and
 * crossed accordingly.
 *
 * @author nhitomi
 */
public class VariableLengthOnePointCrossover implements Variation {

    /**
     * The probability of applying this operator to solutions.
     */
    private final double probability;

    /**
     * Flag to declare whether there should be a cross point per chromosome or
     * if the crosspoint should be selected from the shorter of the two
     * chromosomes
     */
    private final boolean doubleCrossPoint;

    /**
     * Constructs a variable chromosome length one-point crossover operator with
     * the specified probability of applying this operator to solutions.
     *
     * @param probability the probability of applying this operator to solutions
     * @param doubleCrossPoint Flag to declare whether there should be a cross
     * point per chromosome or if the crosspoint should be selected from the
     * shorter of the two chromosomes
     */
    public VariableLengthOnePointCrossover(double probability, boolean doubleCrossPoint) {
        this.probability = probability;
        this.doubleCrossPoint = doubleCrossPoint;
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
                    result1.setVariable(i, newVars[1]);
                }
            }
        }

        return new Solution[]{result1, result2};
    }

    private ConstellationVariable[] evolve(ConstellationVariable var1, ConstellationVariable var2) {
        //identify the constellation with fewer satellites
        ConstellationVariable constel1;
        ConstellationVariable constel2;
        if (var1.getNumberOfSatellites() <= var2.getNumberOfSatellites()) {
            constel1 = var1;
            constel2 = var2;
        } else {
            constel1 = var2;
            constel2 = var1;
        }

        if (constel1.getNumberOfSatellites() > 1
                && constel2.getNumberOfSatellites() > 1) {
            //select crossover points
            int crossoverPoint1 = PRNG.nextInt(
                    constel1.getNumberOfSatellites() - 1);

            int crossoverPoint2;
            if (doubleCrossPoint) {
                crossoverPoint2 = PRNG.nextInt(
                        constel2.getNumberOfSatellites() - 1);
            } else {
                crossoverPoint2 = crossoverPoint1;
            }

            ArrayList<SatelliteVariable> satList1 = new ArrayList();
            ArrayList<SatelliteVariable> satList2 = new ArrayList();
            Iterator<SatelliteVariable> iter1 = constel1.getSatelliteVariables().iterator();
            Iterator<SatelliteVariable> iter2 = constel2.getSatelliteVariables().iterator();

            //exchange the first few satellites until the crossover point
            for (int j = 0; j <= crossoverPoint1; j++) {
                satList2.add(iter1.next());
            }

            //exchange the first few satellites until the crossover point
            for (int j = 0; j <= crossoverPoint2; j++) {
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
