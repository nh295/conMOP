/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators.knowledge;

import aos.operator.CheckParents;
import java.util.ArrayList;
import java.util.Collections;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import seak.conmop.deployment.DeploymentStrategy;
import seak.conmop.deployment.Installment;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 * This operator tries to increase the number of planes in the constellation by
 * taking satellites that are in the same or near the same plane and
 * redistributing them so that they are no longer in the same plane.
 *
 * @author nozomihitomi
 */
public class DecreasePlanes implements Variation, CheckParents {

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution child = parents[0].copy();
        for (int i = 0; i < child.getNumberOfVariables(); i++) {
            if (child.getVariable(i) instanceof ConstellationVariable) {
                child.setVariable(i, evolve((ConstellationVariable) child.getVariable(i)));
            }
        }
        return new Solution[]{child};
    }

    /**
     * The operation to decrease the number of planes in the constellation by
     * taking one plane and merging it with the inc and raan of another plane
     *
     * @param constelVariable
     * @return The modified instance of a constellation variable
     */
    private ConstellationVariable evolve(ConstellationVariable constelVariable) {
        if (constelVariable.getNumberOfSatellites() <= 1
                || constelVariable.getDeploymentStrategy().getInstallments().size() <= 1) {
            return constelVariable;
        }

        DeploymentStrategy deploymentStrategy = constelVariable.getDeploymentStrategy();

        //Select an installment to move
        ArrayList<Installment> candidates = new ArrayList(deploymentStrategy.getInstallments());
        Collections.shuffle(candidates);
        Installment candidate = candidates.get(0);

        //Find random installment to merge with
        Installment candidateOther = candidates.get(1);

        DescriptiveStatistics raans = new DescriptiveStatistics();
        DescriptiveStatistics incs = new DescriptiveStatistics();
        for (SatelliteVariable sat : candidateOther.getSatellites()) {
            raans.addValue(sat.getRaan());
            incs.addValue(sat.getInc());
        }
        double meanRaan = raans.getMean();
        double meanInc = incs.getMean();

        //move candidate to other's plane
        for (SatelliteVariable var : candidate.getSatellites()) {
            var.setInc(meanInc);
            var.setRaan(meanRaan);
        }

        ArrayList<SatelliteVariable> sats = new ArrayList<>();
        for (Installment installment : candidates) {
            sats.addAll(installment.getSatellites());
        }
        constelVariable.setSatelliteVariables(sats);
        return constelVariable;
    }

    @Override
    public boolean check(Solution[] parents) {
        for (Solution parent : parents) {
            for (int i = 0; i < parent.getNumberOfVariables(); i++) {
                if (parent.getVariable(i) instanceof ConstellationVariable) {
                    ConstellationVariable constelVariable
                            = (ConstellationVariable) parent.getVariable(i);
                    if (constelVariable.getDeploymentStrategy().getInstallments().size() > 1
                            && constelVariable.getDeploymentStrategy().getInstallments().size() >= constelVariable.getSatelliteBound().getLowerBound()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
