/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators.knowledge;

import java.util.ArrayList;
import java.util.Collections;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.hipparchus.util.FastMath;
import org.moeaframework.core.PRNG;
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
public class IncreasePlanes implements Variation {

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
        
        if(installmentCandidates.isEmpty()){
            return constelVariable;
        }
        
        Collections.shuffle(installmentCandidates);
        Installment installmentCandidate = installmentCandidates.get(0);

        DescriptiveStatistics raans = new DescriptiveStatistics();
        for(SatelliteVariable sat : installmentCandidate.getSatellites()){
            raans.addValue(sat.getRaan());
        }
        double meanRaan = raans.getMean();
        
        //try to set a different raan by choosing a random raan that is +[90,270] deg different from the mean
        double newRaan = FastMath.PI*PRNG.nextDouble()+FastMath.PI/2. + meanRaan;
        if(newRaan > 2*FastMath.PI){
            newRaan -= 2*FastMath.PI;
        }
        ArrayList<SatelliteVariable> satCandidates = new ArrayList<>(installmentCandidate.getSatellites());
        Collections.shuffle(satCandidates);
        satCandidates.get(0).setRaan(newRaan);

        constelVariable.setSatelliteVariables(sats);
        return constelVariable;
    }

}
