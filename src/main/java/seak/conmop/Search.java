/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import seak.orekit.propagation.PropagatorFactory;
import seak.orekit.propagation.PropagatorType;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import seak.conmop.util.Bounds;
import seak.orekit.STKGRID;
import seak.orekit.util.OrekitConfig;

/**
 *
 * @author nozomihitomi
 */
public class Search {

    private static final double DEG_TO_RAD = Math.PI / 180.;

    /**
     * @param args the command line arguments
     * @throws org.orekit.errors.OrekitException
     */
    public static void main(String[] args) throws OrekitException {
        //if running on a non-US machine, need the line below
        Locale.setDefault(new Locale("en", "US"));
        long startTime = System.nanoTime();

        OrekitConfig.init();

        TimeScale utc = TimeScalesFactory.getUTC();
        AbsoluteDate startDate = new AbsoluteDate(2016, 1, 1, 00, 00, 00.000, utc);
        AbsoluteDate endDate = new AbsoluteDate(2016, 1, 10, 00, 00, 00.000, utc);
        
        //Enter satellite orbital parameters
        double a = 6978137.0;

        PropagatorFactory pf = new PropagatorFactory(PropagatorType.KEPLERIAN);

        Properties problemProperty = new Properties();
        problemProperty.setProperty("fov.numThreads", "6");

        Bounds<Integer> tBounds = new Bounds(1, 3);
        Bounds<Double> smaBounds = new Bounds(a + 400000, a + 800000);
        Bounds<Double> incBounds = new Bounds(30. * DEG_TO_RAD, 100. * DEG_TO_RAD);

        Problem problem = new WalkerOptimizer("", startDate, endDate, pf, 
                new HashSet(STKGRID.getPoints20()), tBounds, smaBounds, 
                incBounds, problemProperty);

        int populationSize = 100;
        Initialization initialization = new RandomInitialization(problem,
                populationSize);

        Population population = new Population();
        DominanceComparator comparator = new ParetoDominanceComparator();
        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(new double[]{1, 1});
        final TournamentSelection selection = new TournamentSelection(2, comparator);
        Variation variation = new CompoundVariation(new SBX(1, 20), new PM(0.01, 20));
        EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive,
                selection, variation, initialization, comparator);

        int maxNFE = 1000;
        
        System.out.println(String.format("Initializing population... Size = %d",populationSize));
        while (emoea.getNumberOfEvaluations() < maxNFE) {
            emoea.step();
            double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9))/60.;
            System.out.print(
                    String.format("\r%d NFE out of %d NFE: Time elapsed = %10f min."
                            + " Approximate time remaining %10f min.",
                    emoea.getNumberOfEvaluations(), maxNFE, currentTime, 
                    currentTime/emoea.getNumberOfEvaluations()*(maxNFE-emoea.getNumberOfEvaluations())));
        }
        System.out.println(emoea.getArchive().size());
        System.out.println(Arrays.toString(emoea.getArchive().get(0).getObjectives()));

        long endTime = System.nanoTime();
        Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));
    }

}
