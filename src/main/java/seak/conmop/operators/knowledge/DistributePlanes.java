/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.operators.knowledge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Operator that tries to distribute planes with similar inclinations evenly
 * about their right ascension of the ascending node
 *
 * @author nozomihitomi
 */
public class DistributePlanes implements Variation {

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
     * The operation to distribute all the planes in the constellation evenly
     *
     * @param constelVariable
     * @return The modified instance of a constellation variable
     */
    private ConstellationVariable evolve(ConstellationVariable constelVariable) {
        DeploymentStrategy deploymentStrategy = constelVariable.getDeploymentStrategy();

        ArrayList<SatelliteVariable> allSats = new ArrayList<>();
        ArrayList<Installment> installmentCandidates = new ArrayList();
        for (Installment installment : deploymentStrategy.getInstallments()) {
            installmentCandidates.add(installment);
            allSats.addAll(installment.getSatellites());
        }

        int nplanes = installmentCandidates.size();
        double separation = 2. * FastMath.PI / nplanes;

        //select random installment whose raan will remain constant
        Collections.sort(installmentCandidates, new InstallmentComparator());
        int n = PRNG.nextInt(nplanes);
        Installment anchor = installmentCandidates.get(n);
        DescriptiveStatistics anchorStats = new DescriptiveStatistics();
        for (SatelliteVariable sats : anchor.getSatellites()) {
            anchorStats.addValue(sats.getRaan());
        }
        double anchorRaan = anchorStats.getMean();

        for (int i = 0; i < n; i++) {
            double newRaan = anchorRaan - separation * (n - i);
            if (newRaan < 0) {
                newRaan += 2. * FastMath.PI;
            }
            for (SatelliteVariable sat : installmentCandidates.get(i).getSatellites()) {
                sat.setRaan(newRaan);
            }
        }
        for (int i = n + 1; i < installmentCandidates.size(); i++) {
            double newRaan = anchorRaan + separation * (i - n);
            if (newRaan > 2. * FastMath.PI) {
                newRaan -= 2. * FastMath.PI;
            }
            for (SatelliteVariable sat : installmentCandidates.get(i).getSatellites()) {
                sat.setRaan(newRaan);
            }
        }

        constelVariable.setSatelliteVariables(allSats);
        return constelVariable;
    }

    private class InstallmentComparator implements Comparator<Installment> {

        @Override
        public int compare(Installment o1, Installment o2) {
            DescriptiveStatistics stats1 = new DescriptiveStatistics();
            for (SatelliteVariable sats : o1.getSatellites()) {
                stats1.addValue(sats.getRaan());
            }
            DescriptiveStatistics stats2 = new DescriptiveStatistics();
            for (SatelliteVariable sats : o2.getSatellites()) {
                stats2.addValue(sats.getRaan());
            }

            return Double.compare(stats1.getMean(), stats2.getMean());
        }

    }

}
