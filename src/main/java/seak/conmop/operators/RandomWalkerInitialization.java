/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.RandomInitialization;
import seak.conmop.util.Bounds;
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

    /**
     * The bounds allowable on the number of planes
     */
    private final Bounds<Integer> pBound;

    /**
     * The bounds allowable on the phasing value
     */
    private final Bounds<Integer> fBound;

    /**
     * Allows all p and f values
     *
     * @param problem the problem
     * @param populationSize the number of solutions to initialize
     */
    public RandomWalkerInitialization(Problem problem, int populationSize) {
        this(problem, populationSize, null, null);
    }

    /**
     *
     * @param problem the problem
     * @param populationSize the number of solutions to initialize
     * @param pBound The bounds allowable on the number of planes
     * @param fBound The bounds allowable on the phasing value
     */
    public RandomWalkerInitialization(Problem problem, int populationSize,
            Bounds<Integer> pBound, Bounds<Integer> fBound) {
        super(problem, populationSize);
        this.pBound = pBound;
        this.fBound = fBound;
    }

    @Override
    public Solution[] initialize() {
        Solution[] initialPopulation = new Solution[populationSize];

        for (int i = 0; i < populationSize; i++) {
            Solution solution = problem.newSolution();

            for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                if (solution.getVariable(j) instanceof ConstellationVariable) {
                    ConstellationVariable constel = (ConstellationVariable) solution.getVariable(j);
                    WalkerVariable walker;
                    if (this.pBound == null && this.fBound == null) {
                        walker = new WalkerVariable(
                                constel.getSmaBound(), constel.getIncBound(),
                                constel.getSatelliteBound(),
                                constel.getSatelliteBound(),
                                constel.getSatelliteBound());
                    } else {
                        walker = new WalkerVariable(
                                constel.getSmaBound(), constel.getIncBound(),
                                constel.getSatelliteBound(),
                                this.pBound, this.fBound);
                    }
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
