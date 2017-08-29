/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.operator.real.PM;
import static org.moeaframework.core.operator.real.PM.evolve;
import org.moeaframework.core.variable.RealVariable;

/**
 * This polynomial mutation will always use a probability that is the inverse of
 * the length of the chromosome
 *
 * @author SEAK2
 */
public class VariablePM extends PM {

    public VariablePM(double distributionIndex) {
        super(1.0, distributionIndex);
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();

        for (int i = 0; i < result.getNumberOfVariables(); i++) {
            Variable variable = result.getVariable(i);

            if ((PRNG.nextDouble() <= 1. / result.getNumberOfVariables())
                    && (variable instanceof RealVariable)) {
                evolve((RealVariable) variable, getDistributionIndex());
            }
        }

        return new Solution[]{result};
    }
}
