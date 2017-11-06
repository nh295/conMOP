/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.RandomInitialization;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.WalkerVariable;

/**
 * A initialization method that creates random walker constellations but allows
 * the search to explore non-walker constellations. All variables that are not
 * constellation variables will be randomly initialized
 *
 * @author nozomihitomi
 */
public class RandomWalkerInitialization extends RandomInitialization {

    public RandomWalkerInitialization(Problem problem, int populationSize) {
        super(problem, populationSize);
    }

    @Override
    public Solution[] initialize() {
        Solution[] initialPopulation = new Solution[populationSize];

        for (int i = 0; i < populationSize; i++) {
            Solution solution = problem.newSolution();

            for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                if (solution.getVariable(j) instanceof ConstellationVariable) {
                    ConstellationVariable constel = (ConstellationVariable) solution.getVariable(j);
                    WalkerVariable walker = new WalkerVariable(
                            constel.getSmaBound(), constel.getIncBound(),
                            constel.getSatelliteBound(),
                            constel.getSatelliteBound(),
                            constel.getSatelliteBound());
                    walker.randomize();
                    constel.setSatelliteVariables(walker.getSatelliteVariables());
                } else {
                    solution.getVariable(j).randomize();
                }
            }

            initialPopulation[i] = solution;
        }

        return initialPopulation;
    }

}
