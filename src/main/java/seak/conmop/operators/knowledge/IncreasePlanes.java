/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators.knowledge;

import aos.operator.AbstractCheckParent;
import java.util.ArrayList;
import java.util.Collections;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.hipparchus.util.FastMath;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import seak.conmop.deployment.DeploymentStrategy;
import seak.conmop.deployment.Installment;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;

/**
 * This operator tries to increase the number of planes in the constellation by
 * taking half of the satellites that are in the same or near the same plane and
 * placing them in a new plane at the same inclination but different RAAN
 *
 * @author nozomihitomi
 */
public class IncreasePlanes extends AbstractCheckParent  {

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolveParents(Solution[] parents) {
        Solution child = parents[0].copy();
        for (int i = 0; i < child.getNumberOfVariables(); i++) {
            if (child.getVariable(i) instanceof ConstellationVariable) {
                child.setVariable(i, evolve((ConstellationVariable) child.getVariable(i)));
            }
        }
        return new Solution[]{child};
    }

    /**
     * The operation to increase the number of planes in the constellation by
     * modifying the RAAN of a satellite in one of the installments
     *
     * @param constelVariable
     * @return The modified instance of a constellation variable
     */
    private ConstellationVariable evolve(ConstellationVariable constelVariable) {
        DeploymentStrategy deploymentStrategy = constelVariable.getDeploymentStrategy();

        ArrayList<SatelliteVariable> sats = new ArrayList<>();

        //randomly select a installment to break up
        ArrayList<Installment> installmentCandidates = new ArrayList();
        for (Installment installment : deploymentStrategy.getInstallments()) {
            if (installment.getSatellites().size() > 1) {
                installmentCandidates.add(installment);
            }
            sats.addAll(installment.getSatellites());
        }

        if (installmentCandidates.isEmpty()) {
            return constelVariable;
        }

        Collections.shuffle(installmentCandidates);
        Installment installmentCandidate = installmentCandidates.get(0);

        DescriptiveStatistics raans = new DescriptiveStatistics();
        for (SatelliteVariable sat : installmentCandidate.getSatellites()) {
            raans.addValue(sat.getRaan());
        }
        double meanRaan = raans.getMean();

        //try to set a different raan by choosing a random raan that is +[90,270] deg different from the mean
        double newRaan = FastMath.PI * PRNG.nextDouble() + FastMath.PI / 2. + meanRaan;
        if (newRaan > 2 * FastMath.PI) {
            newRaan -= 2 * FastMath.PI;
        }
        ArrayList<SatelliteVariable> satCandidates = new ArrayList<>(installmentCandidate.getSatellites());
        Collections.shuffle(satCandidates);
        //put half of the satellites in the chosen plane into new plane at new raan
        for(int i=0; i< Math.floorDiv(satCandidates.size(),2); i++){
            satCandidates.get(i).setRaan(newRaan);
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
                    if (constelVariable.getDeploymentStrategy().getInstallments().size()
                            < constelVariable.getSatelliteBound().getUpperBound()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
