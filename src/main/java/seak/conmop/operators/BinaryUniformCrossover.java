/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators;

import java.util.ArrayList;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.UniformCrossover;
import org.moeaframework.core.variable.BinaryVariable;

/**
 * This operator will only try to cross variables that are binary variables
 *
 * @author nozomihitomi
 */
public class BinaryUniformCrossover extends UniformCrossover {

    public BinaryUniformCrossover(double probability) {
        super(probability);
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        ArrayList<Integer> decisionIndex = new ArrayList<>();

        //identify which decisions are binary variables
        for (int i = 0; i < parents[0].getNumberOfVariables(); i++) {
            if (parents[0].getVariable(i) instanceof BinaryVariable) {
                decisionIndex.add(i);
            }
        }
        //create temporary solutions that only contain the binary variables
        Solution[] tmp = new Solution[getArity()];
        for (int i=0; i< parents.length; i++) {
            Solution soln = new Solution(decisionIndex.size(), parents[i].getNumberOfObjectives());
            for(int j=0; j<decisionIndex.size(); j++){
                soln.setVariable(j, parents[i].getVariable(decisionIndex.get(j)));
            }
            tmp[i] = soln;
        }
        
        //apply uniform crossover on binary variables
        Solution[] tmpChildren = super.evolve(tmp);
        
        //set the recombined binary variables
        Solution[] children = new Solution[getArity()];
        for(int i=0; i<parents.length; i++){
            children[i] = parents[i].copy();
            for(int j=0; j<decisionIndex.size(); j++){
                children[i].setVariable(decisionIndex.get(j), tmpChildren[i].getVariable(j));
            }
        }
        return children;
    }

}
